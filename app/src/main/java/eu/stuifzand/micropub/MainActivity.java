package eu.stuifzand.micropub;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import eu.stuifzand.micropub.databinding.ActivityMainBinding;
import okhttp3.HttpUrl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        PostViewModel model = ViewModelProviders.of(MainActivity.this).get(PostViewModel.class);
        binding.setViewModel(model);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            String urlOrNote = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (urlOrNote != null) {
                HttpUrl url = HttpUrl.parse(urlOrNote);
                if (url != null) {
                    model.inReplyTo.set(urlOrNote);
                } else {
                    model.content.set(urlOrNote);
                }
            }
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_send) {
//            EditText mEdit   = (EditText)findViewById(R.id.editContent);
//            new PostMessageTask(this.findViewById(R.id.editContent)).execute("http://192.168.178.21:5000/micropub", mEdit.getText().toString());
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendPost(View view) {
        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();
        am.getAuthTokenByFeatures("Indieauth", "token", null, this, options, null, new OnTokenAcquired(true), null);
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        private boolean sendMessage;

        public OnTokenAcquired(boolean sendMessage) {
            this.sendMessage = sendMessage;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, 0);
                    return;
                }

                // The token is a named value in the bundle. The name of the value
                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
                String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                if (sendMessage) {
                    PostViewModel model = ViewModelProviders.of(MainActivity.this).get(PostViewModel.class);
                    AccountManager am = AccountManager.get(MainActivity.this);
                    Account[] accounts = am.getAccountsByType(bundle.getString("accountType"));
                    String accountName = bundle.getString("authAccount");

                    String micropubBackend = null;
                    for (Account account : accounts) {
                        if (account.name.equals(accountName)) {
                            micropubBackend = am.getUserData(account, "micropub");
                        }
                    }

                    if (micropubBackend != null) {
                        Log.i("micropub", "Sending message to " + micropubBackend);
                        new PostMessageTask(MainActivity.this, token, model, micropubBackend)
                                .execute();
                    }
                }

                Log.d("micropub", "GetTokenForAccount Bundle is " + token);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }
        }
    }

    public void startSignin(View view) {
        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();
        am.getAuthTokenByFeatures("Indieauth", "token", null, this, options, null, new OnTokenAcquired(false), null);
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
