package eu.stuifzand.micropub.auth

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import eu.stuifzand.micropub.R
import kotlinx.android.synthetic.main.activity_web_signin.*

const val AUTHENTICATION_REQUEST = 14;

class WebSigninActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_signin)
    }

    fun startWebsignin(view : View) {
        val url = profileUrl.text.toString();
        val me = if (!url.matches(Regex.fromLiteral("^https?://"))) "https://$url" else url
        val parcelable = intent.getParcelableExtra<AccountAuthenticatorResponse>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        WebsigninTask(this, parcelable).execute(me);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTHENTICATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}
