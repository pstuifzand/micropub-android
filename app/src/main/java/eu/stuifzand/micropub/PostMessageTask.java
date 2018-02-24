package eu.stuifzand.micropub;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostMessageTask extends AsyncTask<String, Void, String> {
    private final String accessToken;
    private PostViewModel postModel;
    private String micropubBackend;
    private WeakReference<Context> context;

    public PostMessageTask(Context context, String accessToken, PostViewModel postModel, String micropubBackend) {
        this.context = new WeakReference<Context>(context);
        this.accessToken = accessToken;
        assert postModel != null;
        this.postModel = postModel;
        this.micropubBackend = micropubBackend;
    }

    @Override
    protected String doInBackground(String... strings) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("content", postModel.content.get());
        RequestBody formBody = builder.build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .method("POST", formBody)
                .url(micropubBackend)
                .build();

        OkHttpClient client = new OkHttpClient();
        String msg;
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            msg = Integer.toString(response.code());
            return msg;
        } catch (IOException e) {
            Log.e("micropub", e.getMessage(), e);
            return e.getMessage();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected void onPostExecute(String message) {
        Toast.makeText(context.get(), message, Toast.LENGTH_SHORT).show();
        postModel.content.set("");
    }
}
