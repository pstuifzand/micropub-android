package eu.stuifzand.micropub;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;

import java.util.Arrays;

import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Post;
import eu.stuifzand.micropub.client.Syndication;
import okhttp3.HttpUrl;

public class PostViewModel extends ViewModel {
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> category = new ObservableField<>();
    public final ObservableField<String> inReplyTo = new ObservableField<>();
    public final ObservableField<String> photo = new ObservableField<>();

    public PostViewModel() {
        this.name.set("");
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
        this.photo.set("");
    }

    public void clear() {
        this.name.set("");
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
        this.photo.set("");
    }

    public void setPhoto(String url) {
        this.photo.set(url);
    }

    public Post getPost() {
        Post post = new Post(null, content.get(), category.get(), HttpUrl.parse(inReplyTo.get()));
        if (!this.photo.get().equals("")) {
            post.setPhoto(this.photo.get());
        }
        return post;
    }
}
