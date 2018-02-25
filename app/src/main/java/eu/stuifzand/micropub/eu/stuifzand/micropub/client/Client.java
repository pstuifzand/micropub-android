package eu.stuifzand.micropub.eu.stuifzand.micropub.client;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class Client extends AndroidViewModel {
    private MutableLiveData<Response> response = new MutableLiveData<>();

    public Client(@NonNull Application application) {
        super(application);
    }

    public void createPost(Post post, String accessToken, HttpUrl micropubBackend) {
        new PostTask(post, micropubBackend, accessToken, response).execute();
    }

    public LiveData<Response> getResponse() {
        return response;
    }

    private static class PostTask extends AsyncTask<String, Void, Void> {

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

            if (post.hasInReplyTo()) {
                builder.add("in-reply-to", post.getInReplyTo());
            }

            if (post.hasName()) {
                builder.add("name", post.getName());
            }

            RequestBody formBody = builder.build();
            micropubBackend = HttpUrl.parse("http://192.168.178.21:5000/micropub");
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
