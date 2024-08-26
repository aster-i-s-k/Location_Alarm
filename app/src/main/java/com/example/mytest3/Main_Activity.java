package com.example.mytest3;

import static com.example.mytest3.Main_PagerAdapter.Home.adapter;
import static com.example.mytest3.Main_PagerAdapter.Locate.arrayAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.config.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Set;


public class Main_Activity extends AppCompatActivity{
    public static Drawable Icon;
    ViewPager2 viewpager;
    static Main_Activity return_context;
    static double lat;
    static double lon;
    AlarmSettingFragment dialogFragment;

    static int RequestCode;
    static int RequestCodeTime;
    static ArrayList<ArrayList<String>> Alarms = new ArrayList<>();
    //{{0is_on,1"Time",2time,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for},{~},...}
    //{{0is_on,1"Name",2name,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for,9staying_for},{~},...}
    //{{0is_on,1"Location",2location,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for,9staying_for,10within,11lat,12lon},{~},...}
    static ArrayList<Home_AlarmListItem> AlarmItems = new ArrayList<>();
    //{"Main","Description",is_sound,is_vibration,is_Popup,is_Event,is_recycle,is_on}
    static ArrayList<ArrayList<ArrayList<String>>> Events = new ArrayList<>();
    //{{{"app",packageName,className,AppName},{"brightness",progress},{"volume",progress}...},{}...}
    static ArrayList<String> FavoriteLocation = new ArrayList<>();
    static ArrayList<String> MaybeLocation = new ArrayList<>();
    static ArrayList<Integer> MaybeLocation_count = new ArrayList<>();

