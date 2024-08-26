package com.example.mytest3;

import static com.example.mytest3.Main_Activity.AlarmItems;
import static com.example.mytest3.Main_Activity.Alarms;
import static com.example.mytest3.Main_Activity.Events;
import static com.example.mytest3.Main_Activity.RequestCode;
import static com.example.mytest3.Main_Activity.RequestCodeTime;
import static com.example.mytest3.Main_Activity.return_context;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mytest3.databinding.RingingAlarmBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;
import java.util.Set;

public class AlarmNotificationActivity extends Activity {
    private static MediaPlayer mediaplayer;
    private static Vibrator vibrator;
    private static int RequestCodeTime_send;
    private static Boolean isLocationAlarm;
    private static View AlarmView;
    private static WindowManager wm;
    private static Boolean isLocked=false;
    private static Boolean bool;


    //データの取得
    public void loadInformation(){
        System.out.println("ANActivity:loading!");
        Bundle bundle = new Bundle();  //保存用のバンドル
        Map<String,?> prefKV = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).getAll();
        Set<String> keys = prefKV.keySet();
        for(String key : keys){
            Object value = prefKV.get(key);
            if(value instanceof String){
                bundle.putString(key, (String) value);
            }
        }

        String alarmsList = bundle.getString("Alarms");
        if(alarmsList!=null) {
            Alarms = getA_list2D(alarmsList);
        }

        String eventsList = bundle.getString("Events");
        if(eventsList!=null) {
            Events = getE_list3D(eventsList);
        }

        String requestCode_String = bundle.getString("RequestCode");
        if(requestCode_String!=null){
            RequestCode = Integer.parseInt(requestCode_String);
        }

        String requestCodeTime_String = bundle.getString("RequestCodeTime");
        if (requestCodeTime_String!=null) {
            RequestCodeTime = Integer.parseInt(requestCodeTime_String);
        }

