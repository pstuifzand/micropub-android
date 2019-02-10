package eu.stuifzand.micropub.client;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

public class Destination {
    public ObservableBoolean checked = new ObservableBoolean();
    public ObservableField<String> uid = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();

    public Destination(String uid, String name) {
        this.checked.set(false);
        this.uid.set(uid);
        this.name.set(name);
    }
}