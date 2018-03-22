package eu.stuifzand.micropub;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class OnTokenAcquired implements AccountManagerCallback<Bundle> {
    private final TokenReady callback;
    private AuthError error;
    private Activity activity;

    public OnTokenAcquired(Activity activity, TokenReady callback, AuthError error) {
        this.activity = activity;
        this.callback = callback;
        this.error = error;
    }

    @Override
    public void run(AccountManagerFuture<Bundle> result) {
        // Get the result of the operation from the AccountManagerFuture.
        try {
            Bundle bundle = result.getResult();
            Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
            if (launch != null) {
                activity.startActivityForResult(launch, 0);
                return;
            }

            // The token is a named value in the bundle. The name of the value
            // is stored in the constant AccountManager.KEY_AUTHTOKEN.
            String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            Log.d("micropub", "GetTokenForAccount Bundle is " + token);
            callback.tokenReady(bundle.getString("accountType"), bundle.getString("authAccount"), token);
        } catch (OperationCanceledException e) {
            Log.e("micropub", "on token acquired", e);
            error.handleErrorMessage(e.getMessage());
        } catch (IOException e) {
            Log.e("micropub", "on token acquired", e);
            error.handleErrorMessage(e.getMessage());
        } catch (AuthenticatorException e) {
            Log.e("micropub", "on token acquired", e);
            error.handleErrorMessage(e.getMessage());
        }
    }
}
