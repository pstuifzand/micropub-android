package eu.stuifzand.micropub.eu.stuifzand.micropub.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class WebsigninTask extends AsyncTask<String, Void, WebsigninTask.AuthResponse> {
    public static final String ENDPOINT = "eu.stuifzand.microsub.ENDPOINT";
    public static final String ME = "eu.stuifzand.micropub.ME";
    protected AccountAuthenticatorResponse response;
    protected Activity activity;

    public class AuthResponse {
        String me;
        String endpoint;
    }

    public WebsigninTask(Activity activity, AccountAuthenticatorResponse response) {
        this.activity = activity;
        this.response = response;
    }

    @Override
    protected AuthResponse doInBackground(String... strings) {
        try {
            Document doc = Jsoup.connect(strings[0]).get();
            Elements links = doc.select("link[rel=\"authorization_endpoint\"]");
            for (Element link : links) {
                String href = link.attr("href");

                AuthResponse auth = new AuthResponse();
                auth.me = strings[0];
                auth.endpoint = href;
                return auth;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(AuthResponse auth) {
        Intent intent = new Intent(this.activity, AuthenticationActivity.class);
        intent.putExtras(activity.getIntent());
        intent.putExtra(ENDPOINT, auth.endpoint);
        intent.putExtra(ME, auth.me);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        this.activity.startActivity(intent);
    }
}
