package eu.stuifzand.micropub.eu.stuifzand.micropub.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import eu.stuifzand.micropub.R;
import okhttp3.HttpUrl;

public class AuthenticatedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticated);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String urlString = intent.getStringExtra("url");
        String endpoint = intent.getStringExtra(WebsigninTask.ENDPOINT);
        String me = intent.getStringExtra(WebsigninTask.ME);

        TextView textResult = findViewById(R.id.textResult);

        HttpUrl url = HttpUrl.parse(urlString);
        String code = url.queryParameter("code");
        String state = url.queryParameter("state");

      //  new VerifyAuthenticationTask(this).execute(endpoint, me, code);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
