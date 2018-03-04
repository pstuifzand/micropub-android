package eu.stuifzand.micropub.client;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class Client extends ViewModel {
    private final OkHttpClient httpClient;

    private MutableLiveData<Response> response = new MutableLiveData<>();
    private MutableLiveData<Response> mediaResponse = new MutableLiveData<>();
    public final ObservableArrayList<Syndication> syndicates = new ObservableArrayList<>();

    private String accountType;
    private String accountName;
    private String token;
    private HttpUrl mediaEndpoint;

    public Client() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public OkHttpClient getClient() {
        return httpClient;
    }

    public LiveData<Response> getMediaResponse() {
        return mediaResponse;
    }

    public void loadConfig(HttpUrl micropubBackend) {
        MicropubConfigResponseCallback callback = configElement -> {
            // Media endpoint
            JsonObject config = configElement.getAsJsonObject();
            JsonElement elem = config.get("media-endpoint");
            if (elem != null) {
                setMediaEndpoint(elem.getAsString());
            }

            // Syndications.
            JsonArray arr = config.getAsJsonArray("syndicate-to");
            if (arr != null) {
                syndicates.clear();
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject syn = arr.get(i).getAsJsonObject();
                    syndicates.add(new Syndication(syn.get("uid").getAsString(), syn.get("name").getAsString()));
                }
            }
        };
        new MicropubConfigTask(this.getClient(), micropubBackend, token, callback, "config").execute();
    }

    public void loadSyndicates(HttpUrl micropubBackend) {
        MicropubConfigResponseCallback callback = element -> {
            JsonArray arr = element.getAsJsonObject().getAsJsonArray("syndicate-to");
            syndicates.clear();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject syn = arr.get(i).getAsJsonObject();
                syndicates.add(new Syndication(syn.get("uid").getAsString(), syn.get("name").getAsString()));
            }
        };
        new MicropubConfigTask(this.getClient(), micropubBackend, token, callback, "syndicate-to").execute();
    }

    public void createPost(Post post, String accessToken, HttpUrl micropubBackend) {
        List<String> uids = new ArrayList<String>();
        for (Syndication s : syndicates) {
            if (s.checked.get()) {
                uids.add(s.uid.get());
            }
        }
        post.setSyndicationUids(uids.toArray(new String[uids.size()]));

        new PostTask(post, micropubBackend, accessToken, response).execute();
    }

    public LiveData<Response> getResponse() {
        return response;
    }

    public void setToken(String accountType, String accountName, String token) {
        this.accountType = accountType;
        this.accountName = accountName;
        this.token = token;
    }

    public void postMedia(byte[] output, @NonNull String mimeType) {
        new PostMediaTask(mediaResponse, mediaEndpoint, token, output, mimeType).execute();
    }

    public void setMediaEndpoint(String mediaEndpoint) {
        this.mediaEndpoint = HttpUrl.parse(mediaEndpoint);
    }

    public String getMediaEndpoint() {
        return this.mediaEndpoint.toString();
    }
}
