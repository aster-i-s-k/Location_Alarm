package com.example.mytest3;

import java.util.Objects;

public class ASF_EventsListItem {
    private String Mode="app";
    //mode={app,volume,slightness,manorMode}
    private String PackageName;
    private String ClassName;
    private String AppName="アプリの選択";
    private int Progress=0;
    public ASF_EventsListItem(int mode){
        if(Objects.equals(mode, 0)){
            Mode="app";
        }else if (Objects.equals(mode, 1)) {
            Mode="brightness";
        }else if (Objects.equals(mode, 2)) {
            Mode="volume";
        }
    }
    public void setPackageName(String packageName){PackageName=packageName;}
    public void setClassName(String className){ClassName=className;}
    public void setAppName(String appName){AppName=appName;}
    public void setProgress(int progress){Progress=progress;}
    public String getMode() {
        return Mode;
    }
    public String getPackageName(){return PackageName;}
    public String getClassName(){return  ClassName;}
    public String getAppName(){return AppName;}
    public int getProgress(){return Progress;}
}
