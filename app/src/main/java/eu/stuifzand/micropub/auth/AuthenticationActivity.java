package eu.stuifzand.micropub.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import eu.stuifzand.micropub.R;
import okhttp3.HttpUrl;

public class AuthenticationActivity extends AccountAuthenticatorActivity {
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private static final String TAG = "AuthenticationActivity";
    public static final String PARAM_USER_PASS = "eu.stuifzand.micropub.UserPass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Intent intent = getIntent();
        final String endpoint = intent.getStringExtra("authorization_endpoint");
        final String me = intent.getStringExtra(WebsigninTask.ME);
        final AccountAuthenticatorResponse response = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);


        WebView webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        HttpUrl.Builder builder = HttpUrl.parse(endpoint).newBuilder();
        builder.setQueryParameter("me", me)
                .setQueryParameter("client_id", "https://stuifzand.eu/micropub")
                .setQueryParameter("redirect_uri", "https://stuifzand.eu/micropub-auth")
                .setQueryParameter("response_type", "code")
                .setQueryParameter("state", "1234")
                .setQueryParameter("scope", "create edit update post delete");
        Log.i("micropub", builder.toString());
        webview.loadUrl(builder.toString());
        webview.setWebViewClient(new WebViewClient() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean shouldOverrideUrlLoading(WebView viewx, WebResourceRequest request) {
                Log.i("micropub", "New API: " + request.getUrl().toString());
                String url = request.getUrl().toString();
                if (url.startsWith("https://stuifzand.eu/micropub-auth")) {
                    HttpUrl httpUrl = HttpUrl.parse(url);
                    String code = httpUrl.queryParameter("code");
                    String state = httpUrl.queryParameter("state");

                    new VerifyAuthenticationTask(response, AuthenticationActivity.this).execute(endpoint, me, code);

                    return true;
                }
                viewx.loadUrl(request.getUrl().toString());
                return false;
            }

            public boolean shouldOverrideUrlLoading(WebView viewx, String url) {
                // TODO: fix for older versions
                Log.i("micropub", "Old API: " + url);
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    viewx.loadUrl(url);
                    return false;
                }
                return true;
            }
        });
    }

    void finishLogin(Intent intent) {
        Log.d("micropub", TAG + "> finishLogin");
        AccountManager accountManager = AccountManager.get(this);
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d("micropub", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = "FULL_ACCESS";

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            Bundle bundle = new Bundle();
            bundle.putAll(getIntent().getExtras());
            Log.i("micropub", bundle.toString());
            accountManager.addAccountExplicitly(account, accountPassword, bundle);
            accountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d("micropub", TAG + "> finishLogin > setPassword");
            accountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}
