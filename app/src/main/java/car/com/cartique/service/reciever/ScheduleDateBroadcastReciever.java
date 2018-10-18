package car.com.cartique.service.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import car.com.cartique.service.ScheduleDateBackgroundService;

public class ScheduleDateBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent= new Intent(context,ScheduleDateBackgroundService.class);
        context.startService(serviceIntent);

    }
}