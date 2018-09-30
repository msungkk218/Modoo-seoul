package com.minsung.examples;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.minsung.examples.Data.DataStoreFunc;
import com.minsung.examples.Data.Database;
import com.minsung.examples.Info.Register;
import com.minsung.examples.Tutorial.tutorial;

import java.util.UUID;

public class SplashActivity extends Activity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String bluetoothID = "2f234454-cf6d-4a0f-adf2-f4911b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        DataStoreFunc dataManager = new DataStoreFunc( getApplicationContext());
        // set blueth
        if(dataManager.getBleID().equals("")) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            bluetoothID += uuid.substring(0, 6);
            System.out.println("------------blueID : " + bluetoothID);
            dataManager.setBleID(bluetoothID);
        }
        else{
            bluetoothID = dataManager.getBleID();
        }
        System.out.println("----------------------id:"+dataManager.getBleID());

        if (FirebaseInstanceId.getInstance().getToken() != null) {
            Log.d("fcm", "token = " + FirebaseInstanceId.getInstance().getToken());
        }
        sharedPreferences = getSharedPreferences("pref",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String s = (sharedPreferences.getString("S","NULL"));
        String v = (sharedPreferences.getString("V","NULL"));
        String p = (sharedPreferences.getString("P","NULL"));
        String n = sharedPreferences.getString("N","NULL");
        String g = sharedPreferences.getString("G","NULL");
        String t = sharedPreferences.getString("T","NULL");
        String Auth = sharedPreferences.getString("Auth","NULL");
        String Tutorial = sharedPreferences.getString("Tutorial","NULL");
        String Option = sharedPreferences.getString("Option","NULL");
        String Bonus = sharedPreferences.getString("Bonus","NULL");
        String Total = sharedPreferences.getString("Total","NULL");

        if (s.equals("NULL")){
            editor.putString("S","true");
            editor.putString("V","true");
            editor.putString("P","true");
            editor.putString("N","홍길동");
            editor.putString("G","4급");
            editor.putString("T","3초");
            editor.putString("Auth","false");
            editor.putString("Tutorial","true");
            editor.putString("Option","0");
            editor.putString("Bonus","true");
            editor.putString("Total","true");
            editor.commit();



        }
        else{
            Database.setAlarmSound(Boolean.valueOf(s));
            Database.setAlarmbVibration(Boolean.valueOf(v));
            Database.setAlarmPush(Boolean.valueOf(p));
            Database.setUserName(n);
            Database.setUserGrade(g);
            Database.setBounusTimeString(t);
            Database.setTutorial(Boolean.valueOf(Tutorial));
            Database.setAuth(Boolean.valueOf(Auth));
            Database.setOption(Option);
            Database.setBounusTimeBool(Boolean.valueOf(Bonus));
            Database.setTotalAlarm(Boolean.valueOf(Total));
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getBaseContext(),MainActivity.class);
                if (Database.isTutorial()){
                    intent = new Intent(getBaseContext(),tutorial.class);
                }
                else if(!Database.isAuth()){
                    intent = new Intent(getBaseContext(),Register.class);
                }
                startActivity(intent);
                finish();
            }
        },2000);
    }
}
