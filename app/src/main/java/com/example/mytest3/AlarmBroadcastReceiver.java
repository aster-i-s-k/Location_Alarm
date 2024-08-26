package com.example.mytest3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("AlarmBroadcastReceived");

        Intent notification = new Intent();
        notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notification.setClassName("com.example.mytest3","com.example.mytest3.AlarmNotificationActivity");
        context.startActivity(notification);
    }
}
