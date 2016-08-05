package net.medcorp.library.worldclock;

import io.realm.RealmObject;

/**
 * Created by karl-john on 5/8/2016.
 */

public class TestCity extends RealmObject{

    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
