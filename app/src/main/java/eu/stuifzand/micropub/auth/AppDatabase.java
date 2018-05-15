package eu.stuifzand.micropub.auth;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Auth.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AuthDao authDao();
}
