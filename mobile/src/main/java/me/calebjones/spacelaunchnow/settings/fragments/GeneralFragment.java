package me.calebjones.spacelaunchnow.settings.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import de.mrapp.android.preference.activity.PreferenceFragment;
import io.realm.Realm;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.calendar.CalendarSyncService;
import me.calebjones.spacelaunchnow.calendar.model.Calendar;
import me.calebjones.spacelaunchnow.calendar.model.CalendarItem;
import me.calebjones.spacelaunchnow.content.database.SwitchPreferences;
import me.calebjones.spacelaunchnow.settings.util.CalendarPermissionListener;
import me.calebjones.spacelaunchnow.supporter.SupporterHelper;
import timber.log.Timber;

public class GeneralFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private Realm mRealm;
    private MultiplePermissionsListener allPermissionsListener;
    private SwitchPreferences switchPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        context = getActivity();
        mRealm = Realm.getDefaultInstance();

        Dexter.continuePendingRequestsIfPossible(allPermissionsListener);

        createPermissionListeners();
        setupPreference();
    }

    @Override
    public void onResume() {
        Timber.v("onResume - setting OnSharedPreferenceChangeListener");
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        switchPreferences = SwitchPreferences.getInstance(this.context);
        super.onResume();
    }

    @Override
    public void onPause() {
        Timber.v("onPause - removing OnSharedPreferenceChangeListener");
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
        Timber.d("onDestroy");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.i("General preference %s changed.", key);
        if (key.equals("calendar_reminder_array") ) {
            CalendarSyncService.startActionSyncAll(context);
        }

        if (key.equals("calendar_count")){
            CalendarSyncService.startActionResync(context);
        }

        if (key.equals("calendar_sync_state")) {
            Timber.v("Calendar Sync State: %s", sharedPreferences.getBoolean(key, true));
            if (sharedPreferences.getBoolean(key, true)) {
                Timber.v("Calendar Status: %s", switchPreferences.getCalendarStatus());
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    Timber.v("Calendar Permission - Granted");
                    switchPreferences.setCalendarStatus(true);
                    setCalendarPreference();
                    if (mRealm.where(CalendarItem.class).findFirst() == null){
                        setDefaultCalendar();
                    } else {
                        CalendarSyncService.startActionSyncAll(context);
                    }
                } else {
                    Timber.v("Calendar Permission - Denied/Pending");
                    checkCalendarPermission();
                }
            } else {
                CalendarSyncService.startActionDeleteAll(context);
                switchPreferences.setCalendarStatus(false);
            }
        }
    }

    private void setupPreference() {
        SwitchPreference calendarSyncState = (SwitchPreference) findPreference("calendar_sync_state");
        PreferenceCategory calendarCategory = (PreferenceCategory) findPreference("calendar_category");
        if (!SupporterHelper.isSupporter()) {
            calendarSyncState.setChecked(false);
            calendarSyncState.setEnabled(false);
            calendarCategory.setTitle(calendarCategory.getTitle() + " (Supporter Feature)");
        } else {
            if (calendarSyncState.isChecked() && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                setCalendarPreference();
            } else if (calendarSyncState.isChecked() && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Calendar permission denied, try re-selecting or go to Android Settings -> Apps to enable.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setCalendarPreference() {
        final List<Calendar> calendarList = Calendar.getWritableCalendars(context.getContentResolver());

        final ArrayList<String> listName = new ArrayList<String>();
        ArrayList<String> listCount = new ArrayList<String>();

        for (int i = 0; i < calendarList.size(); i++) {
            Calendar calendar = calendarList.get(i);
            listName.add(calendar.accountName);
            listCount.add(String.valueOf(i));
        }

        final CharSequence[] nameSequences = listName.toArray(new CharSequence[listName.size()]);
        final CharSequence[] countSequences = listCount.toArray(new CharSequence[listCount.size()]);
        ListPreference calendarPrefList = (ListPreference) findPreference("default_calendar_state");
        calendarPrefList.setEntries(nameSequences);
        calendarPrefList.setEntryValues(countSequences);

        String summary;
        final CalendarItem calendarItem = mRealm.where(CalendarItem.class).findFirst();

        if (calendarItem != null){
            summary = "Current calendar: " + calendarItem.getAccountName();
        } else {
            summary = "Select a Calendar to add launch events.";
        }
        calendarPrefList.setSummary(summary);
        calendarPrefList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                final Calendar calendar = calendarList.get(Integer.valueOf(obj.toString()));
                Timber.v("Updating selected calendar to %s", calendar.accountName);
                final CalendarItem calendarItem = new CalendarItem();
                calendarItem.setId(calendar.id);
                calendarItem.setAccountName(calendar.accountName);
                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        CalendarItem oldCalendar = realm.where(CalendarItem.class).findFirst();
                        if (oldCalendar != null && (oldCalendar.getId() != calendarItem.getId())) {
                            realm.where(CalendarItem.class).findAll().deleteAllFromRealm();
                            realm.copyToRealm(calendarItem);
                        } else {
                            realm.copyToRealmOrUpdate(calendarItem);
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Timber.v("Successfully updated active Calendar, sending resync.");
                        setCalendarPreference();
                        CalendarSyncService.startActionResync(context);
                    }
                });

                return true;
            }
        });
    }

    private void setDefaultCalendar() {
        final List<Calendar> calendarList = Calendar.getWritableCalendars(context.getContentResolver());

        if (calendarList.size() > 0) {
            ListPreference calendarPreference = (ListPreference) findPreference("default_calendar_state");
            String summary;
            final CalendarItem calendarItem = new CalendarItem();
            calendarItem.setAccountName(calendarList.get(0).accountName);
            calendarItem.setId(calendarList.get(0).id);

            if (calendarItem != null) {
                summary = "Default calendar: " + calendarItem.getAccountName();
            } else {
                summary = "Select a Calendar to add launch events.";
            }
            calendarPreference.setSummary(summary);
            mRealm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(CalendarItem.class).findAll().deleteAllFromRealm();
                    realm.copyToRealm(calendarItem);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    CalendarSyncService.startActionSyncAll(context);
                }
            });
        } else {
            Toast.makeText(context, "No Calendars available to sync launch events with.", Toast.LENGTH_LONG).show();
            SwitchPreference calendarSyncState = (SwitchPreference) findPreference("calendar_sync_state");
            calendarSyncState.setChecked(false);
            switchPreferences.setCalendarStatus(false);
        }
    }

    private void createPermissionListeners() {
        MultiplePermissionsListener feedbackViewMultiplePermissionListener =
                new CalendarPermissionListener(this);

        allPermissionsListener =
                new CompositeMultiplePermissionsListener(feedbackViewMultiplePermissionListener,
                        DialogOnAnyDeniedMultiplePermissionsListener.Builder.withContext(context)
                                .withTitle("Permission Denied")
                                .withMessage("If you change your mind, try to enable again. Or go to Settings -> Application -> Space Launch Now -> Permissions.")
                                .withButtonText(android.R.string.ok)
                                .withIcon(R.mipmap.ic_launcher)
                                .build());
    }

    public void checkCalendarPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Dexter.isRequestOngoing()) {
                return;
            }
            Dexter.checkPermissions(allPermissionsListener, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
        }
    }

    public void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Dexter.isRequestOngoing()) {
                return;
            }
            Dexter.checkPermissions(allPermissionsListener, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(context).setTitle("Calendar Permission Needed")
                .setMessage("This permission is needed to sync launches with your calendar.")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switchPreferences.setCalendarStatus(false);
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        switchPreferences.setCalendarStatus(false);
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }

    public void showPermissionGranted(String permission) {
        switchPreferences.setCalendarStatus(true);
        setCalendarPreference();
        if (mRealm.where(CalendarItem.class).findFirst() == null){
            setDefaultCalendar();
        }
    }

    public void showPermissionDenied(String permission, boolean isPermanentlyDenied) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("calendar_sync_state", false);
        editor.apply();
        SwitchPreference calendarSyncState = (SwitchPreference) findPreference("calendar_sync_state");
        calendarSyncState.setChecked(false);
        switchPreferences.setCalendarStatus(false);
        if (isPermanentlyDenied){
            Toast.makeText(context, "Calendar permission denied, please go to Android Settings -> Apps to enable.", Toast.LENGTH_LONG).show();
        }
    }

}

