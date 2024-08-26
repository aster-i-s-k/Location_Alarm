package com.example.mytest3;

import static com.example.mytest3.Main_Activity.Alarms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import java.util.List;
import java.util.Objects;

public class Home_AlarmListAdapter extends ArrayAdapter<Home_AlarmListItem> {
    private final int mResource;
    private final List<Home_AlarmListItem> mItems;
    private final LayoutInflater mInflater;
    private final Main_Activity mActivity;

    public Home_AlarmListAdapter(Context context, int resource, List<Home_AlarmListItem> items,Main_Activity activity) {
        super(context, resource, items);
        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActivity = activity;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView != null) {view = convertView;}
        else {view = mInflater.inflate(mResource, null);}
        Home_AlarmListItem item = mItems.get(position);

        TextView title = view.findViewById(R.id.Main);
        title.setText(item.getMain());

        TextView des = view.findViewById(R.id.Description);
        des.setText(item.getDescription());
        des.setSelected(true);

        ImageView sound= view.findViewById(R.id.Sound);
        if(Objects.equals(item.getSound(), "false")){
            sound.setVisibility(View.GONE);
        }
        else{sound.setVisibility(View.VISIBLE);}

        ImageView vibration= view.findViewById(R.id.Vibration);
        if(Objects.equals(item.getVibration(), "false")){
            vibration.setVisibility(View.GONE);
        }
        else{vibration.setVisibility(View.VISIBLE);}

        ImageView popup= view.findViewById(R.id.Popup);
        if(Objects.equals(item.getPopup(), "false")){
            popup.setVisibility(View.GONE);
        }
        else{popup.setVisibility(View.VISIBLE);}

        ImageView event= view.findViewById(R.id.Event);
        if(Objects.equals(item.getEvent(), "false")){
            event.setVisibility(View.GONE);
        }
        else{event.setVisibility(View.VISIBLE);}

        ImageView recycle= view.findViewById(R.id.Recycle);
        if(Objects.equals(item.getRecycle(), "false")){
            recycle.setImageResource(R.drawable.sync_disabled);
        }
        else{recycle.setImageResource(R.drawable.sync);}

        SwitchCompat power= view.findViewById(R.id.Power);
        power.setChecked(!Objects.equals(item.getPower(), "false"));
        power.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Alarms.get(position).set(0, String.valueOf(isChecked));
            mActivity.saveAlarmsEvents();
            mActivity.loadAlarmItems();
            notifyDataSetChanged();
            if (Objects.equals(Alarms.get(position).get(1), "Time")) {
                mActivity.createAlarm();
            }
        });


        return view;
    }
}
