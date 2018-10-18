package car.com.cartique;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bhargavms.dotloader.DotLoader;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import car.com.cartique.adapter.FeedListAdapter;
import car.com.cartique.adapter.RecordsListAdapter;
import car.com.cartique.model.Client;
import car.com.cartique.model.Order;
import car.com.cartique.model.OrderType;

public class RecordOrderActivity extends AppCompatActivity {
private RecyclerView recyclerView;
private RecordsListAdapter listAdapter;
private LinearLayoutManager linearLayoutManager;
private RelativeLayout layoutNoData;
private RelativeLayout layoutWithData;
private List<Order> ordersList = new ArrayList<>();
private DatabaseReference databaseReference;
private DotLoader bar;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_records);
        recyclerView = findViewById(R.id.recordsList);
        listAdapter = new RecordsListAdapter(this, ordersList);
        recyclerView.setAdapter(listAdapter);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        layoutNoData = findViewById(R.id.layoutNoData);
        layoutWithData = findViewById(R.id.layoutWithData);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        bar = findViewById(R.id.prgload);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Orders").orderByChild("userID").startAt(auth.getCurrentUser().getUid()).endAt(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Order>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Order>>() {
                };
                Map<String, Order> orders = dataSnapshot.getValue(genericTypeIndicator);

                if (orders != null) {
                   /* for (Order order : orders.values()) {
                        ordersList.add(order);
                    }*/
                    for (String key:orders.keySet()){
                        Order newOrder = orders.get(key);
                        if(newOrder.getOrderType() == OrderType.SERVICE) {
                            newOrder.setOrderID(key);
                            ordersList.add(newOrder);
                        }
                    }
                    layoutNoData.setVisibility(View.GONE);
                    listAdapter.notifyDataSetChanged();
                    bar.setVisibility(View.GONE);
                }else{
                    bar.setVisibility(View.GONE);
                    layoutWithData.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
