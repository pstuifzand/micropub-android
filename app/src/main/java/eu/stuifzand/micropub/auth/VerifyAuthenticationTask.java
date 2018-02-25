package eu.stuifzand.micropub.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class VerifyAuthenticationTask extends AsyncTask<String, Void, VerifyAuthenticationTask.AuthenticationResult> {
    private final AccountAuthenticatorResponse response;
    private final AuthenticationActivity activity;

    public class AuthenticationResult {
        public String me;
        public String scope;
        public String code;

        public AuthenticationResult(String me, String scope, String code) {
            this.me = me;
            this.scope = scope;
            this.code = code;
        }
    }

    public VerifyAuthenticationTask(AccountAuthenticatorResponse response, AuthenticationActivity activity) {
        this.response = response;
        this.activity = activity;
    }

    @Override
    protected AuthenticationResult doInBackground(String[] args) {
        String endpoint = args[0];
        String me = args[1];
        String code = args[2];

        RequestBody formBody = new FormBody.Builder()
                .add("code", code)
                .add("redirect_uri", "https://stuifzand.eu/micropub-auth")
                .add("client_id", "https://stuifzand.eu/micropub")
                .build();
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .url(args[0])
                .method("POST", formBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        String msg;
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            ResponseBody body = response.body();
            JsonParser parser = new JsonParser();
            JsonObject element = parser.parse(body.string()).getAsJsonObject();
            return new AuthenticationResult(element.get("me").getAsString(), element.get("scope").getAsString(), code);
        } catch (IOException e) {
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected void onPostExecute(AuthenticationResult message) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, message.me);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "Indieauth");
        bundle.putString(AuthenticationActivity.PARAM_USER_PASS, message.code);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        this.activity.finishLogin(intent);
        this.response.onResult(bundle);
    }
}
