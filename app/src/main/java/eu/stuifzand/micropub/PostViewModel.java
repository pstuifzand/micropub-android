package eu.stuifzand.micropub;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;

import java.util.Arrays;

import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Syndication;

public class PostViewModel extends ViewModel {
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> category = new ObservableField<>();
    public final ObservableField<String> inReplyTo = new ObservableField<>();

    public PostViewModel() {
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
    }

    public void clear() {
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
    }
}
