package eu.stuifzand.micropub.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableArrayList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import eu.stuifzand.micropub.MainActivity;
import eu.stuifzand.micropub.TokenReady;
import eu.stuifzand.micropub.auth.VerifyAuthenticationTask;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class Client extends AndroidViewModel {
    private MutableLiveData<Response> response = new MutableLiveData<>();
    public final ObservableArrayList<Syndication> syndicates = new ObservableArrayList<>();

    private String accountType;
    private String accountName;
    private String token;

    public Client(@NonNull Application application) {
        super(application);
    }

    class LoadSyndicatesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            AccountManager am = AccountManager.get(Client.this.getApplication());
            Account[] accounts = am.getAccountsByType(accountType);
            String micropubBackend = null;
            for (Account account : accounts) {
                if (account.name.equals(accountName)) {
                    micropubBackend = am.getUserData(account, "micropub");
                    break;
                }
            }
            if (micropubBackend != null) {
                HttpUrl backend = HttpUrl.parse(micropubBackend);
                backend = backend.newBuilder()
                        .setQueryParameter("q", "syndicate-to")
                        .build();

                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + token)
                        .method("GET", null)
                        .url(backend)
                        .build();

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                Call call = client.newCall(request);
                okhttp3.Response httpResponse = null;
                try {
                    httpResponse = call.execute();
                    if (httpResponse.code() == 200) {
                        JsonParser parser = new JsonParser();
                        JsonObject element = parser.parse(httpResponse.body().string()).getAsJsonObject();
                        JsonArray arr = element.getAsJsonArray("syndicate-to");
                        syndicates.clear();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject syn = arr.get(i).getAsJsonObject();
                            syndicates.add(new Syndication(syn.get("uid").getAsString(), syn.get("name").getAsString()));
                        }
                    }
                } catch (IOException e) {
                } finally {
                    if (httpResponse != null) {
                        httpResponse.close();
                    }
                }
            }
            return null;
        }
    }

    public void loadSyndicates() {
        new LoadSyndicatesTask().execute();
    }

    public void createPost(Post post, String accessToken, HttpUrl micropubBackend) {
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

    private class PostTask extends AsyncTask<String, Void, Void> {

        private Post post;
        private HttpUrl micropubBackend;
        private String accessToken;
        private MutableLiveData<Response> response;

        PostTask(Post post, HttpUrl micropubBackend, String accessToken, MutableLiveData<Response> response) {
            this.post = post;
            this.micropubBackend = micropubBackend;
            this.accessToken = accessToken;
            this.response = response;
        }

        @Override
        protected Void doInBackground(String... strings) {
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("h", "entry")
                    .add("content", post.getContent());

            for (String cat : post.getCategories()) {
                builder.add("category[]", cat);
            }

            for (String uid : post.getSyndicationUids()) {
                builder.add("mp-syndicate-to[]", uid);
            }

            if (post.hasInReplyTo()) {
                builder.add("in-reply-to", post.getInReplyTo());
            }

            if (post.hasName()) {
                builder.add("name", post.getName());
            }

            RequestBody formBody = builder.build();

            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .method("POST", formBody)
                    .url(micropubBackend)
                    .build();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Call call = client.newCall(request);
            okhttp3.Response httpResponse = null;
            try {
                httpResponse = call.execute();
                if (httpResponse.code() == 201) {
                    String location = httpResponse.header("Location");
                    response.postValue(Response.successful(location));
                } else {
                    response.postValue(Response.failed());
                }
                for (Syndication s : Client.this.syndicates) {
                    s.checked.set(false);
                }
            } catch (IOException e) {
                response.postValue(Response.failed());
            } finally {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            }
            return null;
        }
    }
}
