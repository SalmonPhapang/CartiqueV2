package car.com.cartique.about;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bhargavms.dotloader.DotLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import car.com.cartique.R;
import car.com.cartique.app.Config;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private Toolbar toolbar;
    private DotLoader bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_activity);
        FirebaseApp.initializeApp(getApplicationContext());
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_privacy);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        bar = findViewById(R.id.prgload);

        final WebView wv = (WebView) findViewById(R.id.privacy_web_view);
        wv.setWebViewClient(new WebViewClient());

        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference storageRef = storage.getReferenceFromUrl(Config.firebaseStrorage+"privacy and Terms").child("privacy policy.html");
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String htmlString = new String(bytes);
                wv.loadData(htmlString, "text/html", null);
                bar.setVisibility(View.GONE);
            }
        });

    }
}
