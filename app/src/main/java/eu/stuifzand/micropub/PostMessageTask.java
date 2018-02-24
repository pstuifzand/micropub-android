package eu.stuifzand.micropub;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
    private EditText mEdit;
    private WeakReference<Context> context;

    public PostMessageTask(Context context, String accessToken, EditText mEdit) {
        this.context = new WeakReference<Context>(context);
        this.accessToken = accessToken;
        this.mEdit = mEdit;
    }

    @Override
    protected String doInBackground(String... strings) {
        RequestBody formBody = new FormBody.Builder()
                .add("content", strings[1])
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .method("POST", formBody)
                .url(strings[0])
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
        mEdit.setText("");
        Toast.makeText(context.get(), message, Toast.LENGTH_SHORT).show();
    }
}
