package eu.stuifzand.micropub;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Post;
import eu.stuifzand.micropub.client.Syndication;
import okhttp3.HttpUrl;

public class PostViewModel extends ViewModel {
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> category = new ObservableField<>();
    public final ObservableField<String> inReplyTo = new ObservableField<>();
    public final ObservableField<String> photo = new ObservableField<>();
    public final ObservableField<String> likeOf = new ObservableField<>();

    public PostViewModel() {
        this.name.set("");
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
        this.photo.set("");
        this.likeOf.set("");
    }

    public void clear() {
        this.name.set("");
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
        this.photo.set("");
        this.likeOf.set("");
    }

    public void findReplyTo(String urlOrNote) {
        Matcher matcher = urlPattern.matcher(urlOrNote);
        if (matcher.find()) {
            String url = matcher.group(1);
            inReplyTo.set(url);
            String s = urlOrNote.replaceFirst(urlPattern.pattern(), "");
            this.content.set(s);
        } else {
            this.content.set(urlOrNote);
        }
    }

    public void findLikeOf(String urlOrNote) {
        Matcher matcher = urlPattern.matcher(urlOrNote);
        if (matcher.find()) {
            likeOf.set(matcher.group(1));
        }
    }

    public void setPhoto(String url) {
        this.photo.set(url);
    }

    public Post getPost() {
        Post post = new Post(null, content.get(), category.get(), HttpUrl.parse(inReplyTo.get()));
        if (!this.photo.get().equals("")) {
            post.setPhoto(this.photo.get());
        }
        post.setLikeOf(HttpUrl.parse(likeOf.get()));
        return post;
    }
}
