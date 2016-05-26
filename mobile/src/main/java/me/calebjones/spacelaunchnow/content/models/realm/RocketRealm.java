
package me.calebjones.spacelaunchnow.content.models.realm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import me.calebjones.spacelaunchnow.content.models.Family;
import me.calebjones.spacelaunchnow.content.models.RocketAgency;

public class RocketRealm extends RealmObject {

    @PrimaryKey
    private Integer id;
    private String name;
    private String configuration;
    private String familyname;
    private String infoURL;
    private String wikiURL;
    private String imageURL;
    private FamilyRealm family;
    private RealmList<RocketAgencyRealm> agencies = new RealmList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfoURL() {
        return infoURL;
    }

    public void setInfoURL(String infoURL) {
        this.infoURL = infoURL;
    }

    public String getWikiURL() {
        return wikiURL;
    }

    public void setWikiURL(String wikiURL) {
        this.wikiURL = wikiURL;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public FamilyRealm getFamily() {
        return family;
    }

    public void setFamily(FamilyRealm family) {
        this.family = family;
    }

    public String getFamilyname() {
        return familyname;
    }

    public void setFamilyname(String familyname) {
        this.familyname = familyname;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public RealmList<RocketAgencyRealm> getAgencies() {
        return agencies;
    }

    public void setAgencies(RealmList<RocketAgencyRealm> agencies) {
        this.agencies = agencies;
    }


}
