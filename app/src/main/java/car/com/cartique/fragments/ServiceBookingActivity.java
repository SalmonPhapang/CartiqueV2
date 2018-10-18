package car.com.cartique.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import car.com.cartique.R;
import car.com.cartique.adapter.FeedListAdapter;
import car.com.cartique.model.Client;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServiceBookingActivity extends Fragment {

    View v;
    Activity activity;
    LinearLayoutManager layoutManager;
    com.bhargavms.dotloader.DotLoader bar;
    private RecyclerView recyclerView;
    private FeedListAdapter listAdapter;
    private List<Client> clientList = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = getActivity();
        FirebaseApp.initializeApp(getContext());
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = inflater.inflate(R.layout.fragment_blank, container, false);

        recyclerView = v.findViewById(R.id.list);

        listAdapter = new FeedListAdapter(getActivity(), clientList);
        recyclerView.setAdapter(listAdapter);
        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        bar = v.findViewById(R.id.prgload);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Clients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String,Client>> genericTypeIndicator = new GenericTypeIndicator<Map<String,Client>>() {
                };
                Map<String,Client> clients = dataSnapshot.getValue(genericTypeIndicator);

                for (Client client :   clients.values()) {
                    Client item = new Client();
                    item.setName(client.getName());
                    item.setAddress(client.getAddress());
                    item.setCity(client.getCity());
                    item.setSuburb(client.getSuburb());
                    item.setImageUrl(client.getImageUrl());
                    item.setLatitude(client.getLatitude());
                    item.setLongitude(client.getLongitude());
                    item.setBio(client.getBio());
                    item.setUserID(client.getUserID());
                    clientList.add(item);
                }
                listAdapter.notifyDataSetChanged();
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        return v;
    }
}
