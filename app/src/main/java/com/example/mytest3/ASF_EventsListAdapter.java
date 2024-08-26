package com.example.mytest3;


import static com.example.mytest3.AlarmSettingFragment.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASF_EventsListAdapter extends ArrayAdapter<ASF_EventsListItem> {
    private final int mResource;
    private final List<ASF_EventsListItem> mItems;
    private final LayoutInflater mInflater;
    private final AlarmSettingFragment.EventListDeleteClickedListener mlistener1;
    private final AlarmSettingFragment.EventListAppPickClickedListener mlistener2;
    public ASF_EventsListAdapter(Context context,
                                 int resource,
                                 ArrayList<ASF_EventsListItem> items,
                                 AlarmSettingFragment.EventListDeleteClickedListener listener1,
                                 AlarmSettingFragment.EventListAppPickClickedListener listener2) {
        super(context, resource, items);
        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistener1 = listener1;
        mlistener2 = listener2;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        }
        else {
            view = mInflater.inflate(mResource, null);
        }
        ASF_EventsListItem item = mItems.get(position);
        String Mode = item.getMode();
        String AppName = item.getAppName();

        TextView name = view.findViewById(R.id.event_name);
        Button appPicker = view.findViewById(R.id.apppicker);
        SeekBar seekBar = view.findViewById(R.id.seekBar);

        if(Objects.equals(Mode,"app")){
            name.setText("アプリを起動");
            appPicker.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.GONE);
            appPicker.setText(AppName);
        }
        else if(Objects.equals(Mode,"brightness")){
            name.setText("明るさを変更");
            appPicker.setVisibility(View.GONE);
            seekBar.setVisibility(View.VISIBLE);
        }
        else if(Objects.equals(Mode,"volume")){
            name.setText("音量を変更");
            appPicker.setVisibility(View.GONE);
            seekBar.setVisibility(View.VISIBLE);
        }

        appPicker.setOnClickListener(v->{
            mlistener2.AppPick(position);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                events.get(position).setProgress(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        view.findViewById(R.id.delete).setOnClickListener(v->{
            mlistener1.deleteEvent(position,view);
        });
        return view;
    }

}
