package eu.stuifzand.micropub;

import android.arch.lifecycle.MutableLiveData;
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
            "(?:^|[\\W])(((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<String> category = new ObservableField<>();
    public final ObservableField<String> inReplyTo = new ObservableField<>();
    public final ObservableField<String> photo = new ObservableField<>();
    public final ObservableField<String> likeOf = new ObservableField<>();
    public final ObservableField<String> bookmarkOf = new ObservableField<>();
    public final ObservableField<String> postStatus = new ObservableField<>();
    public final ObservableField<String> visibility = new ObservableField<>();


    public PostViewModel() {
        this.name.set("");
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
        this.photo.set("");
        this.likeOf.set("");
        this.bookmarkOf.set("");
        this.postStatus.set("");
        this.visibility.set("");
    }

    public void clear() {
        this.name.set("");
        this.content.set("");
        this.category.set("");
        this.inReplyTo.set("");
        this.photo.set("");
        this.likeOf.set("");
        this.bookmarkOf.set("");
        this.postStatus.set("");
        this.visibility.set("");
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

    public void findBookmarkOf(String urlOrNote) {
        Matcher matcher = urlPattern.matcher(urlOrNote);
        if (matcher.find()) {
            String url = matcher.group(1);
            bookmarkOf.set(url);
            String s = urlOrNote.replaceFirst(urlPattern.pattern(), "");
            this.name.set(s);
        } else {
            this.content.set(urlOrNote);
        }
    }

    public void setPhoto(String url) {
        this.photo.set(url);
    }

    public Post getPost() {
        Post post = new Post(name.get(), content.get(), category.get(), HttpUrl.parse(inReplyTo.get()));
        if (!this.photo.get().equals("")) {
            post.setPhoto(this.photo.get());
        }
        if (!this.likeOf.get().equals("")) {
            post.setLikeOf(HttpUrl.parse(likeOf.get()));
        }
        if (!this.bookmarkOf.get().equals("")) {
            post.setBookmarkOf(HttpUrl.parse(bookmarkOf.get()));
        }
        if (!this.postStatus.get().equals("")) {
            post.setPostStatus((postStatus.get()));
        }
        if (!this.visibility.get().equals("")) {
            post.setPostStatus((visibility.get()));
        }
        return post;
    }
}
