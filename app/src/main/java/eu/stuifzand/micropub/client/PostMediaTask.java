package eu.stuifzand.micropub.client;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

class PostMediaTask extends AsyncTask<String, Void, Void> {
    private MutableLiveData<Response> mediaResponse;
    private HttpUrl mediaEndpoint;
    private String accessToken;
    private byte[] output;
    private String mimeType;

    PostMediaTask(MutableLiveData<Response> mediaResponse, HttpUrl mediaEndpoint, String accessToken, byte[] output, String mimeType) {
        this.mediaResponse = mediaResponse;
        this.mediaEndpoint = mediaEndpoint;
        this.accessToken = accessToken;
        this.output = output;
        this.mimeType = mimeType;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.i("micropub", "output size: " + output.length);
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "photo.jpg", RequestBody.create(MediaType.parse(mimeType), output))
                .build();

        Request request = new Request.Builder()
                .url(mediaEndpoint)
                .addHeader("Authorization", "Bearer " + accessToken)
                .method("POST", formBody)
                .build();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Call call = client.newCall(request);
        try {
            okhttp3.Response httpResponse = call.execute();
            if (httpResponse.code() == 201) {
                Log.i("micropub", "response received");
                String location = httpResponse.header("Location");
                mediaResponse.postValue(Response.successful(location));
            } else {
                Log.i("micropub", httpResponse.body().string());
            }
        } catch (IOException e) {
            Log.e("micropub", "Error while sending image", e);
        }
        return null;
    }
}