    //位置情報権限がない場合、権限取得
    private final ActivityResultLauncher<String>
       requestPermissionLauncher1 = registerForActivityResult(
       new ActivityResultContracts.RequestPermission(),
       isGranted -> {
           if (isGranted) {
               if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                   ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
               }
               locationStart();
               Perm_Notification();
           }
           else {
               Toast toast = Toast.makeText(this,"権限を許可してください", Toast.LENGTH_SHORT);
               toast.show();
           }
       });
    //SDK>=31&&アラームの設定権限がない場合、権限取得
    private final ActivityResultLauncher<String>
        requestPermissionLauncher2 = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
            isGranted ->{
                if (isGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Perm_AlarmSetting();
                    }else{init();}
                }
                else {
                    Toast toast = Toast.makeText(this,"権限を許可してください", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        return_context = this;
        Perm_DrawOverlays();
    }

    //<権限取得
    private void Perm_DrawOverlays(){
        if(!Settings.canDrawOverlays(this)){
            setContentView(R.layout.require_authority);
            TextView textView = findViewById(R.id.authorityName);
            textView.setText(R.string.RequireAuthority_value1);
            findViewById(R.id.require_authority).setOnClickListener(v->{
                Uri uri = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                startActivity(intent);
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                appOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, getPackageName(), new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        System.out.println("Callback1!");
                        appOpsManager.stopWatchingMode(this);
                        Intent intent1 = new Intent(getApplicationContext(), Main_Activity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                    }
                });
            });
        }else{
            Perm_WriteSetting();
        }
    }
    private void Perm_WriteSetting(){
        if(!Settings.System.canWrite(this)){
            setContentView(R.layout.require_authority);
            TextView textView = findViewById(R.id.authorityName);
            textView.setText(R.string.RequireAuthority_value2);
            findViewById(R.id.require_authority).setOnClickListener(v->{
                Uri uri = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri);
                startActivity(intent);
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                appOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, getPackageName(), new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        System.out.println("Callback3!");
                        appOpsManager.stopWatchingMode(this);
                        Intent intent2 = new Intent(getApplicationContext(), Main_Activity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        Perm_Location();
                    }
                });
            });
        }
        else{
            Perm_Location();
        }
    }
    @SuppressLint("MissingInflatedId")
    private void Perm_Location() {
        @SuppressLint("InlinedApi") String Permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ActivityCompat.checkSelfPermission(this, Permission) != PackageManager.PERMISSION_GRANTED) {
            setContentView(R.layout.require_authority);
            TextView textView = findViewById(R.id.authorityName);
            textView.setText(R.string.RequireAuthority_value3);
            findViewById(R.id.Location_moreExplain).setVisibility(View.VISIBLE);
            findViewById(R.id.require_authority).setOnClickListener(v->{
                findViewById(R.id.Location_moreExplain).setVisibility(View.GONE);
                requestPermissionLauncher1.launch(Permission);
            });
        } else {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
            }
            locationStart();
            Perm_Notification();
        }
    }
    private void Perm_Notification(){
        @SuppressLint("InlinedApi") String Permission = Manifest.permission.POST_NOTIFICATIONS;
        if (ActivityCompat.checkSelfPermission(this, Permission) != PackageManager.PERMISSION_GRANTED) {
            setContentView(R.layout.require_authority);
            TextView textView = findViewById(R.id.authorityName);
            textView.setText(R.string.RequireAuthority_value4);
            findViewById(R.id.require_authority).setOnClickListener(v-> requestPermissionLauncher2.launch(Permission));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Perm_AlarmSetting();
            }else {init();}
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void Perm_AlarmSetting(){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if(!alarmManager.canScheduleExactAlarms()){
            setContentView(R.layout.require_authority);
            TextView textView = findViewById(R.id.authorityName);
            textView.setText(R.string.RequireAuthority_value5);
            findViewById(R.id.require_authority).setOnClickListener(v->{
                Uri uri = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri);
                startActivity(intent);
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                appOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, getPackageName(), new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        appOpsManager.stopWatchingMode(this);
                        Intent intent1 = new Intent(getApplicationContext(), Main_Activity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        init();
                    }
                });
            });
        }else {
            init();
        }
    }
    //権限取得>

    private void init(){
        loadInformation();
        loadAlarmItems();

        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.nowpos,null);
        assert icon != null;
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
        Icon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 30, 30, true));

        setContentView(R.layout.main_activity);

        findViewById(R.id.Map).setOnClickListener(v -> viewpager.setCurrentItem(0,true));
        findViewById(R.id.Home).setOnClickListener(v -> viewpager.setCurrentItem(1,true));
        findViewById(R.id.Locate).setOnClickListener(v -> viewpager.setCurrentItem(2,true));

        viewpager = findViewById(R.id.ViewPager);
        viewpager.setAdapter(new Main_PagerAdapter(this));
        viewpager.setCurrentItem(1,false);
        viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewpager.setUserInputEnabled(position != 0);
            }
        });
    }

    //位置情報取得開始
    public void locationStart(){
        Intent intent = new Intent(this, LocationService.class);
        startForegroundService(intent);
    }
    @Override
    //位置情報取得終了
    protected void onDestroy() {
        // Serviceの停止
        int count=0;
        for(ArrayList<String>Alarm:Alarms){
            if(Objects.equals(Alarm.get(0), "true")){
                if(Objects.equals(Alarm.get(1), "Time")){
                    count++;
                }
            }else{
                count++;
            }
        }
        if(count>=Alarms.size()) {
            Intent intent = new Intent(getApplication(),LocationService.class);
            stopService(intent);
        }
        super.onDestroy();
    }

    //アラーム追加ボタン
    public void click_add(View view){
        if(getSupportFragmentManager().findFragmentByTag("dialog")==null) {
            dialogFragment = new AlarmSettingFragment();
            dialogFragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    //アラーム追加
    @SuppressLint("ScheduleExactAlarm")
    public void createAlarm(){
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
            RequestCodeTime=index;
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

            intent.putStringArrayListExtra("alarm",Alarms.get(RequestCodeTime));

            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null),
                    pending
            );

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("次回のアラーム：MM/dd HH:mm");
            Toast.makeText(getApplicationContext(),
                    sdf.format(calendar.getTime()), Toast.LENGTH_LONG).show();
        }else{RequestCodeTime=-1;}
        saveRequestCodeTime();
        System.out.println(RequestCodeTime);
    }

    //データの保存、読込、削除
    public void saveFavoriteLocation(){
        JSONArray array = new JSONArray();
        for (int i = 0, length = FavoriteLocation.size(); i < length; i++) {
            try {
                array.put(i, FavoriteLocation.get(i));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
        editor.putString("FavoriteLocation", array.toString()); //key名を"list"としてシリアライズ化したデータを保存
        editor.apply();
    }
    public void saveAlarmsEvents(){
        JSONArray array1 = getJsonArray();
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
    private static @NonNull JSONArray getJsonArray() {
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

    public void saveMaybeLocation_and_count(){
        JSONArray array1 = new JSONArray();
        for (int i = 0, length = MaybeLocation.size(); i < length; i++) {
            try {
                array1.put(i, MaybeLocation.get(i));
            } catch (JSONException e) {
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }
        }
        SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).edit();
        editor2.putString("MaybeLocation_count", array2.toString());
        editor2.apply();
    }
    private void saveRequestCodeTime(){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("shared_preference",Context.MODE_PRIVATE).edit();
        editor.putString("RequestCodeTime",String.valueOf(RequestCodeTime));
        editor.apply();
    }
    public void loadInformation(){
        Bundle bundle = new Bundle();  //保存用のバンドル
        Map<String, ?> prefKV = getApplicationContext().getSharedPreferences("shared_preference", Context.MODE_PRIVATE).getAll();
        Set<String> keys = prefKV.keySet();
        for(String key : keys){
            Object value = prefKV.get(key);
            if(value instanceof String){
                bundle.putString(key, (String) value);
            }
        }

        String stringList = bundle.getString("FavoriteLocation");  //key名が"FavoriteLocation"のものを取り出す
        if(stringList!=null) {
            ArrayList<String> FL_list = new ArrayList<>();
            try {
                JSONArray array1 = new JSONArray(stringList);
                for (int i = 0, length = array1.length(); i < length; i++) {
                    FL_list.add(array1.optString(i));
                }
            } catch (JSONException e1) {
                throw new RuntimeException(e1);
            }
            FavoriteLocation = FL_list;
        }

        String alarmsList = bundle.getString("Alarms");
        if(alarmsList!=null) {
            Alarms = getALists_2D(alarmsList);
        }

        String eventsList = bundle.getString("Events");
        if(eventsList!=null) {
            Events = getEList_3D(eventsList);
        }

        String MaybeLocateList = bundle.getString("MaybeLocation");
        if(MaybeLocateList!=null) {
            ArrayList<String> ML_list = new ArrayList<>();
            try {
                JSONArray array4 = new JSONArray(MaybeLocateList);
                for (int i = 0, length = array4.length(); i < length; i++) {
                    ML_list.add(array4.optString(i));
                }
            } catch (JSONException e1) {
                throw new RuntimeException(e1);
            }
            MaybeLocation = ML_list;
        }

        String MaybeLocate_countList = bundle.getString("MaybeLocation_count");
        if(MaybeLocate_countList!=null) {
            ArrayList<Integer> MLC_list = new ArrayList<>();
            try {
                JSONArray array5 = new JSONArray(MaybeLocate_countList);
                for (int i = 0, length = array5.length(); i < length; i++) {
                    MLC_list.add(array5.optInt(i));
                }
            } catch (JSONException e1) {
                throw new RuntimeException(e1);
            }
            MaybeLocation_count = MLC_list;
        }

        String requestCodeTime_String = bundle.getString("RequestCodeTime");
        if (requestCodeTime_String!=null) {
            RequestCodeTime = Integer.parseInt(requestCodeTime_String);
        }else {
            System.out.println("RequestCodeTime is null!!");
        }
    }

    private static @NonNull ArrayList<ArrayList<ArrayList<String>>> getEList_3D(String eventsList) {
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
    private static @NonNull ArrayList<ArrayList<String>> getALists_2D(String alarmsList) {
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

    public void deleteFavoriteLocation(int Pos){
        FavoriteLocation.remove(Pos);
        arrayAdapter.notifyDataSetChanged();
        saveFavoriteLocation();
    }
    public void deleteAlarmsEvents(int Pos){
        Alarms.remove(Pos);
        Events.remove(Pos);
        saveAlarmsEvents();
        loadAlarmItems();
        adapter.notifyDataSetChanged();
    }
    public void loadAlarmItems(){
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
        if(adapter!=null){
        adapter.notifyDataSetChanged();
        }
    }
}