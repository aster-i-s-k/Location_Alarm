package com.example.mytest3;

import static com.example.mytest3.Main_Activity.Alarms;
import static com.example.mytest3.Main_Activity.Events;
import static com.example.mytest3.Main_PagerAdapter.Home.adapter;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlarmSettingFragment extends DialogFragment {
    public static class Timepicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        String time;
        int picked_hour=12;
        int picked_minute=0;
        Main_Activity listener;
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new TimePickerDialog(requireContext(), this, 12, 0, true);
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            listener = (Main_Activity) getActivity();
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            picked_hour = hourOfDay;
            picked_minute = minute;
            time = String.format(Locale.US,"%d:%02d",hourOfDay,minute);
            listener.dialogFragment.onTimePicked(time);
        }
    }
    Dialog dialog;
    Timepicker timepicker;
    public static ArrayList<ASF_EventsListItem> events = new ArrayList<>();
    public static int position = 0;
    public static ASF_EventsListAdapter arrayAdapter;
    private static String locationName;
    private static String locationTAG;
    interface EventListDeleteClickedListener{
        void deleteEvent(int position,View view);
    }
    interface EventListAppPickClickedListener{
        void AppPick(int pos);
    }
    int waiting_for = 0;
    int staying_for = 0;
    int within = 500;

    private double Lat;
    private double Lon;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        dialog = new Dialog(requireActivity());
        // タイトル非表示
        Objects.requireNonNull(dialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.as_dialog);
        // 背景を透明にする
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        timepicker = new Timepicker();
        RadioGroup Time_or_Location=dialog.findViewById(R.id.Time_or_Location);
        RadioButton Time = dialog.findViewById(R.id.is_TimeAlarm);
        TextView PickedTime = dialog.findViewById(R.id.PickedTime);
        Spinner StayingFor = dialog.findViewById(R.id.StayingFor);
        TextInputLayout StayMinute = dialog.findViewById(R.id.StayMinute);//(Second)
        TextInputEditText MINUTE = dialog.findViewById(R.id.MINUTE);
        SwitchCompat is_recycle = dialog.findViewById(R.id.is_recycle);
        CheckBox is_sound = dialog.findViewById(R.id.is_Sound);
        CheckBox is_vibration = dialog.findViewById(R.id.is_Vibration);
        CheckBox is_popup = dialog.findViewById(R.id.is_Popup);
        CheckBox is_Event = dialog.findViewById(R.id.is_Event);
        Spinner WaitingFor = dialog.findViewById(R.id.WaitingFor);
        TextInputLayout WaitSecond = dialog.findViewById(R.id.WaitSecond);
        TextInputEditText SECOND = dialog.findViewById(R.id.SECOND);

        Time_or_Location.setOnCheckedChangeListener((view, id)->{
            if(id==R.id.is_TimeAlarm){
                dialog.findViewById(R.id.TimePickers).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.LocationPickers).setVisibility(View.GONE);
                dialog.findViewById(R.id.is_recycle).setVisibility(View.VISIBLE);
            } else if (id==R.id.is_LocationAlarm) {
                dialog.findViewById(R.id.TimePickers).setVisibility(View.GONE);
                dialog.findViewById(R.id.LocationPickers).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.is_recycle).setVisibility(View.GONE);
            }
        });

        dialog.findViewById(R.id.Time_Picker).setOnClickListener(v -> {
            if(requireActivity().getSupportFragmentManager().findFragmentByTag("timepicker")==null) {
                timepicker.show(requireActivity().getSupportFragmentManager(), "timepicker");
            }
        });

        dialog.findViewById(R.id.Location_Picker).setOnClickListener(v -> {
            if(requireActivity().getSupportFragmentManager().findFragmentByTag("LocationPicker")==null) {
                new ASF_LocationPickFragment().show(requireActivity().getSupportFragmentManager(), "LocationPicker");
            }
        });

        StayingFor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (StayingFor.getSelectedItem().toString().equals("N秒滞在後")){
                    StayMinute.setVisibility(View.VISIBLE);
                    staying_for = Integer.parseInt(Objects.requireNonNull(MINUTE.getText()).toString());
                } else {
                    StayMinute.setVisibility(View.GONE);
                    staying_for = 0;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 選択されなかった場合
            }
        });

        is_Event.setOnCheckedChangeListener((view,id)->{
            if(id){dialog.findViewById(R.id.Events).setVisibility(View.VISIBLE);}
            else{dialog.findViewById(R.id.Events).setVisibility(View.GONE);}
        });

        arrayAdapter = new ASF_EventsListAdapter(dialog.getContext(),R.layout.as_dialog_events_list, events,
                (position, view) -> {
                    events.remove(position);
                    arrayAdapter.notifyDataSetChanged();
                }, (pos)->{
                    ASF_AppPickFragment app_picker = new ASF_AppPickFragment();
                    position=pos;
                    app_picker.show(requireActivity().getSupportFragmentManager(),"app_picker");
                });
        ListView listView = dialog.findViewById(R.id.Event_List);
        listView.setAdapter(arrayAdapter);

        WaitingFor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (WaitingFor.getSelectedItem().toString().equals("N秒間鳴らす")) {
                    WaitSecond.setVisibility(View.VISIBLE);
                    waiting_for = Integer.parseInt(Objects.requireNonNull(SECOND.getText()).toString());
                } else {
                    WaitSecond.setVisibility(View.GONE);
                    waiting_for = 0;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 選択されなかった場合
            }
        });

        dialog.findViewById(R.id.Event_add).setOnClickListener(v -> {
            Spinner selectedEvent = dialog.findViewById(R.id.selected_Event);
            events.add(new ASF_EventsListItem(selectedEvent.getSelectedItemPosition()));
            arrayAdapter.notifyDataSetChanged();
        });

        dialog.findViewById(R.id.Cancel).setOnClickListener(v->dismiss());

        dialog.findViewById(R.id.Enter).setOnClickListener(v -> {
            ArrayList<String> newEvent= new ArrayList<>();
            //{{0is_on,1"Time",2time,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for},{~},...}
            //{{0is_on,1"Name",2name,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for,9staying_for},{~},...}
            //{{0is_on,1"Location",2location,3is_recycle,4is_sound,5is_vibration,6is_Popup,7is_Event,8waiting_for,9staying_for,10within,11lat,12lon},{~},...}
            newEvent.add("true");//0
            if (Time.isChecked()){
                newEvent.add("Time");//1
                newEvent.add((String) PickedTime.getText());//2
            }
            else{
                newEvent.add(locationTAG);//1
                if(Objects.equals(locationTAG, "Name")){
                    TextView location = dialog.findViewById(R.id.PickedLocation);
                    newEvent.add(location.getText().toString());//2
                }
                else{
                    if(locationName==null){
                        TextView PickedLocation = dialog.findViewById(R.id.PickedLocation);
                        newEvent.add(PickedLocation.getText().toString());//2
                    }else {
                        newEvent.add(locationName);//2
                    }
                }
            }
            newEvent.add(String.valueOf(is_recycle.isChecked()));//3
            newEvent.add(String.valueOf(is_sound.isChecked()));//4
            newEvent.add(String.valueOf(is_vibration.isChecked()));//5
            newEvent.add(String.valueOf(is_popup.isChecked()));//6
            newEvent.add(String.valueOf(is_Event.isChecked()));//7
            newEvent.add(String.valueOf(waiting_for));//8
            if(!Time.isChecked()){
                if(StayingFor.getSelectedItemPosition()==1){newEvent.add(Objects.requireNonNull(MINUTE.getText()).toString());}//9
                else if(StayingFor.getSelectedItemPosition()==0){newEvent.add("0");}//9
                newEvent.add(String.valueOf(within));//10
                if(Lat!=0.0&&Lon!=0.0){
                    newEvent.add(String.valueOf(Lat));//11
                    newEvent.add(String.valueOf(Lon));//11
                }
            }
            Alarms.add(newEvent);
            System.out.println(Alarms);

            ArrayList<ArrayList<String>> events_ALS=new ArrayList<>();
            for(ASF_EventsListItem item : events){
                ArrayList<String> appendArrayList = new ArrayList<>();
                String itemMode=item.getMode();
                appendArrayList.add(itemMode);//0
                if(Objects.equals(itemMode, "app")){
                    appendArrayList.add(item.getPackageName());//1
                    appendArrayList.add(item.getClassName());//2
                    appendArrayList.add(item.getAppName());//3
                } else if (Objects.equals(itemMode, "brightness") || Objects.equals(itemMode, "volume")) {
                    appendArrayList.add(String.valueOf(item.getProgress()));//1
                }
                events_ALS.add(appendArrayList);
            }
            Events.add(events_ALS);
            events.clear();

            Main_Activity callingActivity = (Main_Activity) getActivity();
            assert callingActivity != null;
            callingActivity.saveAlarmsEvents();
            callingActivity.loadAlarmItems();
            adapter.notifyDataSetChanged();
            callingActivity.createAlarm();
            dismiss();
        });
        return dialog;
    }
    public void onTimePicked(String time) {
        TextView PickedTime = dialog.findViewById(R.id.PickedTime);
        PickedTime.setText(time);
    }
    public void onLocationPicked(String location,String Tag){
        TextView PickedLocation = dialog.findViewById(R.id.PickedLocation);
        if(Objects.equals(Tag, "Name")) {PickedLocation.setText(location);}
        else if (locationName==null) {PickedLocation.setText(location);}
        locationTAG=Tag;
    }
    public void onLocationName(double lat,double lon){
        String ApiID = "dj00aiZpPWRGNlpKbFhOZ3NGVyZzPWNvbnN1bWVyc2VjcmV0Jng9N2M-";
        TextView PickedLocation = dialog.findViewById(R.id.PickedLocation);

        ExecutorService service = Executors.newFixedThreadPool(1);
        LocationCallable locationCallable = new LocationCallable(lat,lon,ApiID);
        Future<ArrayList<String[]>> pos_info_F = service.submit(locationCallable);
        try {
            String[] pos_info = pos_info_F.get().get(0);
            PickedLocation.setText(String.format("%s付近", pos_info[0]));
            locationName=String.format("%s付近", pos_info[0]);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        service.shutdown();
    }
    public void setLatLon(double lat,double lon){
        Lat=lat;
        Lon=lon;
    }
    public void setWithin(int get_within){within=get_within;}
}