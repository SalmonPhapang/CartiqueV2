package car.com.cartique;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Date;

import car.com.cartique.R;
import car.com.cartique.app.AppController;
import car.com.cartique.app.CircularNetworkImageView;
import car.com.cartique.app.Config;
import car.com.cartique.model.Car;
import car.com.cartique.model.Order;
import car.com.cartique.model.OrderStatus;
import car.com.cartique.model.OrderType;
import car.com.cartique.model.User;

public class ServiceBookDetails extends AppCompatActivity {

    TextView txtDetailName;
    NetworkImageView imageView;
    AppCompatButton btnServiceBook;
    TextView txtServiceMake;
    TextView txtServiceModel;
    TextView txtServiceYear;
    TextView txtServiceColor;
    TextView txtServiceDescription;
    Switch rdbServiceProfile;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private String clientID;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_booking_form_activity);
        txtDetailName = findViewById(R.id.txtdetailName);
        imageView = findViewById(R.id.detailPic);
        btnServiceBook = findViewById(R.id.btnServiceBook);
        txtServiceMake = findViewById(R.id.txtServiceMake);
        txtServiceModel = findViewById(R.id.txtServiceModel);
        txtServiceYear = findViewById(R.id.txtServiceYear);
        txtServiceColor = findViewById(R.id.txtServiceColor);
        txtServiceDescription = findViewById(R.id.txtServiceDescription);
        rdbServiceProfile = findViewById(R.id.rdbServiceProfile);
        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference("Orders");
        Intent intent = getIntent();
         name = intent.getStringExtra("name");
        txtDetailName.setText(name);
        Picasso.with(getApplicationContext())
                .load(intent.getStringExtra("image"))
                .fit()
                .transform(new CircularNetworkImageView(getApplicationContext()))
                .into(imageView);
         clientID = intent.getStringExtra("clientID");
        rdbServiceProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                    String userObj = pref.getString(Config.USER_OBJECT,"");
                    Gson gson = new Gson();
                    User user = gson.fromJson(userObj, User.class);
                    txtServiceMake.setText(user.getCar().getMake());
                    txtServiceModel.setText(user.getCar().getModel());
                    txtServiceYear.setText(user.getCar().getYear());
                    txtServiceColor.setText(user.getCar().getColor());
                } else {
                    txtServiceMake.setText("");
                    txtServiceModel.setText("");
                    txtServiceYear.setText("");
                    txtServiceColor.setText("");
                }
            }
        });
        btnServiceBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String make = txtServiceMake.getText().toString();
                String model = txtServiceModel.getText().toString();
                String color = txtServiceColor.getText().toString();
                String year = txtServiceYear.getText().toString();
                String description = txtServiceDescription.getText().toString();
                if (make.isEmpty() ) {
                    txtServiceMake.setError(getString(R.string.service_make_error));
                } else if (model.isEmpty()) {
                    txtServiceModel.setError(getString(R.string.service_make_error));
                } else if (color.isEmpty() ) {
                    txtServiceColor.setError(getString(R.string.service_make_error));
                } else if (year.isEmpty()) {
                    txtServiceYear.setError(getString(R.string.service_make_error));
                } else if (description.isEmpty()) {
                    txtServiceDescription.setError(getString(R.string.service_decription_error));
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(ServiceBookDetails.this);
                    progressDialog.setTitle("Placing Booking");
                    progressDialog.setMessage("We are placing your booking");
                    progressDialog.show();

                    Order order = new Order();
                    Car car = new Car();
                    car.setMake(make);
                    car.setModel(model);
                    car.setColor(color);
                    car.setYear(year);
                    order.setCar(car);
                    order.setOrderDate(new Date());
                    order.setOrderType(OrderType.SERVICE);
                    order.setOrderStatus(OrderStatus.INITIATED);
                    order.setUserName(auth.getCurrentUser().getEmail());
                    order.setUserID(auth.getCurrentUser().getUid());
                    order.setClientID(clientID);
                    order.setClientName(name);
                    order.setOrderNumber("Cart"+order.getOrderDate().getYear()
                            +order.getOrderDate().getMonth()
                            +order.getOrderDate().getDate()
                            +order.getOrderDate().getHours()
                            +order.getOrderDate().getMinutes()
                            +order.getOrderDate().getSeconds()+"S"+auth.getCurrentUser().getUid().substring(0,4));
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                    order.setUserNotificationToken( pref.getString("regId",""));
                    String userId = mDatabase.push().getKey();
                    mDatabase.child(userId).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Intent resultIntent = new Intent(getApplicationContext(),ResultsActivity.class);
                            startActivity(resultIntent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Problem with sending request, Please try again later",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}
