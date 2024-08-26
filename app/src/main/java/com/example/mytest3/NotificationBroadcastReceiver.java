package com.example.mytest3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("NotificationBroadcastReceived");
        Intent notification = new Intent();
        notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notification.setClassName("com.example.mytest3","com.example.mytest3.AlarmNotificationActivity");
        context.startActivity(notification);
    }
}
