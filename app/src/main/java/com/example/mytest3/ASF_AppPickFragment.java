package com.example.mytest3;

import static com.example.mytest3.AlarmSettingFragment.arrayAdapter;
import static com.example.mytest3.AlarmSettingFragment.events;
import static com.example.mytest3.AlarmSettingFragment.position;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ASF_AppPickFragment extends DialogFragment {
    private static class AppData {
        String label;
        Drawable icon;
        String pname;
    }
    private static class AppListAdapter extends ArrayAdapter<AppData> {
        private final LayoutInflater mInflater;
        public AppListAdapter(Context context, List<AppData> dataList) {
            super(context, R.layout.as_dialog_apps_list);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(dataList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.as_dialog_apps_list, parent, false);
                holder.textLabel = convertView.findViewById(R.id.label);
                holder.imageIcon = convertView.findViewById(R.id.icon);
                holder.packageName = convertView.findViewById(R.id.pname);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 表示データを取得
            final AppData data = getItem(position);
            // ラベルとアイコンをリストビューに設定
            assert data != null;
            holder.textLabel.setText(data.label);
            holder.imageIcon.setImageDrawable(data.icon);
            holder.packageName.setText(data.pname);
            return convertView;
        }
    }
    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
        TextView packageName;
    }
    ArrayList<Integer> target_pos = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Dialog dialog = new Dialog(requireActivity());
        // 端末にインストール済のアプリケーション一覧情報を取得
        final PackageManager pm = requireActivity().getPackageManager();
        final int flags = PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES;
        final List<PackageInfo> installedAppList = pm.getInstalledPackages(0);

        // リストに一覧データを格納する
        final List<AppData> dataList = new ArrayList<>();
        int i = 0;
        for (PackageInfo app : installedAppList) {
            if(pm.getLaunchIntentForPackage(app.packageName)!=null){
                AppData data = new AppData();
                ApplicationInfo appInfo=app.applicationInfo;
                data.label = appInfo.loadLabel(pm).toString();
                data.icon = appInfo.loadIcon(pm);
                data.pname = app.packageName;
                target_pos.add(i);
                dataList.add(data);
            }
            i++;
        }
        // リストビューにアプリケーションの一覧を表示する
        final ListView listView = getListView(dataList, installedAppList, pm);
        dialog.setContentView(listView);
        return dialog;
    }

    private @NonNull ListView getListView(List<AppData> dataList, List<PackageInfo> installedAppList, PackageManager pm) {
        final ListView listView = new ListView(getContext());
        listView.setAdapter(new AppListAdapter(getContext(), dataList));
        //クリック処理
        listView.setOnItemClickListener((parent, view, pos, id) -> {
            PackageInfo item = installedAppList.get(target_pos.get(pos));
            events.get(position).setPackageName(item.packageName);

            ApplicationInfo app=item.applicationInfo;

            if(app.className==null){
                events.get(position).setClassName(item.packageName);
            }else {
                events.get(position).setClassName(app.className);
            }
            events.get(position).setAppName(app.loadLabel(pm).toString());
            arrayAdapter.notifyDataSetChanged();
            dismiss();
        });
        return listView;
    }
}
