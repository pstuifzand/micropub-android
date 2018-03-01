package eu.stuifzand.micropub.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

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


public class Authenticator extends AbstractAccountAuthenticator {

    private final Context context;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(context, WebSigninActivity.class);

        // This key can be anything. Try to use your domain/package
        intent.putExtra("eu.stuifzand.micropub.AccountType", accountType);

        // This key can be anything too. It's just a way of identifying the token's type (used when there are multiple permissions)
        intent.putExtra("full_access", authTokenType);

        // This key can be anything too. Used for your reference. Can skip it too.
        intent.putExtra(AuthenticationActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);

        // Copy this exactly from the line below.
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        AccountManager am = AccountManager.get(this.context);

        String authToken = am.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            RequestBody formBody = new FormBody.Builder()
                    .add("code", am.getPassword(account))
                    .add("redirect_uri", "https://stuifzand.eu/micropub-auth")
                    .add("client_id", "https://stuifzand.eu/micropub")
                    .add("me", account.name)
                    .add("grant_type", "authorization_code")
                    .build();

            String tokenEndpoint = am.getUserData(account, "token_endpoint");
            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .url(tokenEndpoint)
                    .method("POST", formBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            String msg;
            Call call = client.newCall(request);
            Response tokenResponse = null;
            try {
                tokenResponse = call.execute();
                if (tokenResponse.isSuccessful()) {
                    ResponseBody body = tokenResponse.body();
                    JsonParser parser = new JsonParser();
                    JsonObject element = parser.parse(body.string()).getAsJsonObject();
                    authToken = element.get("access_token").getAsString();
                }
            } catch (IOException e) {
                Log.e("micropub", "Failed getting token response", e);
            } finally {
                if (tokenResponse != null) {
                    tokenResponse.close();
                }
            }
        }


        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If you reach here, person needs to login again. or sign up

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity which is the AccountsActivity in my case.
        final Intent intent = new Intent(context, WebSigninActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra("eu.stuifzand.micropub.AccountType", account.type);
        intent.putExtra("full_access", authTokenType);
        intent.putExtra(AuthenticationActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);

        Bundle retBundle = new Bundle();
        retBundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return retBundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return "Indieauth";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
