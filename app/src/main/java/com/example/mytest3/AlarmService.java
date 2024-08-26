package com.example.mytest3;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Objects;

public class AlarmService extends Service {
    private static final String TAG = AlarmService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Thread thr = new Thread(null, mTask, "MyAlarmServiceThread");
        thr.start();
        Log.v(TAG,"スレッド開始");
    }

    // アラーム用サービス
    Runnable mTask = new Runnable() {
        @SuppressLint("ForegroundServiceType")
        public void run() {
            Notification notification = get_notification();
            startForeground(1,notification);
            // アラームを受け取るActivityを指定
            Intent alarmBroadcast = new Intent("Alarm");
            // ここでActionをセットする(Manifestに書いたものと同じであれば何でもよい)
            alarmBroadcast.setPackage("com.example.mytest3");
            // レシーバーへ渡す
            getBaseContext().sendBroadcast(alarmBroadcast);
            // 役目を終えたサービスを止める
            AlarmService.this.stopSelf();
            Log.v(TAG,"サービス停止");
        }
    };

    private Notification get_notification() {
        String Title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {        // ・・・(1)
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel                              // ・・・(2)
                    = new NotificationChannel("CHANNEL_ID", "サンプルアプリ", importance);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Title = "アラーム！！";

            return new Notification.Builder(this, "CHANNEL_ID")
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle(Title)
                    .setContentText("この通知をタップしてアラームを停止できます")
                    .setContentIntent(getPendingIntent())
                    .setDeleteIntent(getPendingIntent())
                    .build();
        }
        return null;
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(),NotificationBroadcastReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(),0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}