package com.example.mytest3;

import static com.example.mytest3.Main_Activity.AlarmItems;
import static com.example.mytest3.Main_Activity.Alarms;
import static com.example.mytest3.Main_Activity.FavoriteLocation;
import static com.example.mytest3.Main_Activity.Icon;
import static com.example.mytest3.Main_Activity.MaybeLocation;
import static com.example.mytest3.Main_Activity.MaybeLocation_count;
import static com.example.mytest3.Main_Activity.lat;
import static com.example.mytest3.Main_Activity.lon;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Main_PagerAdapter extends FragmentStateAdapter {
    private static Main_Activity listener;
    private static final int Pages = 3;

    public static class Home extends Fragment{
        static Home_AlarmListAdapter adapter;
        View rootView;
        @SuppressLint("StaticFieldLeak")
        static TextView pos;
        @SuppressLint("StaticFieldLeak")
        static TextView rat;
        public static int ListPos = -1;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.home, container, false);
            return rootView;
        }
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Main_Activity mainActivity=(Main_Activity)getActivity();
            adapter = new Home_AlarmListAdapter(getContext(), R.layout.home_alarms_list,AlarmItems,mainActivity);
            ListView AlarmView = view.findViewById(R.id.AlarmList);

            AlarmView.setOnItemLongClickListener((parent, view1, position, id) -> {
                if(requireActivity().getSupportFragmentManager().findFragmentByTag("deleteA?") ==null) {
                    ListPos = position;
                    DialogFragment dialogFragment = new Home_AlarmDeleteFragment();
                    dialogFragment.show(requireActivity().getSupportFragmentManager(), "deleteA?");
                    return true;
                }
                return false;
            });

            AlarmView.setAdapter(adapter);

            pos=view.findViewById(R.id.pos);
            rat=view.findViewById(R.id.rate);
        }
        public void change_text(String position,String rate){
            if(rat!=null) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.post(()->{
                    pos.setText(String.format("現在地：%s", position));
                    rat.setText(String.format("精度：%s", rate));
                });
            }
        }
    }

    public static class Map extends Fragment{
        static Boolean flag = false;
        static IMapController mv_Controller;
        static MapView mapview;
        static Marker myLocation;
        static List<Polygon> areas = new ArrayList<>();
        View rootView;

        public void onResume(){
            super.onResume();
            if (mapview!=null) {
                mapview.onResume();
            }
        }
        public void onPause(){
            super.onPause();
            if (mapview!=null) {
                mapview.onPause();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.map, container, false);
            return rootView;
        }
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if(mapview==null) {
                mapview = view.findViewById(R.id.LP_mapView);
                mapview.setTileSource(TileSourceFactory.MAPNIK);
                mapview.setDestroyMode(false);
                mapview.setMultiTouchControls(true);
                mv_Controller = mapview.getController();
                mv_Controller.setZoom(18.0);
                setArea();
                mapview.invalidate();
                setPos();
            }
            view.findViewById(R.id.back_to_pos).setOnClickListener(v->{flag=!flag;setPos();});
        }

        public void setPos(){
            if(mv_Controller != null && lat!=0.0 && lon!=0.0) {
                GeoPoint pos = new GeoPoint(lat, lon);
                if (!flag) {
                    mv_Controller.setCenter(pos);
                    flag = true;
                    setArea();
                }
                deleteMarker(myLocation);
                myLocation = new Marker(mapview);
                myLocation.setInfoWindow(null);
                myLocation.setPosition(pos);
                Drawable icon = Icon;
                myLocation.setIcon(icon);
                mapview.getOverlays().add(myLocation);

                mapview.invalidate();

            }
        }
        public void setArea(){
            List<List<GeoPoint>> holes = new ArrayList<>();
            deleteArea(areas);
            areas.clear();
            for(ArrayList<String> Alarm:Alarms){
                System.out.println(Alarm.get(1));
                if(Objects.equals(Alarm.get(0),"true")) {
                    if (Objects.equals(Alarm.get(1), "Location")) {
                        GeoPoint point = new GeoPoint(Double.parseDouble(Alarm.get(11)), Double.parseDouble(Alarm.get(12)));
                        holes.add(Polygon.pointsAsCircle(point, Double.parseDouble(Alarm.get(10))));
                        holes.get(0).add(holes.get(0).get(0));
                    }
                }
            }
            for(List<GeoPoint> hole:holes) {
                Polygon polygon = new Polygon(mapview);
                polygon.getFillPaint().setColor(Color.parseColor("#4BFF0000"));
                polygon.setPoints(hole);
                polygon.getFillPaint().setStrokeWidth(2);
                polygon.setInfoWindow(null);
                areas.add(polygon);
                mapview.getOverlayManager().add(polygon);
            }
        }
        private void deleteMarker(Marker Pin){
            if(Pin!=null) {
                mapview.getOverlays().remove(Pin);
            }
        }
        private void deleteArea(List<Polygon> delete_areas){
            if(delete_areas!=null){
                for(Polygon delete_area:delete_areas){
                    mapview.getOverlayManager().remove(delete_area);
                }
            }
        }
    }

    public static class Locate extends Fragment{
        static class MyComparableObject implements Comparable<MyComparableObject>{
            private final String Name;
            private final double Score;
            public MyComparableObject(String Name,double Score) {
                this.Name=Name;
                this.Score= Score;
            }
            public String getName(){
                return this.Name;
            }
            public  int getScore(){
                return ((int) this.Score);
            }
            public int compareTo(MyComparableObject passedObj) {
                return 0;
            }
        }
        static ArrayAdapter<String> arrayAdapter;
        View rootView;
        public static int FavoriteListPos=-1;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.locate, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Button addButton1 = view.findViewById(R.id.AddLocationButton1);
            Button addButton2 = view.findViewById(R.id.AddLocationButton2);
            Button addButton3 = view.findViewById(R.id.AddLocationButton3);
            Button addButton4 = view.findViewById(R.id.AddLocationButton4);
            TextInputEditText editText = view.findViewById(R.id.AddLocationInputText);
            Button AddLocation = view.findViewById(R.id.addLocation);
            ListView LocationList = view.findViewById(R.id.Locate_LocationList);

            listener=(Main_Activity) getActivity();
            assert listener != null;

            view.findViewById(R.id.Locate_explain1).setSelected(true);
            view.findViewById(R.id.Locate_explain2).setSelected(true);

            List<MyComparableObject> Location_Count = new ArrayList<>();
            for (int i=0;i<MaybeLocation.size();i++) {
                Location_Count.add(new MyComparableObject(MaybeLocation.get(i), MaybeLocation_count.get(i)));
            }
            Collections.sort(Location_Count);
            if(Location_Count.size()>0) {
                addButton1.setText(Location_Count.get(0).getName());
            }
            if(Location_Count.size()>1) {
                addButton2.setText(Location_Count.get(1).getName());
            }
            if(Location_Count.size()>2) {
                addButton3.setText(Location_Count.get(2).getName());
            }
            if(Location_Count.size()>3) {
                addButton4.setText(Location_Count.get(3).getName());
            }

            for(int i=0; i<3 && i<Location_Count.size();i++){
                int Pos = MaybeLocation.indexOf(Location_Count.get(i).getName());
                MaybeLocation.remove(Location_Count.get(i).getName());
                MaybeLocation_count.remove(Pos);
            }
            listener.saveMaybeLocation_and_count();

            addButton1.setOnClickListener(v -> {
                String text = addButton1.getText().toString();
                add_Favorite(text,addButton1);
            });
            addButton2.setOnClickListener(v -> {
                String text = addButton2.getText().toString();
                add_Favorite(text,addButton2);
            });
            addButton3.setOnClickListener(v -> {
                String text = addButton3.getText().toString();
                add_Favorite(text,addButton3);
            });
            addButton4.setOnClickListener(v -> {
                String text = addButton4.getText().toString();
                add_Favorite(text,addButton4);
            });

            AddLocation.setOnClickListener(v->{
                String text = Objects.requireNonNull(editText.getText()).toString();
                if(!text.equals("")){
                    FavoriteLocation.add(text);
                    editText.setText("");
                    arrayAdapter.notifyDataSetChanged();
                    listener.saveFavoriteLocation();
                }
            });

            arrayAdapter = new ArrayAdapter<>(requireContext(),android.R.layout.simple_list_item_1,FavoriteLocation);
            LocationList.setAdapter(arrayAdapter);
            LocationList.setOnItemLongClickListener((parent, view1, position, id) -> {
                FavoriteListPos=position;
                DialogFragment dialogFragment = new Home_AlarmDeleteFragment();
                dialogFragment.show(requireActivity().getSupportFragmentManager(), "deleteFL?");
                return true;
            });
        }

        public void add_Favorite(String text,Button button){
            if(!text.equals("いろいろな場所を訪れましょう")) {
                FavoriteLocation.add(text);
                arrayAdapter.notifyDataSetChanged();
                button.setText("お気に入りの場所に追加しました");
            }
        }
    }

    public Main_PagerAdapter(FragmentActivity fm) {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment createFragment(int pos) {
        if(pos == 0){
            return new Map();
        }
        else if(pos == 1){
            return new Home();
        }
        else if(pos == 2){
            return new Locate();
        }
        else {
            return new Home();
        }
    }

    @Override
    public int getItemCount() {
        return Pages;
    }
}
