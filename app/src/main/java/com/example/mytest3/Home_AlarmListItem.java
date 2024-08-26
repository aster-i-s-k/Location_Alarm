package com.example.mytest3;

public class Home_AlarmListItem {
    private String Main;
    private String Description;
    private String Sound;
    private String Vibration;
    private String Popup;
    private String Event;
    private String Recycle;
    private String is_On;

    public Home_AlarmListItem(String main, String description, String sound, String vibration, String popup, String event, String recycle, String is_on) {
        //{{is_on ,"Time" ,time ,is_recycle ,is_sound ,is_vibration ,is_Popup,is_Event,Events},{~},...}
        Main = main;
        Description = description;
        Sound=sound;
        Vibration = vibration;
        Popup=popup;
        Event=event;
        Recycle=recycle;
        is_On=is_on;
    }
    public String getMain() {return Main;}
    public String getDescription() {
        return Description;
    }
    public String getSound(){
        return Sound;
    }
    public String getVibration(){
        return Vibration;
    }
    public String getPopup(){
        return Popup;
    }
    public String getEvent(){
        return Event;
    }
    public String getRecycle(){
        return Recycle;
    }
    public String getPower(){
        return is_On;
    }

}
