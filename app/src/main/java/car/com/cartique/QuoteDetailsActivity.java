package car.com.cartique;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import car.com.cartique.adapter.QuoteDetailsAdapter;
import car.com.cartique.model.Order;

public class QuoteDetailsActivity extends AppCompatActivity {
    private TextView txtClientName;
    private TextView txtOrderType;
    private TextView txtOrderStatus;
    private TextView txtOrderDate;
    private TextView txtOrderNumber;
    private TextView txtOrderAmount;
    private TextView txtMake;
    private TextView txtModel;
    private TextView txtYear;
    private TextView txtColor;
    private RecyclerView quoteRecyclerView;
    private QuoteDetailsAdapter quoteDetailsAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_details);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_quotes_details);
        Order order =(Order) getIntent().getSerializableExtra("Order");
        txtClientName = findViewById(R.id.txtUserName);
        txtOrderNumber = findViewById(R.id.txtOrderNumber);
        txtOrderType = findViewById(R.id.txtOrderType);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtOrderAmount = findViewById(R.id.txtOrderAmount);
        txtMake = findViewById(R.id.txtCarMake);
        txtModel = findViewById(R.id.txtCarModel);
        txtYear = findViewById(R.id.txtCarYear);
        txtColor = findViewById(R.id.txtCarColor);

        txtClientName.setText(order.getClientName());
        txtOrderNumber.setText(order.getOrderNumber());
        txtOrderType.setText(order.getOrderType().toString());
        txtOrderDate.setText(order.getOrderDate().toString());
        txtOrderStatus.setText(order.getOrderStatus().toString());
        if (order.getAmount()==null || order.getAmount().isEmpty()){
            txtOrderAmount.setText("Not Quoted");
        } else{
            txtOrderAmount.setText(order.getAmount());
        }
        txtModel.setText(order.getCar().getModel());
        txtMake.setText(order.getCar().getMake());
        txtYear.setText(order.getCar().getYear());
        txtColor.setText(order.getCar().getColor());

        quoteRecyclerView = findViewById(R.id.quoteList);
        quoteDetailsAdapter = new QuoteDetailsAdapter(this,order.getQuotes(),order);
        quoteRecyclerView.setAdapter(quoteDetailsAdapter);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        quoteRecyclerView.setLayoutManager(linearLayoutManager);
        quoteRecyclerView.setHasFixedSize(true);
        quoteRecyclerView.setItemViewCacheSize(20);
        quoteRecyclerView.setDrawingCacheEnabled(true);
        if(order.getQuotes() == null || order.getQuotes().size()==0){
            TextView txtHide = findViewById(R.id.lblNodata);
            txtHide.setVisibility(View.VISIBLE);
        }else
            quoteDetailsAdapter.notifyDataSetChanged();






    }
}
