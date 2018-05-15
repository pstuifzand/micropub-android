package eu.stuifzand.micropub.auth;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface AuthDao {
    @Query("SELECT * FROM auth WHERE state = :state")
    Auth load(String state);

    @Insert
    void save(Auth auth);

    @Delete
    void delete(Auth auth);
}
