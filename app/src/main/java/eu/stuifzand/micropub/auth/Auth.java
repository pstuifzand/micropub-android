package eu.stuifzand.micropub.auth;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Auth {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "state")
    private String state;

    @ColumnInfo(name = "me")
    private String me;

    //private String authorization_endpoint;
    public Auth(String state, String me) {
        this.state = state;
        this.me = me;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMe() {
        return me;
    }

    public void setMe(String me) {
        this.me = me;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
