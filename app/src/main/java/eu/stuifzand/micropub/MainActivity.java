package eu.stuifzand.micropub;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Post;
import eu.stuifzand.micropub.databinding.ActivityMainBinding;
import okhttp3.HttpUrl;

import static eu.stuifzand.micropub.utils.IOUtils.getBytes;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 12;
    private AccountManager accountManager;

    private Account selectedAccount;
    private String authToken;
    private Client client;
    private PostViewModel postModel;

    private ActionMenuItemView actionSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = AccountManager.get(this);

        AccountManager am = AccountManager.get(this);
        Bundle options = new Bundle();

        postModel = ViewModelProviders.of(MainActivity.this).get(PostViewModel.class);
        client = ViewModelProviders.of(MainActivity.this).get(Client.class);

        TokenReady callback = (accountType, accountName, token) -> {
            Account[] accounts = accountManager.getAccountsByType(accountType);
            if (accounts.length == 0)
                return;
            for (int i = 0; i < accounts.length; i++) {
                Log.i("micropub", "accounts["+i+"] = " + accounts[i].name + " " + accounts[i].type);
                if (accounts[i].type.equals(accountType)&&accounts[i].name.equals(accountName)) {
                    selectedAccount = accounts[i];
                    authToken = token;
                    break;
                }
            }

            String micropubBackend = accountManager.getUserData(selectedAccount, "micropub");
            if (micropubBackend == null) return;

            client.setToken(accountType, accountName, token);
            client.loadConfig(HttpUrl.parse(micropubBackend));

            final View coordinator = findViewById(R.id.coordinator);

            client.getResponse().observe(MainActivity.this, response -> {
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

            client.getMediaResponse().observe(MainActivity.this, response -> {
                Log.i("micropub", "media response received " + response.isSuccess());
                if (response.isSuccess()) {
                    postModel.setPhoto(response.getUrl());
                    Toast.makeText(MainActivity.this, "Photo upload succesful, photo url filled", Toast.LENGTH_SHORT).show();
                }
            });
        };

        AuthError onError = (msg) -> {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
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
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(postModel);
        binding.setClient(client);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            String urlOrNote = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (urlOrNote != null) {
                HttpUrl url = HttpUrl.parse(urlOrNote);
                if (url != null) {
                    postModel.inReplyTo.set(urlOrNote);
                } else {
                    postModel.findReplyTo(urlOrNote);
                }
            }
        }
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

        if (id == R.id.action_send) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (this.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                inputManager.hideSoftInputFromInputMethod(this.getCurrentFocus().getWindowToken(), 0);
            }
            sendPost(null);
        } else if (id == R.id.action_photo) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (this.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                inputManager.hideSoftInputFromInputMethod(this.getCurrentFocus().getWindowToken(), 0);
            }
            galleryIntent(null);
        } else if (id == R.id.action_save_draft) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (this.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
                inputManager.hideSoftInputFromInputMethod(this.getCurrentFocus().getWindowToken(), 0);
            }
            postModel.postStatus.set("draft");
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
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        };

        if (selectedAccount == null || authToken == null) {
            accountManager.getAuthTokenByFeatures("Indieauth", "token", null, this, options, null, new OnTokenAcquired(this, callback, onError), null);
        } else {
            callback.tokenReady(selectedAccount.type, selectedAccount.name, authToken);
        }
    }

    public void galleryIntent(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Log.i("micropub", "response received " + data.toString());
        try (InputStream input = getApplicationContext().getContentResolver().openInputStream(data.getData())) {
            byte[] output = getBytes(input);
            String mimeType = data.getType();
            if (mimeType == null) {
                mimeType = data.resolveType(getApplicationContext().getContentResolver());
            }

            client.postMedia(output, mimeType);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("micropub", "Error while copying image", e);
        } catch (IOException e) {
            Toast.makeText(this, "Problem with IO " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("micropub", "Error while copying image", e);
        }
    }

}
