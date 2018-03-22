package eu.stuifzand.micropub.client;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

class PostTask extends AsyncTask<String, Void, Void> {

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
        builder.add("h", "entry");

        if (post.hasContent()) {
            builder.add("content", post.getContent());
        }
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

        if (post.hasPhoto()) {
            builder.add("photo", post.getPhoto());
        }

        if (post.hasLikeOf()) {
            builder.add("like-of", post.getLikeOf());
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
            int code = httpResponse.code();
            if (code == 201) {
                String location = httpResponse.header("Location");
                response.postValue(Response.successful(location));
            } else {
                response.postValue(Response.failed(httpResponse.toString()));
            }

        } catch (IOException e) {
            response.postValue(Response.failed(e.getMessage()));
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
        return null;
    }
}
