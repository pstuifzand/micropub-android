package eu.stuifzand.micropub;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.stuifzand.micropub.databinding.ActivityMainBinding;
import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Post;
import eu.stuifzand.micropub.client.Syndication;
import okhttp3.HttpUrl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        PostViewModel model = ViewModelProviders.of(MainActivity.this).get(PostViewModel.class);
        Client client = ViewModelProviders.of(MainActivity.this).get(Client.class);
        binding.setViewModel(model);
        binding.setClient(client);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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

        TokenReady callback = (accountType, accountName, token) -> {
            Log.i("micropub", "TokenReady called " + accountType + " " + accountName + " " + token);
            client.setToken(accountType, accountName, token);
            client.loadSyndicates();
        };

        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();
        am.getAuthTokenByFeatures("Indieauth", "token", null, this, options, null, new OnTokenAcquired(callback), null);
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendPost(View view) {
        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();
        TokenReady callback = new TokenReady() {
            @Override
            public void tokenReady(String accountType, String accountName, String token) {

                PostViewModel model = ViewModelProviders.of(MainActivity.this).get(PostViewModel.class);
                AccountManager am = AccountManager.get(MainActivity.this);
                Account[] accounts = am.getAccountsByType(accountType);

                String micropubBackend = null;
                for (Account account : accounts) {
                    if (account.name.equals(accountName)) {
                        micropubBackend = am.getUserData(account, "micropub");
                    }
                }

                if (micropubBackend != null) {
                    Log.i("micropub", "Sending message to " + micropubBackend);
                    Client client = ViewModelProviders.of(MainActivity.this).get(Client.class);
                    client.getResponse().observe(MainActivity.this, response -> {
                        Log.i("micropub", "response received " + response.isSuccess());
                        if (response.isSuccess()) {
                            model.clear();
                        }
                    });
                    Post post = new Post(null, model.content.get(), model.category.get(), HttpUrl.parse(model.inReplyTo.get()));
                    List<String> uids = new ArrayList<String>();
                    for (Syndication s : client.syndicates) {
                        if (s.checked.get()) {
                            uids.add(s.uid.get());
                        }
                    }
                    post.setSyndicationUids(uids.toArray(new String[uids.size()]));
                    client.createPost(post, token, HttpUrl.parse(micropubBackend));
                }
            }
        };

        am.getAuthTokenByFeatures("Indieauth", "token", null, this, options, null, new OnTokenAcquired(callback), null);
    }

    public class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        private final TokenReady callback;

        public OnTokenAcquired(TokenReady callback) {
            this.callback = callback;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    MainActivity.this.startActivityForResult(launch, 0);
                    return;
                }

                // The token is a named value in the bundle. The name of the value
                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
                String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                Log.d("micropub", "GetTokenForAccount Bundle is " + token);
                callback.tokenReady(bundle.getString("accountType"), bundle.getString("authAccount"), token);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }
        }
    }
}
