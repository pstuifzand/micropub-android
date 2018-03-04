package eu.stuifzand.micropub.client;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class MicropubConfigTask extends AsyncTask<String, Void, String> {

    private final OkHttpClient client;
    private final HttpUrl micropubBackend;
    private final String accessToken;
    private final String configKey;
    private MicropubConfigResponseCallback callback;

    public MicropubConfigTask(OkHttpClient client, HttpUrl micropubBackend, String accessToken, MicropubConfigResponseCallback callback, String configKey) {
        this.client = client;
        this.micropubBackend = micropubBackend;
        this.accessToken = accessToken;
        this.callback = callback;
        this.configKey = configKey;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpUrl backend = micropubBackend.newBuilder()
                .setQueryParameter("q", configKey)
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .method("GET", null)
                .url(backend)
                .build();

        Call call = client.newCall(request);
        okhttp3.Response httpResponse = null;

        try {
            httpResponse = call.execute();
            if (httpResponse.code() == 200) {
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(httpResponse.body().string());
                callback.handleResponse(element);
            }
        } catch (IOException e) {
            Log.e("micropub", "Error while getting syndicate-to", e);
        } catch (JsonSyntaxException e) {
            Log.e("micropub", "Error while getting parsing json response", e);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
        return null;
    }
}
