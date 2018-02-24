package eu.stuifzand.micropub;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetPageTask extends AsyncTask<String, Void, String> {
    protected View view;
    public GetPageTask(View view)
    {
        this.view = view;
    }

    @Override
    protected String doInBackground(String... strings) {
        Request request = new Request.Builder()
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
        }
        catch (IOException e) {
            return e.getMessage();
        }
        finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected void onPostExecute(String message) {
        String url = "https://peterstuifzand.nl/";
        Toast.makeText(this.view.getContext(), message, 1).show();
    }
}