        String isLocationAlarm_String = bundle.getString("AlarmIsLocation");
        if(isLocationAlarm_String!=null){
            isLocationAlarm=Boolean.parseBoolean(isLocationAlarm_String);
        }else{
            isLocationAlarm=false;
        }
    }
    private static @NonNull ArrayList<ArrayList<String>> getA_list2D(String alarmsList) {
        ArrayList<ArrayList<String>> A_list2D = new ArrayList<>();
        try {
            JSONArray array2 = new JSONArray(alarmsList);
            for (int i = 0, length = array2.length(); i < length; i++) {
                JSONArray array22 = array2.optJSONArray(i);
                ArrayList<String> A_List1D = new ArrayList<>();
                for (int j = 0, len = array22.length(); j < len; j++) {
                    A_List1D.add(array22.optString(j));
                }
                A_list2D.add(A_List1D);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return A_list2D;
    }
    private static @NonNull ArrayList<ArrayList<ArrayList<String>>> getE_list3D(String eventsList) {
        ArrayList<ArrayList<ArrayList<String>>> E_list3D = new ArrayList<>();
        try {
            JSONArray array3 = new JSONArray(eventsList);
            for (int i = 0, length = array3.length(); i < length; i++) {
                JSONArray array33 = array3.optJSONArray(i);
                ArrayList<ArrayList<String>> E_List2D = new ArrayList<>();
                for (int j = 0, len = array33.length(); j < len; j++) {
                    JSONArray array333 = array33.optJSONArray(j);
                    ArrayList<String> E_List1D = new ArrayList<>();
                    for (int k = 0, l = array333.length(); k < l; k++) {
                        E_List1D.add(array333.optString(k));
                    }
                    E_List2D.add(E_List1D);
                }
                E_list3D.add(E_List2D);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return E_list3D;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("ANActivity:created!!");
        loadInformation();

        if(!Alarms.isEmpty()) {
            //{{0is_on,1"Time",2time,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for},{~},...}
            //{{0is_on,1"Name",2name,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for,9staying_for},{~},...}
            //{{0is_on,1"Location",2location,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for,9staying_for,10within,11lat,12lon},{~},...}
            ArrayList<String> Alarm;
            if(isLocationAlarm) {
                if(RequestCode==-1){
                    stop_Alarm();
                    return;
                }
                else {
                    Alarm = Alarms.get(RequestCode);
                    saveAlarmIsLocation();
                }
            }else {
                if(RequestCodeTime==-1){
                    stop_Alarm();
                    return;
                }
                else {
                    Alarm = Alarms.get(RequestCodeTime);
                }
            }
            if (Objects.equals(Alarm.get(0), "true")) {//is_on is true
                if (!Objects.equals(Alarm.get(3), "true")) {delete_Alarm();}
                else if (!Objects.equals(Alarm.get(1), "Time")) {delete_Alarm();}
                if (Objects.equals(Alarm.get(4), "true")) {play_sound();}
                if (Objects.equals(Alarm.get(5), "true")) {play_vibration();}
                if (Objects.equals(Alarm.get(7), "true")) {play_event();}
                if (Objects.equals(Alarm.get(6), "true")) {show_popup(Alarm);} else {show_notification(Alarm);}
                if (!Objects.equals(Alarm.get(8), "0"))   {delayed_stop_Alarm(Integer.parseInt(Alarm.get(8)));}
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );

            RequestCodeTime=-1;

            createAlarm();
            saveAlarmsEvents();
            loadAlarmItems();
        }
        else{stop_Alarm();}
    }

    private void delete_Alarm() {
        if(isLocationAlarm){
            Alarms.get(RequestCode).set(0,"false");
        }else{
            Alarms.get(RequestCodeTime).set(0,"false");
        }
        saveAlarmsEvents();
        loadAlarmItems();
        if(return_context!=null){
            return_context.loadInformation();
            return_context.loadAlarmItems();
        }

    }

    private void play_sound() {
        mediaplayer = new MediaPlayer();
        mediaplayer.setAudioAttributes(new AudioAttributes.Builder()
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setLegacyStreamType(AudioManager.STREAM_ALARM)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        mediaplayer.setLooping(true);
        try {
            mediaplayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            mediaplayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaplayer.start();
    }

    private void stop_sound() {
        if (mediaplayer != null) {
            mediaplayer.stop();
            mediaplayer.release();
        }else {
            System.out.println("mediaPlayer is not initialized");
        }
    }

    private void play_vibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            long[] wave = {1000, 1000, 1000, 1000, 500, 500};
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(wave, 0);
            vibrator.vibrate(vibrationEffect);
        } else {
            vibrator.vibrate(300);
        }
    }

    private void show_popup(ArrayList<String> Alarm) {
        RingingAlarmBinding binding = RingingAlarmBinding.inflate(getLayoutInflater());
        AlarmView = binding.getRoot();

        TextView Clock = AlarmView.findViewById(R.id.Ringing_Clock);
        Clock.setText(Alarm.get(2));
        AlarmView.findViewById(R.id.BackGround).setOnClickListener(v -> stop_Alarm());

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        );

        bool = true;
        ArrayList<ArrayList<String>> items;
        if(isLocationAlarm){
            items= Events.get(RequestCode);
        }else{
            items= Events.get(RequestCodeTime);
        }
        for(ArrayList<String> item:items){
            if (Objects.equals(item.get(0), "app")) {
                bool = false;
                break;
            }
        }

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        boolean isDeviceLock = keyguardManager.isDeviceLocked();
        isLocked=isDeviceLock;
        if(isDeviceLock && bool){
            setContentView(AlarmView,layoutParams);
        }else {
            wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            wm.addView(AlarmView, layoutParams);
        }
    }

    private void show_notification(ArrayList<String> Alarm) {
        String Title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {        // ・・・(1)
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel                              // ・・・(2)
                    = new NotificationChannel("CHANNEL_ID", "サンプルアプリ", importance);

            channel.setDescription("アラームをポップアップしない場合、ポップアップの代わりに通知が送信されます。");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            if(Objects.equals(Alarm.get(1), "Time")){
                Title=(Alarm.get(2)+"になりました");
            }
            else{
                Title=(Alarm.get(2)+"周辺です");
            }
            notifyTest(Title);
            this.finish();
        }
    }
    public void notifyTest(String Title) {
        @SuppressLint("NotificationTrampoline") NotificationCompat.Builder builder
                = new NotificationCompat.Builder(this, "CHANNEL_ID")     // ・・・(4)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(Title)
                .setContentText("この通知をタップしてアラームを停止できます")
                .setContentIntent(getPendingIntent())
                .setDeleteIntent(getPendingIntent())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager
                = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(R.string.app_name, builder.build());
        // ・・・(5)
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(),NotificationBroadcastReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(),0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void play_event(){
        System.out.println("play_event");
        ArrayList<ArrayList<String>> items;
        if(isLocationAlarm){
            items= Events.get(RequestCode);
        }else{
            items= Events.get(RequestCodeTime);
        }
        for(ArrayList<String> item : items){
            if(Objects.equals(item.get(0), "app")){
                try {
                    System.out.println("AppLaunched");
                    PackageManager packageManager = return_context.getPackageManager();
                    Intent notification = packageManager.getLaunchIntentForPackage(item.get(1));
                    assert notification != null;
                    notification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(notification);
                }catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }else if(Objects.equals(item.get(0), "brightness")){
                System.out.println("--change_brightness");
                BigDecimal bd1 = new BigDecimal(255*Integer.parseInt(item.get(1)));
                BigDecimal bd2 = new BigDecimal("10");
                BigDecimal result = bd1.divide(bd2,RoundingMode.DOWN);
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, result.intValue());
            }else if(Objects.equals(item.get(0), "volume")){
                System.out.println("--change_volume");
                AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                int MaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                BigDecimal bd1 = new BigDecimal(String.valueOf(MaxVolume*Integer.parseInt(item.get(1))));
                BigDecimal bd2 = new BigDecimal("10");
                BigDecimal result = bd1.divide(bd2,RoundingMode.DOWN);
                System.out.println("setVolume:"+result);

                am.setStreamVolume(AudioManager.STREAM_MUSIC,result.intValue(), AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    public void delayed_stop_Alarm(int SECOND){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stop_Alarm();
            }
        },SECOND* 1000L);
    }
    public void stop_Alarm(){
        stop_sound();
        if(vibrator!=null) {
            System.out.println(vibrator);
            vibrator.cancel();
        }
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancel(R.string.app_name);
        if(!(isLocked && bool)) {
            try {
                wm.removeView(AlarmView);
            } catch (Exception ignored) {
            }
        }
        this.finishAndRemoveTask();
    }

    //データの保存、更新
    private void createAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int hour;
        int minute;
        int index=-1;
        int min=2360;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            alarmManager.cancelAll();
        } else{
            Intent cancelintent = new Intent(this, AlarmBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, cancelintent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        LocalDateTime date1 = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HHmm");
        int now = Integer.parseInt(dateTimeFormatter.format(date1));

        for (int i=0;i<Alarms.size();i++) {
            if (Objects.equals(Alarms.get(i).get(0), "true") && Objects.equals(Alarms.get(i).get(1), "Time")) {
                String time1 = Alarms.get(i).get(2);
                int int_time=Integer.parseInt(time1.replace(":",""));
                if(now < int_time && int_time-now < min){
                    min=int_time-now;
                    index=i;
                }
            }
        }
        if(index==-1){
            for (int i=0;i<Alarms.size();i++) {
                if (Objects.equals(Alarms.get(i).get(0), "true") && Objects.equals(Alarms.get(i).get(1), "Time")) {
                    String time1 = Alarms.get(i).get(2);
                    int int_time=Integer.parseInt(time1.replace(":",""));
                    if(int_time-now < min){
                        index=i;
                    }
                }
            }
        }
        if(index!=-1) {
            RequestCodeTime_send=index;
            String time = Alarms.get(index).get(2);
            if (Objects.equals(String.valueOf(time.charAt(2)), ":")) {
                hour = Integer.parseInt(time.substring(0, 2));
                minute = Integer.parseInt(time.substring(3, 5));
            } else {
                hour = Integer.parseInt(time.substring(0, 1));
                minute = Integer.parseInt(time.substring(2, 4));
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            System.out.println(calendar.getTime());

            Context context = getBaseContext();
            Intent intent = new Intent(context, AlarmService.class);
            PendingIntent pending = PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null),
                    pending
            );

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("次回のアラーム：MM/dd HH:mm");
            Toast.makeText(getApplicationContext(),
                    sdf.format(calendar.getTime()), Toast.LENGTH_LONG).show();
        }else{RequestCodeTime_send=-1;}
        saveRequestCodeTime();
    }
    private void saveAlarmsEvents(){
        JSONArray array1 = getArray1();
        SharedPreferences.Editor editor1 = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
        editor1.putString("Alarms", array1.toString()); //key名を"list"としてシリアライズ化したデータを保存
        editor1.apply();

        JSONArray array2 = new JSONArray();
        for (int i = 0, length = Events.size(); i < length; i++) {
            try {
                JSONArray array22 = getArray22(i);
                array2.put(i,array22);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
        editor2.putString("Events", array2.toString()); //key名を"list"としてシリアライズ化したデータを保存
        editor2.apply();
    }
    private static @NonNull JSONArray getArray22(int i) throws JSONException {
        ArrayList<ArrayList<String>> Event = Events.get(i);
        JSONArray array22 = new JSONArray();
        for (int j = 0, len = Event.size();j<len;j++){
            ArrayList<String> Event_info = Event.get(j);
            JSONArray array222 = new JSONArray();
            for (int k = 0, l = Event_info.size(); k < l; k++){
                array222.put(k,Event_info.get(k));
            }
            array22.put(j,array222);
        }
        return array22;
    }
    private static @NonNull JSONArray getArray1() {
        JSONArray array1 = new JSONArray();
        for (int i = 0, length = Alarms.size(); i < length; i++) {
            try {
                ArrayList<String> Alarm = Alarms.get(i);
                JSONArray array11 = new JSONArray();
                for (int j = 0, len = Alarm.size(); j < len; j++){
                    array11.put(j,Alarm.get(j));
                }
                array1.put(i,array11);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return array1;
    }

    private void saveRequestCodeTime(){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("shared_preference",Context.MODE_PRIVATE).edit();
        editor.putString("RequestCodeTime",String.valueOf(RequestCodeTime_send));
        editor.apply();
    }
    private void saveAlarmIsLocation(){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("shared_preference",Context.MODE_PRIVATE).edit();
        editor.putString("AlarmIsLocation",String.valueOf((Boolean) false));
        editor.apply();
    }
    private void loadAlarmItems(){
        AlarmItems.clear();
        for(int i=0;i<Alarms.size(); i++){
            ArrayList<String> Alarm = Alarms.get(i);
            StringBuilder bf = new StringBuilder();
            bf.append(Alarm.get(1));
            if(Objects.equals(Alarm.get(7), "true")){
                bf.append("　イベント:");
                for(ArrayList<String>event : Events.get(i)){
                    bf.append(" ").append(event.get(0));
                    if(Objects.equals(event.get(0), "app")){
                        bf.append("-").append(event.get(3));
                    }else {
                        bf.append("-").append(event.get(1));
                    }
                }
            }
            if(!Objects.equals(Alarm.get(1), "Time")){
                bf.append("　滞在時間：").append(Alarm.get(9)).append("秒");
                if(Objects.equals(Alarm.get(1), "Location")){
                    bf.append("　判定：").append(Alarm.get(10)).append("m");
                }
            }
            Home_AlarmListItem item = new Home_AlarmListItem(Alarm.get(2),bf.toString(),Alarm.get(4),Alarm.get(5),Alarm.get(6),Alarm.get(7),Alarm.get(3),Alarm.get(0));
            AlarmItems.add(item);
        }
    }
}
