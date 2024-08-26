package com.example.mytest3;

import static com.example.mytest3.Main_Activity.FavoriteLocation;
import static com.example.mytest3.Main_Activity.Icon;
import static com.example.mytest3.Main_Activity.lat;
import static com.example.mytest3.Main_Activity.lon;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;
import java.util.Objects;

public class ASF_LocationPickFragment extends DialogFragment {
    private Main_Activity listener;
    private TextInputEditText RadInputText;
    private Boolean flag = false;
    private MapView mapView;
    static IMapController mv_Controller;
    static Marker myLocation;
    static Marker Marker;
    static GeoPoint MarkerPos;
    static Polygon polygon;
    private double latitude;
    private double longitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);
        Dialog dialog = new Dialog(requireActivity());
        Objects.requireNonNull(dialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.as_location_pick_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RadioGroup RadioPickBy = dialog.findViewById(R.id.LocationPickBy);
        TextInputEditText LocationInputText = dialog.findViewById(R.id.LocationInputText);
        RadInputText = dialog.findViewById(R.id.Rad_InputTextEdit);
        ListView LocationList = dialog.findViewById(R.id.LocationList);
        mapView = dialog.findViewById(R.id.LP_mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setDestroyMode(false);
        mapView.setMultiTouchControls(true);
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                deleteMarker(Marker);
                deleteArea(polygon);
                setMarker(p);
                setArea(p);
                mapView.invalidate();
                MarkerPos=p;
                LocationInputText.setText(p.toString());
                latitude = p.getLatitude();
                longitude = p.getLongitude();

                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay tapOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(tapOverlay);
        mv_Controller = mapView.getController();
        mv_Controller.setZoom(18.0);
        setPos();

        RadioPickBy.setOnCheckedChangeListener((view, id)->{
            if(id==R.id.PickFromName){
                LocationList.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                LocationInputText.setEnabled(true);
                dialog.findViewById(R.id.Within).setVisibility(View.GONE);
            } else if (id==R.id.PickFromMap) {
                LocationList.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                LocationInputText.setEnabled(false);
                dialog.findViewById(R.id.Within).setVisibility(View.VISIBLE);
            }
        });

        RadInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                deleteArea(polygon);
                setArea(MarkerPos);
            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(dialog.getContext(),android.R.layout.simple_list_item_1,FavoriteLocation);
        LocationList.setAdapter(arrayAdapter);
        LocationList.setOnItemClickListener((parent, view, position, id) -> LocationInputText.setText(FavoriteLocation.get(position)));

        dialog.findViewById(R.id.Location_Enter).setOnClickListener(v->{
            if(!Objects.requireNonNull(LocationInputText.getText()).toString().equals("")) {
                String Tag;
                listener=(Main_Activity) getActivity();
                assert listener != null;
                int Id = RadioPickBy.getCheckedRadioButtonId();
                if(Id==R.id.PickFromName){
                    Tag="Name";
                    listener.dialogFragment.setLatLon(0.0,0.0);
                }
                else{
                    Tag="Location";
                    listener.dialogFragment.setWithin(Integer.parseInt(RadInputText.getText().toString()));
                    listener.dialogFragment.onLocationName(latitude,longitude);
                    listener.dialogFragment.setLatLon(latitude,longitude);
                }
                listener.dialogFragment.onLocationPicked(LocationInputText.getText().toString(),Tag);
                dismiss();
            }
            else {
                Toast.makeText(getContext(),"場所名または(緯度,経度)を入力してください",Toast.LENGTH_SHORT).show();
            }
        });
        return dialog;
    }

    private void setMarker(GeoPoint p){
        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.pin, null);
        assert icon != null;
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
        Drawable iconNew = new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(bitmap,60,45,true));
        Marker Pin = new Marker(mapView);
        Pin.setPosition(p);
        Pin.setIcon(iconNew);
        Pin.setInfoWindow(null);
        mapView.getOverlays().add(Pin);

        Marker = Pin;
    }
    private void setArea(GeoPoint p){
        List<GeoPoint> hole;
        GeoPoint point = new GeoPoint(p);
        hole=Polygon.pointsAsCircle(point,Double.valueOf(RadInputText.getText().toString()));
        polygon = new Polygon(mapView);
        polygon.getFillPaint().setColor(Color.parseColor("#4BFF0000"));
        polygon.setPoints(hole);
        polygon.getFillPaint().setStrokeWidth(2);
        polygon.setInfoWindow(null);
        mapView.getOverlayManager().add(polygon);
    }
    private void setPos(){
        if(mv_Controller != null && lat!=0.0 && lon!=0.0) {
            GeoPoint pos = new GeoPoint(lat, lon);
            if (!flag) {
                mv_Controller.setCenter(pos);
                flag = true;
            }
            deleteMarker(myLocation);
            deleteArea(polygon);
            myLocation = new Marker(mapView);
            myLocation.setInfoWindow(null);
            myLocation.setPosition(pos);
            Drawable icon = Icon;
            myLocation.setIcon(icon);
            mapView.getOverlays().add(myLocation);

            mapView.invalidate();
        }
    }

    private void deleteMarker(Marker marker){
        if(marker!=null) {
            mapView.getOverlays().remove(marker);
        }
    }
    private void deleteArea(Polygon polygon){
        if(polygon!=null){
            mapView.getOverlayManager().remove(polygon);
        }
    }
}
