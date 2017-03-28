package net.medcorp.library.worldclock;

import io.realm.annotations.RealmModule;

/**
 * Created by Jason on 2017/3/14.
 */

@RealmModule(library = true, classes = {City.class, TimeZone.class})
public class WorldClockLibraryModule {
}
