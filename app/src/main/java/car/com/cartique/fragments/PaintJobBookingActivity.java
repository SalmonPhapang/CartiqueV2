package car.com.cartique.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import car.com.cartique.R;
import car.com.cartique.ResultsActivity;
import car.com.cartique.ServiceBookDetails;
import car.com.cartique.app.Config;
import car.com.cartique.model.Car;
import car.com.cartique.model.Order;
import car.com.cartique.model.OrderStatus;
import car.com.cartique.model.OrderType;
import car.com.cartique.model.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class PaintJobBookingActivity extends Fragment {

    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static int RESULT_LOAD_IMG = 1;
    EditText txtMake;
    EditText txtModel;
    EditText txtYear;
    EditText txtColor;
    TextView txtFilesChosenText;
    AppCompatButton btnChoose;
    AppCompatButton btnUpload;
    ByteArrayInputStream imagestream;
    String action;
    String imgDecodableString;
    View v;
    Activity activity;
    FirebaseStorage firebaseStorageReference;
    FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    ArrayList<Uri> chosenImages= new ArrayList<>();
    ArrayList<String> uploadedImages = new ArrayList<>();
    Switch profileSwitch;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    int countUploaded = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = getActivity();
        FirebaseApp.initializeApp(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = inflater.inflate(R.layout.pain_job_booking, container, false);
        firebaseStorageReference = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        permissionStatus = activity.getSharedPreferences("permissionStatus", MODE_PRIVATE);

        txtMake = v.findViewById(R.id.txtPaintMake);
        txtModel = v.findViewById(R.id.txtPaintModel);
        txtYear = v.findViewById(R.id.txtPaintYear);
        txtColor = v.findViewById(R.id.txtPaintColor);

        txtFilesChosenText = v.findViewById(R.id.txtFilesChosen);
        btnChoose = v.findViewById(R.id.btnPaintUpload);
        btnUpload = v.findViewById(R.id.btnGetQuotes);
        profileSwitch = v.findViewById(R.id.rdbProfile);


        mDatabase = FirebaseDatabase.getInstance().getReference("Orders");
        profileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    SharedPreferences pref =   getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                    String userObj = pref.getString(Config.USER_OBJECT,"");
                    Gson gson = new Gson();
                    User user = gson.fromJson(userObj, User.class);
                    txtMake.setText(user.getCar().getMake());
                    txtModel.setText(user.getCar().getModel());
                    txtYear.setText(user.getCar().getYear());
                    txtColor.setText(user.getCar().getColor());
                } else {
                    txtMake.setText("");
                    txtModel.setText("");
                    txtYear.setText("");
                    txtColor.setText("");
                }
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions();
                } else {
                    // Undocumented way to get multiple photo selections from Android Gallery ( on Samsung )
                    Intent intent = new Intent("android.intent.action.MULTIPLE_PICK");//("Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    // Check to see if it can be handled...
                    PackageManager manager = getActivity().getApplicationContext().getPackageManager();
                    List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
                    if (infos.size() > 0) {
                        // Ok, "android.intent.action.MULTIPLE_PICK" can be handled
                        action = "android.intent.action.MULTIPLE_PICK";
                    } else {
                        action = Intent.ACTION_GET_CONTENT;
         /* This is the documented way you are to get multiple images from a gallery BUT IT DOES NOT WORK with Android Gallery! (at least on Samsung )
           But the Android Email client WORKS! What the f'k!
               */
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Note: only supported after Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT, harmless if used below 19, but no mutliple selection supported
                    }
                    startActivityForResult(intent, RESULT_LOAD_IMG);
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        return v;
    }

    //this method will upload the file
    private void uploadFile() {
        //if there is a file to upload
        if (chosenImages != null && chosenImages.size() > 0) {
            for (int i = 0; i < chosenImages.size(); i++) {
                String uniqueID = UUID.randomUUID().toString();
                final String imageName = "/" + auth.getCurrentUser().getEmail() + "/images/PaintJob/UserPaintUpload" + uniqueID + ".jpg";
                uploadedImages.add(imageName);
                final StorageReference riversRef = firebaseStorageReference.getReferenceFromUrl("gs://cartique-1516308965713.appspot.com").child(imageName);
                riversRef.putFile(chosenImages.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //and displaying error message
                        Toast.makeText(activity.getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            savePaintOrder();
        }
        //if there is not any file
        else {
            Toast.makeText(activity.getApplicationContext(), "No images selected please try again..", Toast.LENGTH_LONG).show();
        }
    }

    public void savePaintOrder(){
        String make = txtMake.getText().toString();
        String model = txtModel.getText().toString();
        String color = txtColor.getText().toString();
        String year = txtYear.getText().toString();
        if (make.isEmpty() ) {
            txtMake.setError(getString(R.string.service_make_error));
        } else if (model.isEmpty()) {
            txtModel.setError(getString(R.string.service_make_error));
        } else if (color.isEmpty() ) {
            txtColor.setError(getString(R.string.service_make_error));
        } else if (year.isEmpty()) {
            txtYear.setError(getString(R.string.service_make_error));
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("Sending out request");
            progressDialog.setMessage("We are placing your Quote request");
            progressDialog.show();

            Order order = new Order();
            Car car = new Car();
            car.setMake(make);
            car.setModel(model);
            car.setColor(color);
            car.setYear(year);
            order.setCar(car);
            order.setOrderDate(new Date());
            order.setOrderType(OrderType.PAINT);
            order.setOrderStatus(OrderStatus.INITIATED);
            order.setUserName(auth.getCurrentUser().getEmail());
            order.setUserID(auth.getCurrentUser().getUid());
            order.setClientName("Quotations");
            order.setOrderNumber("Cart"+order.getOrderDate().getYear()
                    +order.getOrderDate().getMonth()
                    +order.getOrderDate().getDate()
                    +order.getOrderDate().getHours()
                    +order.getOrderDate().getMinutes()
                    +order.getOrderDate().getSeconds()+"P"+auth.getCurrentUser().getUid().substring(0,4));
            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
            order.setUserNotificationToken( pref.getString("regId",""));
            //set images by name,Client app will fetch images by name
            order.setUploadedImages(uploadedImages);
            String userId = mDatabase.push().getKey();
            mDatabase.child(userId).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Intent resultIntent = new Intent(getActivity().getApplicationContext(),ResultsActivity.class);
                    startActivity(resultIntent);
                    getActivity().finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity().getApplicationContext(),"Problem with sending request, Please try again later",Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (resultCode == -1 && requestCode == 1 && data != null) {

                if(action.equals("android.intent.action.MULTIPLE_PICK")){
                    final Bundle extras = data.getExtras();
                    int count = extras.getInt("selectedCount");
                    ArrayList<String> items = extras.getStringArrayList("selectedItems");
                    // do somthing
                    txtFilesChosenText.setText("Selected number of images "+count);
                    for (String item:items) {
                        chosenImages.add(Uri.parse(item));
                    }
                }else {
                    if (data != null && data.getData() != null) {
                        Uri uri = data.getData();
                        txtFilesChosenText.setText("Selected number of images 1");
                        chosenImages.add(uri);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ClipData clipData = data.getClipData();
                            if (clipData != null) {
                                ArrayList<Uri> uris = new ArrayList<>();
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    ClipData.Item item = clipData.getItemAt(i);
                                    Uri uri = item.getUri();
                                    uris.add(uri);
                                }
                                // Do someting
                                System.out.println("uris ..................."+uris);
                                txtFilesChosenText.setText("Selected number of images "+clipData.getItemCount());
                                chosenImages.addAll(uris);
                            }
                        }
                    }
                }
                super.onActivityResult(requestCode, resultCode, data);
            } else if (requestCode == REQUEST_PERMISSION_SETTING) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    proceedAfterPermission();
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            Toast.makeText(activity, "Something went wrong " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }

    }

    private void proceedAfterPermission() {
        //We've got the permission, now we can proceed further
        Toast.makeText(activity.getBaseContext(), "We got the Storage Permission", Toast.LENGTH_LONG).show();
    }

    private void requestPermissions() {


        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //Show Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else if (permissionStatus.getBoolean(Manifest.permission.READ_EXTERNAL_STORAGE, false)) {
            //Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sentToSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    Toast.makeText(activity.getApplicationContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            //just request the permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
        }

        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(Manifest.permission.READ_EXTERNAL_STORAGE, true);
        editor.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                proceedAfterPermission();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Need Storage Permission");
                    builder.setMessage("This app needs storage permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(activity.getApplicationContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

}