package com.minsung.examples.Data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class DataStoreFunc extends Activity {

    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    private static final String KEY_BLE_ID = "id";
    Context _context;

    public DataStoreFunc(Context context){
        this._context = context;
        sharedPreferences = _context.getSharedPreferences("pref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setBleID(String id) {

        editor.putString(KEY_BLE_ID, id);
        // commit changes
        editor.commit();

    }

    public String getBleID() {
        return sharedPreferences.getString(KEY_BLE_ID, "");
    }

    public String getDegree(){
        return sharedPreferences.getString("G","");
    }

    public int getTime(){
        return sharedPreferences.getInt("T",0);
    }

    public boolean getVOption(){
        if(sharedPreferences.getString("V","true").equals("true")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean getSOption(){
        if(sharedPreferences.getString("S","true").equals("true")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean getPOption(){
        if(sharedPreferences.getString("P","true").equals("true")){
            return true;
        }
        else{
            return false;
        }
    }

    public int getWhouare(){
        String option = sharedPreferences.getString("Option","");

        if(option.equals(Database.DISABLED)){
            return 1;
        }
        else if(option.equals(Database.SENIOR)){
            return 0;
        }
        else if(option.equals(Database.PREGNANT)){
            return 3;
        }
        else if(option.equals(Database.LEGHURT)){
            return 1;
        }
        else if(option.equals(Database.CHILD)){
            return 2;
        }
        else{
            return 4;
        }
    }

}
