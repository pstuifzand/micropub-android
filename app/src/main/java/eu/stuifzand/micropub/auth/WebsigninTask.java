package eu.stuifzand.micropub.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class WebsigninTask extends AsyncTask<String, Void, Bundle> {
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
    protected Bundle doInBackground(String... strings) {
        Bundle bundle = new Bundle();
        try {
            bundle.putString(ME, strings[0]);
            Document doc = Jsoup.connect(strings[0]).get();
            Elements links = doc.select("link");
            for (Element link : links) {
                String rel = link.attr("rel");
                if (rel.equals("authorization_endpoint")) {
                    bundle.putString(rel, link.attr("href"));
                }
                else if (rel .equals("token_endpoint")) {
                    bundle.putString(rel, link.attr("href"));
                }
                else if (rel.equals("micropub")) {
                    bundle.putString(rel, link.attr("href"));
                }
            }

        } catch (IOException e) {
            return bundle;
        }
        Log.i("micropub", bundle.toString());
        return bundle;
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        Intent intent = new Intent(this.activity, AuthenticationActivity.class);
        intent.putExtras(activity.getIntent());
        intent.putExtras(bundle);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        this.activity.startActivity(intent);
    }
}
