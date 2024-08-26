package com.example.mytest3;

import static com.example.mytest3.Main_Activity.Alarms;
import static com.example.mytest3.Main_Activity.FavoriteLocation;
import static com.example.mytest3.Main_Activity.MaybeLocation;
import static com.example.mytest3.Main_Activity.MaybeLocation_count;
import static com.example.mytest3.Main_Activity.RequestCode;
import static com.example.mytest3.Main_Activity.lat;
import static com.example.mytest3.Main_Activity.lon;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocationService extends Service implements LocationListener {
    NotificationManager notificationManager;
    LocationManager locationManager;

    private ArrayList<String[]> Pos_INFO = new ArrayList<>();
    private ArrayList<Integer> Staying_Judging_AlarmPos = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.d("debug", "NotificationManager not available");
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            Log.d("debug", "LocationManager not available");
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notifyIntent = new Intent(this, Main_Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String NOTIFICATION_TITLE = "位置情報を取得しています";
        String CHANNEL_ID = "CHANNEL_ID";

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_TITLE, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(NOTIFICATION_TITLE)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setAutoCancel(false)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
        String ApiID="dj00aiZpPWRGNlpKbFhOZ3NGVyZzPWNvbnN1bWVyc2VjcmV0Jng9N2M-";
        new Main_PagerAdapter.Map().setPos();

        ExecutorService service = Executors.newFixedThreadPool(1);
        LocationCallable locationCallable = new LocationCallable(lat,lon,ApiID);
        Future<ArrayList<String[]>> pos_info_F = service.submit(locationCallable);
        try {
            ArrayList<String[]> pos_info_All = pos_info_F.get();
            String[] pos_info = pos_info_All.get(0);
            new Main_PagerAdapter.Home().change_text(pos_info[0],pos_info[1]);
            //Location Alarmの判定
            Pos_INFO = pos_info_All;
            registerMaybeLocation(pos_info_All);
            checkLocationAlarm(pos_info_All);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        service.shutdown();
    }
    private void checkLocationAlarm(ArrayList<String[]> Pos_All){
        for(int i=0;i<Alarms.size();i++){
            ArrayList<String> alarm = Alarms.get(i);
            if(Objects.equals(alarm.get(0), "true")){//is_On
                if(!Objects.equals(alarm.get(1), "Time")) {
                    int stayingFor = Integer.parseInt(alarm.get(9));
                    if (Objects.equals(alarm.get(1), "Name")) {//is_Name
                        String locationName = alarm.get(2);
                        for (String[] info : Pos_All) {
                            if (info[0].contains(locationName)) {
                                if (stayingFor == 0) {
                                    RequestCode = i;
                                    saveAlarmIsLocation(true);
                                    saveRequestCode(RequestCode);
                                    Intent notification = new Intent();
                                    notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    notification.setClassName("com.example.mytest3", "com.example.mytest3.AlarmNotificationActivity");
                                    getApplicationContext().startActivity(notification);
                                } else {
                                    if (!Staying_Judging_AlarmPos.contains(i)) {
                                        System.out.println("NameTimer!!");
                                        Timer timer = new Timer();
                                        timer.schedule(task1(info[0],i, Staying_Judging_AlarmPos.size()), stayingFor * 1000L);
                                        Staying_Judging_AlarmPos.add(i);
                                    }
                                }
                            }

                        }
                    } else if (Objects.equals(alarm.get(1), "Location")) {
                        int within = Integer.parseInt(alarm.get(10));
                        float[] results =new float[3];
                        Location.distanceBetween(lat,lon,Double.parseDouble(alarm.get(11)),Double.parseDouble(alarm.get(12)),results);
                        System.out.println(results[0]);
                        if(results[0]<within){
                            if(stayingFor==0) {
                                RequestCode = i;
                                saveAlarmIsLocation(true);
                                saveRequestCode(RequestCode);
                                Intent notification = new Intent();
                                notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                notification.setClassName("com.example.mytest3", "com.example.mytest3.AlarmNotificationActivity");
                                getApplicationContext().startActivity(notification);
                            }else{
                                if(!Staying_Judging_AlarmPos.contains(i)) {
                                    System.out.println("LocationTimer!!");
                                    Timer timer = new Timer();
                                    timer.schedule(task2(alarm, within, i, Staying_Judging_AlarmPos.size()), stayingFor * 1000L);
                                    Staying_Judging_AlarmPos.add(i);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private void registerMaybeLocation(ArrayList<String[]> Pos_All){
        for(String[] item:Pos_All){
            if(Integer.parseInt(item[1])>=70){
                if(FavoriteLocation.contains(item[0])) {
                    if (MaybeLocation.contains(item[0])) {
                        int pos = MaybeLocation.indexOf(item[0]);
                        MaybeLocation_count.set(pos, MaybeLocation_count.get(pos) + 1);
                    } else {
                        MaybeLocation.add(item[0]);
                        MaybeLocation_count.add(1);
                    }
                }
            }else{break;}
        }
        saveMaybeLocation_and_count();
    }

    private TimerTask task1(String pos, int requestCode, int removePos) {
        return new TimerTask() {
            @Override
            public void run() {
                Staying_Judging_AlarmPos.remove(removePos);
                for(String[] info:Pos_INFO) {
                    System.out.println(info[0].contains(pos));
                    if (info[0].contains(pos)) {
                        RequestCode = requestCode;
                        saveAlarmIsLocation(true);
                        saveRequestCode(RequestCode);
                        Intent notification = new Intent();
                        notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        notification.setClassName("com.example.mytest3", "com.example.mytest3.AlarmNotificationActivity");
                        getApplicationContext().startActivity(notification);
                    }
                }
            }
        };
    }
    private TimerTask task2(ArrayList<String> alarm,int within, int requestCode ,int removePos){
        return new TimerTask() {
            @Override
            public void run() {
                float[] results =new float[3];
                Location.distanceBetween(lat,lon,Double.parseDouble(alarm.get(11)),Double.parseDouble(alarm.get(12)),results);
                System.out.println(results[0]);
                Staying_Judging_AlarmPos.remove(removePos);
                if(within>=results[0]){
                    RequestCode=requestCode;
                    saveAlarmIsLocation(true);
                    saveRequestCode(RequestCode);
                    Intent notification = new Intent();
                    notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    notification.setClassName("com.example.mytest3", "com.example.mytest3.AlarmNotificationActivity");
                    getApplicationContext().startActivity(notification);
                }

            }
        };
    }

    private void saveMaybeLocation_and_count(){
        JSONArray array1 = new JSONArray();
        for (int i = 0, length = MaybeLocation.size(); i < length; i++) {
            try {
                array1.put(i, MaybeLocation.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor1 = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
        editor1.putString("MaybeLocation", array1.toString());
        editor1.apply();

        JSONArray array2 = new JSONArray();
        for (int i = 0, length = MaybeLocation_count.size(); i < length; i++) {
            try {
                array2.put(i, MaybeLocation_count.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
        editor2.putString("MaybeLocation_count", array2.toString());
        editor2.apply();
    }
    private void saveRequestCode(int requestCode){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("shared_preference",Context.MODE_PRIVATE).edit();
        editor.putString("RequestCode",String.valueOf(requestCode));
        editor.apply();
    }
    private void saveAlarmIsLocation(Boolean bool){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("shared_preference",Context.MODE_PRIVATE).edit();
        editor.putString("AlarmIsLocation",String.valueOf(bool));
        editor.apply();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
