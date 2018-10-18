package car.com.cartique;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.transferwise.sequencelayout.SequenceLayout;
import com.transferwise.sequencelayout.SequenceStep;

import car.com.cartique.model.Order;
import car.com.cartique.model.OrderStatus;

public class OrderStatusActivity extends AppCompatActivity {
private TextView txtStatusClientName;
private TextView txtStatusOrderNumber;
private SequenceStep sequenceStep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_status_activity);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_order_status);
        txtStatusClientName = findViewById(R.id.txtstatusClientName);
        txtStatusOrderNumber = findViewById(R.id.txtstatusOrderNumber);
        sequenceStep = findViewById(R.id.statusDoneSequenceStep);

        Intent i = getIntent();
        Order order = (Order)i.getSerializableExtra("Order");
        txtStatusOrderNumber.setText(order.getOrderNumber());
        txtStatusClientName.setText(order.getClientName());

        if (order.getOrderStatus().toString().equalsIgnoreCase(OrderStatus.INITIATED.toString())){
            sequenceStep.setActive(true);
            sequenceStep.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        }
    }

}
