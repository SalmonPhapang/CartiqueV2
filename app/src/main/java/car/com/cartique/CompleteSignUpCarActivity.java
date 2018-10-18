package car.com.cartique;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import car.com.cartique.app.Config;
import car.com.cartique.model.Car;
import car.com.cartique.model.User;

public class CompleteSignUpCarActivity extends AppCompatActivity {
    private EditText txtMake;
    private EditText txtModel;
    private EditText txtYear;
    private EditText txtColor;
    AppCompatButton btnNext;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_registration_car_activity);
        Intent intent = getIntent();
        final User user = (User)intent.getSerializableExtra(Config.SIGNUP_USER);

        txtMake = (EditText)findViewById(R.id.txtRegistrationCarMake);
        txtModel = (EditText)findViewById(R.id.txtRegistrationCarModel);
        txtColor = (EditText)findViewById(R.id.txtRegistrationCarColor);
        txtYear = (EditText)findViewById(R.id.txtRegistrationCarYear);
        btnNext = (AppCompatButton)findViewById(R.id.btnSubmitNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(txtMake.getText().toString())) {
                    txtMake.setError(getString(R.string.service_make_error));
                }else if (TextUtils.isEmpty(txtModel.getText().toString())) {
                    txtModel.setError(getString(R.string.service_model_error));
                }else if (TextUtils.isEmpty(txtColor.getText().toString())) {
                    txtColor.setError(getString(R.string.service_color_error));
                }else if (TextUtils.isEmpty(txtYear.getText().toString())) {
                    txtYear.setError(getString(R.string.service_year_error));
                }else{
                    final ProgressDialog progressDialog = new ProgressDialog(CompleteSignUpCarActivity.this);
                    progressDialog.setTitle("Creating Account");
                    progressDialog.setMessage("We are just saving you profile..relax");
                    progressDialog.show();
                    //FireBase initialization
                    FirebaseApp.initializeApp(getApplicationContext());
                    auth = FirebaseAuth.getInstance();
                    mDatabase = FirebaseDatabase.getInstance().getReference("Users");
                    Car car = new Car();
                    car.setMake(txtMake.getText().toString());
                    car.setModel(txtModel.getText().toString());
                    car.setYear(txtYear.getText().toString());
                    car.setColor(txtColor.getText().toString());
                    user.setCar(car);
                    user.setEmail(auth.getCurrentUser().getEmail());
                    user.setUserID(auth.getCurrentUser().getUid());

                    String userId = mDatabase.push().getKey();
                    mDatabase.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Gson gson = new Gson();
                            String userString = gson.toJson(user);
                            storeUserObjInPref(userString);
                            Intent resultIntent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(resultIntent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Problem with saving your data, Please try again later",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
    private void storeUserObjInPref(String user) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Config.USER_OBJECT,user);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.removeExtra(Config.SIGNUP_USER);
        super.onBackPressed();
    }
}
