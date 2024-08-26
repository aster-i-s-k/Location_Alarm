package com.example.mytest3;

import static com.example.mytest3.Main_PagerAdapter.Home.ListPos;
import static com.example.mytest3.Main_PagerAdapter.Locate.FavoriteListPos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class Home_AlarmDeleteFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireActivity())
                .setTitle("確認")
                .setMessage("アイテムを削除しますか？")
                .setPositiveButton("はい",(dialog,id)->{
                    Main_Activity listener = (Main_Activity) getActivity();
                    assert listener != null;
                    if(ListPos!=-1) {
                        listener.deleteAlarmsEvents(ListPos);
                        listener.createAlarm();
                        ListPos=-1;
                    } else if(FavoriteListPos!=-1) {
                        listener.deleteFavoriteLocation(FavoriteListPos);
                        FavoriteListPos=-1;
                    }
                })
                .setNegativeButton("いいえ", (dialog, which) -> {
                    ListPos=-1;
                    FavoriteListPos=-1;
                })
                .create();
    }
}