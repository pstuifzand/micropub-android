package eu.stuifzand.micropub.client;

import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.util.Log;

public class Syndication {
    public ObservableBoolean checked = new ObservableBoolean();
    public ObservableField<String> uid = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();

    public Syndication(String uid, String name) {
        this.checked.set(false);
        this.uid.set(uid);
        this.name.set(name);
    }
}
