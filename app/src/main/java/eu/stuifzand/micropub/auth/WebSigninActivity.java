package eu.stuifzand.micropub.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import eu.stuifzand.micropub.R;


public class WebSigninActivity extends AppCompatActivity {

    public static final int AUTHENTICATION_REQUEST = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_signin);
    }


    public void startWebsignin(View view) {
        EditText profileUrl = (EditText) findViewById(R.id.profileUrl);
        String url = profileUrl.getText().toString();

        Intent intent = getIntent();
        AccountAuthenticatorResponse parcelable = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        new WebsigninTask(this, parcelable).execute(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTHENTICATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}
