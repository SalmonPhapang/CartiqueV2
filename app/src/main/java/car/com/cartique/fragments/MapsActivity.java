package car.com.cartique.fragments;

/**
 * Created by napster on 1/18/18.
 */

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.toolbox.ImageLoader;
import car.com.cartique.R;
import car.com.cartique.app.AppController;
import car.com.cartique.app.CircularNetworkImageView;
import car.com.cartique.model.Client;
import car.com.cartique.model.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class MapsActivity extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private GoogleMap mMap;
    private ArrayList<Location> data = new ArrayList<>();
    private HashMap<String, String> infoImages = new HashMap<>();
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.maps_activity, container, false);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //asycTask for getting map data

        if (supportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            supportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, supportMapFragment).commit();
        } else {
            supportMapFragment.getMapAsync(this);
        }
        return v;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add a marker in Sydney and move the camera
        mMap = googleMap;


        LatLng sydney = new LatLng(-34, 151);

        googleMap.getUiSettings().setAllGesturesEnabled(true);
        FirebaseApp.initializeApp(getContext());
        try {

            permissionStatus = getActivity().getSharedPreferences("permissionStatus", MODE_PRIVATE);
            databaseReference = FirebaseDatabase.getInstance().getReference();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            } else {
                databaseReference.child("Clients").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<Map<String, Client>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Client>>() {
                        };
                        Map<String, Client> clients = dataSnapshot.getValue(genericTypeIndicator);

                            for (Client client : clients.values()) {
                                Location local = new Location();
                                local.setName(client.getName());
                                local.setAddress(client.getAddress());
                                local.setCity(client.getCity());
                                local.setSuburb(client.getSuburb());
                                local.setLatitude(client.getLatitude());
                                local.setLongitude(client.getLongitude());
                                data.add(local);
                                infoImages.put(local.getName(), client.getImageUrl());
                            }
                        for (Location location : data) {
                            latLngs.add(new LatLng(Double.parseDouble(location.getLongitude()), Double.parseDouble(location.getLatitude())));
                        }

                        for (int i = 0; i < latLngs.size(); i++) {
                            mMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title(data.get(i).getName()).snippet(data.get(i).getAddress()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngs.get(0)));

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLngs.get(0)).zoom(15.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        mMap.moveCamera(cameraUpdate);

                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions();
                            mMap.setMyLocationEnabled(true);
                            mMap.setOnMyLocationButtonClickListener(MapsActivity.this);
                            mMap.setOnMyLocationClickListener(MapsActivity.this);
                        } else {
                            mMap.setMyLocationEnabled(true);
                            mMap.setOnMyLocationButtonClickListener(MapsActivity.this);
                            mMap.setOnMyLocationClickListener(MapsActivity.this);
                        }

                        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(infoImages));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMyLocationClick(@NonNull android.location.Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "Retrieving Location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void requestPermissions() {


        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Show Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else if (permissionStatus.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            //Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sentToSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    Toast.makeText(getActivity().getApplicationContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
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
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
        }

        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true);
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
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Need Storage Permission");
                    builder.setMessage("This app needs storage permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);

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
                    Toast.makeText(getActivity().getApplicationContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private void proceedAfterPermission() {
        //We've got the permission, now we can proceed further
        Toast.makeText(getActivity().getBaseContext(), "We got the Storage Permission", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap == null) {

        }
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        private HashMap<String, String> infoImages;

        MyInfoWindowAdapter(HashMap<String, String> infoImages) {
            this.infoImages = infoImages;
            myContentsView = getActivity().getLayoutInflater().inflate(R.layout.custom_maps_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = myContentsView.findViewById(R.id.txtmapname);
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = myContentsView.findViewById(R.id.txtmapaddress);
            tvSnippet.setText(marker.getSnippet());
            CircularNetworkImageView imageView = myContentsView.findViewById(R.id.map_profile_image);
            imageView.setImageUrl(infoImages.get(marker.getTitle()).toString(), imageLoader);
            return myContentsView;
        }
    }
}

