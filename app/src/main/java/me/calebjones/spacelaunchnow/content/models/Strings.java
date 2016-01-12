package me.calebjones.spacelaunchnow.content.models;


public final class Strings {
    public static final String ACTION_FAILURE_PREV_LAUNCHES = "FAILURE_PREV_LAUNCHES";
    public static final String ACTION_FAILURE_UPC_LAUNCHES = "FAILURE_UP_LAUNCHES";
    public static final String ACTION_FAILURE_ROCKETS = "FAILURE_GET_ROCKETS";
    public static final String ACTION_FAILURE_MISSIONS = "FAILURE_GET_MISSION";

    public static final String ACTION_GET_PREV_LAUNCHES = "GET_PREV_LAUNCHES";
    public static final String ACTION_GET_UP_LAUNCHES = "GET_UP_LAUNCHES";
    public static final String ACTION_GET_ROCKETS = "GET_ROCKETS";
    public static final String ACTION_GET_ALL = "GET_ALL_UPDATES";

    public static final String ACTION_SUCCESS_ROCKETS = "SUCCESS_GET_ROCKETS";
    public static final String ACTION_SUCCESS_PREV_LAUNCHES = "SUCCESS_PREV_LAUNCHES";
    public static final String ACTION_SUCCESS_UP_LAUNCHES = "SUCCESS_UP_LAUNCHES";
    public static final String ACTION_SUCCESS_MISSIONS = "SUCCESS_GET_MISSIONS";


    public static final String ACTION_UPDATE_UP_LAUNCHES = "UPDATE_UP_LAUNCHES";
    public static final String ACTION_UPDATE_PREV_LAUNCHES = "UPDATE_PREV_LAUNCHES";
    public static final String ACTION_UPDATE_WEATHER_STATUS = "UPDATE_WEATHER_STATUS";
    public static int NOTIF_ID = 568974;

    public static String MISSION_URL = "https://launchlibrary.net/1.1.1/mission?limit=1000&mode=verbose";

    private Strings() {
    }
}