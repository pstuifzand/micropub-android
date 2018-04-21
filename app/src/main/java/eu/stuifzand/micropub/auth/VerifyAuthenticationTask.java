package eu.stuifzand.micropub.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;


public class VerifyAuthenticationTask extends AsyncTask<String, Void, VerifyAuthenticationTask.AuthenticationResult> {
    private final AccountAuthenticatorResponse response;
    private final AuthenticationActivity activity;

    public class AuthenticationResult {
        private boolean success;
        private String errorMessage;

        public String me;
        public String code;

        public AuthenticationResult(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
        }

        public AuthenticationResult(String me, String code) {
            this.success = true;
            this.me = me;
            this.code = code;
        }

        public boolean isSuccessful() {
            return this.success;
        }

        public String getErrorMessage() {
            return errorMessage;
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
                .add("redirect_uri", "wrimini://oauth")
                .add("client_id", "https://stuifzand.eu/micropub")
                .build();

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .url(endpoint)
                .method("POST", formBody)
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
        Response response = null;
        try {
            response = call.execute();
            if (!response.isSuccessful()) {
                return new AuthenticationResult("Unsuccessful response from authorization_endpoint: HTTP status code is " + String.valueOf(response.code()));
            }
            ResponseBody body = response.body();
            if (!response.header("Content-Type").contains("application/json")) {
                return new AuthenticationResult("Unsupported content type of authorization_endpoint response: " + response.header("Content-Type"));
            }

            JsonParser parser = new JsonParser();
            try {
                JsonElement jsonElement = parser.parse(body.string());
                JsonObject element = jsonElement.getAsJsonObject();

                JsonElement meElement = element.get("me");
                if (meElement == null) {
                    return new AuthenticationResult("Missing element \"me\" in authorization_endpoint response");
                }
                String resultMe = meElement.getAsString();
                return new AuthenticationResult(resultMe, code);
            } catch (JsonParseException e) {
                return new AuthenticationResult("Could not parse json response from authorization_endpoint");
            }
        } catch (IOException e) {
            return new AuthenticationResult("Could not get the response from the endpoint");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected void onPostExecute(AuthenticationResult message) {
        if (message.isSuccessful()) {
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, message.me);
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "Indieauth");
            bundle.putString(AuthenticationActivity.PARAM_USER_PASS, message.code);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            this.activity.finishLogin(intent);
            this.response.onResult(bundle);
        } else {
            this.response.onError(AccountManager.ERROR_CODE_BAD_AUTHENTICATION, "Could not verify authorization: " + message.getErrorMessage());
        }
    }
}
