package eu.stuifzand.micropub;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Post;
import eu.stuifzand.micropub.databinding.ActivityLikeBinding;
import okhttp3.HttpUrl;

public class LikeActivity extends AppCompatActivity {

    private AccountManager accountManager;

    private Account selectedAccount;
    private String authToken;
    private Client client;
    private PostViewModel postModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountManager = AccountManager.get(this);

        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();

        postModel = ViewModelProviders.of(LikeActivity.this).get(PostViewModel.class);
        client = ViewModelProviders.of(LikeActivity.this).get(Client.class);

        TokenReady callback = (accountType, accountName, token) -> {
            Account[] accounts = accountManager.getAccountsByType(accountType);
            if (accounts.length == 0)
                return;
            selectedAccount = accounts[0];
            authToken = token;

            String micropubBackend = accountManager.getUserData(selectedAccount, "micropub");
            if (micropubBackend == null) return;

            client.setToken(accountType, accountName, token);
            client.loadConfig(HttpUrl.parse(micropubBackend));

            final View coordinator = findViewById(R.id.coordinator);

            client.getResponse().observe(LikeActivity.this, response -> {
                Log.i("micropub", "response received " + response.isSuccess());
                if (response.isSuccess()) {
                    postModel.clear();
                    Snackbar.make(coordinator, R.string.post_successful, Snackbar.LENGTH_LONG)
                            .setAction("Open", v -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.getUrl()));
                                startActivity(browserIntent);
                            })
                            .show();
                } else {
                    Snackbar.make(coordinator, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                }
            });

            client.getMediaResponse().observe(LikeActivity.this, response -> {
                Log.i("micropub", "media response received " + response.isSuccess());
                if (response.isSuccess()) {
                    postModel.setPhoto(response.getUrl());
                    Toast.makeText(LikeActivity.this, "Photo upload succesful, photo url filled", Toast.LENGTH_SHORT).show();
                }
            });
        };

        AuthError onError = (msg) -> {
            LikeActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(LikeActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        };
        accountManager.getAuthTokenByFeatures(
                "Indieauth",
                "token",
                null,
                this,
                options,
                null,
                new OnTokenAcquired(this, callback, onError),
                null
        );

//        setContentView(R.layout.activity_main);
        ActivityLikeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_like);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        binding.setViewModel(postModel);
        binding.setClient(client);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent intent = getIntent();
        Log.i("micropub", intent.toString());
        if (intent != null) {
            String urlOrNote = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (urlOrNote != null) {
                HttpUrl url = HttpUrl.parse(urlOrNote);
                if (url != null) {
                    postModel.likeOf.set(urlOrNote);
                } else {
                    postModel.findLikeOf(urlOrNote);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_like, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (this.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                inputManager.hideSoftInputFromInputMethod(this.getCurrentFocus().getWindowToken(), 0);
            }
            sendPost(null);
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendPost(View view) {
        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();

        TokenReady callback = (accountType, accountName, token) -> {
            String micropubBackend = accountManager.getUserData(selectedAccount, "micropub");
            if (micropubBackend == null) {
                Log.i("micropub", "micropub backend == null");
                return;
            }
            Log.i("micropub", "Sending message to " + micropubBackend);
            Post post = postModel.getPost();
            client.createPost(post, token, HttpUrl.parse(micropubBackend));
        };
        AuthError onError = (msg) -> {
            LikeActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(LikeActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        };

        accountManager.getAuthTokenByFeatures("Indieauth", "token", null, this, options, null, new OnTokenAcquired(this, callback, onError), null);
    }
}
