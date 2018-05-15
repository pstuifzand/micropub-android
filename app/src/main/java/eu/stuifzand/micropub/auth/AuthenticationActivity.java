package eu.stuifzand.micropub.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URI;

import eu.stuifzand.micropub.R;
import eu.stuifzand.micropub.utils.RandomStringUtils;
import okhttp3.HttpUrl;

public class AuthenticationActivity extends AccountAuthenticatorActivity {
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private static final String TAG = "AuthenticationActivity";
    public static final String PARAM_USER_PASS = "eu.stuifzand.micropub.UserPass";

    private Bundle bundle;
    private Intent newIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (savedInstanceState != null) {
            bundle = savedInstanceState.getBundle("response");
            return;
        }

        Intent intent = getIntent();
        bundle = intent.getExtras();

        if ("android.intent.action.VIEW".equals(intent.getAction())) {
            Log.i("micropub", intent.toString());
            Uri uri = intent.getData();
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state"); // @TODO: check/use state

            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "auth").build();
            new AsyncTask<String, Void, Auth>() {
                @Override
                protected Auth doInBackground(String... strings) {
                    String state = strings[0];
                    Auth auth = db.authDao().load(state);
                    db.close();
                    return auth;
                }

                @Override
                protected void onPostExecute(Auth auth) {
                    if (auth != null) {
                        finish();
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "Indieauth");
                    bundle.putString(AccountManager.KEY_ACCOUNT_NAME, auth.getMe());
                    bundle.putString(AuthenticationActivity.PARAM_USER_PASS, code);

                    Intent loginIntent = new Intent();
                    loginIntent.putExtras(bundle);
                    finishLogin(loginIntent);
                }
            }.execute(state);
            return;
        }

        String endpoint = bundle.getString("authorization_endpoint");
        String me = bundle.getString(WebsigninTask.ME);
        AccountAuthenticatorResponse response = bundle.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        String state = RandomStringUtils.randomString(16);

        HttpUrl.Builder builder = HttpUrl.parse(endpoint).newBuilder();
        builder.setQueryParameter("me", me)
                .setQueryParameter("client_id", "https://wrimini.net")
                .setQueryParameter("redirect_uri", "https://wrimini.net/oauth/callback")
                .setQueryParameter("response_type", "code")
                .setQueryParameter("state", state)
                .setQueryParameter("scope", "create"); // @TODO use different scope

        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "auth")
                .fallbackToDestructiveMigration()
                .build();

        Auth auth = new Auth(state, me);

        new AsyncTask<Auth, Void, Void>() {
            @Override
            protected Void doInBackground(Auth... auths) {
                db.authDao().save(auths[0]);
                db.close();
                return null;
            }
        }.execute(auth);

        String url = builder.toString();
        Log.i("micropub", "LoadUrl: " + url);
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        webIntent.setData(Uri.parse(url));
        startActivity(webIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle("response", bundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            bundle = getIntent().getExtras();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("micropub", "onCreate action: " + intent.getAction());
        if ("android.intent.action.VIEW".equals(intent.getAction())) {
            Log.i("micropub", intent.toString());
            Uri uri = intent.getData();
            String code = uri.getQueryParameter("code");
            //String state = uri.getQueryParameter("state"); // @TODO: check/use state
            Bundle response = bundle;

            String me = response.getString(WebsigninTask.ME);

            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "Indieauth");
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, me);
            bundle.putString(AuthenticationActivity.PARAM_USER_PASS, code);

            Intent loginIntent = new Intent();
            loginIntent.putExtras(bundle);
            finishLogin(loginIntent);

            AccountAuthenticatorResponse r = response.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
            if (r != null) {
                r.onResult(bundle);
            }
        }
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
