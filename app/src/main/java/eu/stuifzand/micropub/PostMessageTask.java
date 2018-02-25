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
import okhttp3.logging.HttpLoggingInterceptor;

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
        String content = postModel.content.get();

        FormBody.Builder builder = new FormBody.Builder();
        builder.add("h", "entry")
               .add("content", content);

        addCategories(builder, postModel.category.get());
        if (postModel.inReplyTo.get().startsWith("http")) {
            builder.add("in-reply-to", postModel.inReplyTo.get());
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
                .build();

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

    private FormBody.Builder addCategories(FormBody.Builder builder, String category) {
        String[] categories = category.split("\\s+");
        if (categories.length == 1 && categories[0].equals("")) {
            return builder;
        }
        for (String cat : categories) {
            builder.add("category[]", cat);
        }
        return builder;
    }

    protected void onPostExecute(String message) {
        Toast.makeText(context.get(), message, Toast.LENGTH_SHORT).show();
        postModel.clear();
    }
}
