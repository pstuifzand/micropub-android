package eu.stuifzand.micropub.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;

import static eu.stuifzand.micropub.auth.WebSigninActivityKt.AUTHENTICATION_REQUEST;


public class WebsigninTask extends AsyncTask<String, Void, Bundle> {
    static final String ME = "eu.stuifzand.micropub.ME";
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
        Log.i("micropub", "Starting WebsigninTask.doInBackground");
        Bundle bundle = new Bundle();
        HashMap<String, String> linkHeaders = new HashMap<>();
        try {
            String profileUrl = strings[0];
            bundle.putString(ME, profileUrl);
            Connection conn = Jsoup.connect(profileUrl);
            conn.timeout(10*1000);
            Document doc = conn.get();
            Connection.Response resp = conn.response();

            List<String> headers = resp.headers("Link");
            Pattern linkParser = Pattern.compile("<([^>]+)>;\\s+rel=\"([^\"]+)\"");

            for (String header : headers) {
                Log.d("micropub", header);
                Matcher matcher = linkParser.matcher(header);
                while (matcher.find()) {
                    MatchResult results = matcher.toMatchResult();

                    String url = results.group(1);
                    String rel = results.group(2);

                    HttpUrl base = HttpUrl.parse(profileUrl);
                    HttpUrl resolvedUrl = base.resolve(url);

                    if (resolvedUrl != null && !rel.isEmpty() && !linkHeaders.containsKey(rel)) {
                        Log.d("micropub", "Found url=" + resolvedUrl + " and rel=" + rel);
                        linkHeaders.put(rel, resolvedUrl.toString());
                    }
                }
            }

            Elements links = doc.select("link");
            for (Element link : links) {
                String rel = link.attr("rel");
                if (!rel.isEmpty() && !linkHeaders.containsKey(rel)) {
                    linkHeaders.put(rel, link.attr("href"));
                }
            }
        } catch (Exception e) {
            bundle.putString("ERROR", e.getMessage());
            return bundle;
        }

        String[] rels = new String[]{"authorization_endpoint", "token_endpoint", "micropub"};

        for (String rel : rels) {
            if (linkHeaders.containsKey(rel)) {
                bundle.putString(rel, linkHeaders.get(rel));
            }
        }

        for (String rel : rels) {
            if (bundle.getString(rel) == null) {
                bundle.putString("ERROR", "Missing header or link: " + rel);
                break;
            }
        }
        Log.i("micropub", bundle.toString());
        return bundle;
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        String error = bundle.getString("ERROR");
        if (error != null && error.length() > 0) {
            Toast.makeText(this.activity, error, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this.activity, AuthenticationActivity.class);
        intent.putExtras(activity.getIntent());
        intent.putExtras(bundle);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        this.activity.startActivityForResult(intent, AUTHENTICATION_REQUEST);
    }
}
