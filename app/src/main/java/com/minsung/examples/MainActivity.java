package com.minsung.examples;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.RemoteException;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.minsung.examples.Data.DataStoreFunc;
import com.minsung.examples.Data.Database;
import com.minsung.examples.Data.Item;
import com.minsung.examples.Info.InfoMain;
import com.minsung.examples.Info.Register;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {



    private static final int ONE_SECOND = 1000;
    private static final int VIBRATE_ON = 1;
    private static final int VIBRATE_OFF = 0;
    private static final int RED = 1;
    private static final int NO_SIGNAL =2;
    private static final int GREEN =3;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextView tv_light;
    private TextView tv_light_detail;
    private TextView tv_time_left;
    private TextView tv_no_signal;
    private ImageView iv_no_signal_detail;
    private ImageButton ib_drawer;
    private Button btn_alarmOn;
    private Button btn_alarmOff;
    private ProgressBar pb_progress;
    private ImageView test_btn_sign;
    private ImageButton Ring;

    private CountDownTimer timer;
    private Vibrator vibrator;
    private TextToSpeech tts;
    private int allotted_time;
    private int time_left;
    private int vibrate_flag = VIBRATE_OFF;
    private int vibrate_start = 5000;
    long[] vibrate_pattern = {100, 1000, 100, 1000};
    String[] tts_pattern_green = {"빨간불로 바뀌었습니다!", "1초", "2초", "3초", "4초", "5초"};
    String[] tts_pattern_red = {"초록불로 바뀌었습니다!", "1초", "2초", "3초", "4초", "5초"};
    double[] ratio = {1/0.57,1/0.67,1/0.63,1/8};


    Handler mHandler = null;

    private BeaconManager beaconManager;
    private ArrayList<Item> items;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private DataStoreFunc dataManager;

    private LinearLayout ll_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_time_left = (TextView) findViewById(R.id.main_tv_time_sec);
        tv_light = (TextView) findViewById(R.id.main_tv_msgTop);
        tv_light_detail = (TextView) findViewById(R.id.main_tv_msgBottom);
        ib_drawer = (ImageButton) findViewById(R.id.main_ib_drawer);
        pb_progress = (ProgressBar) findViewById(R.id.main_pb_progress);
        test_btn_sign = findViewById(R.id.main_iv_sign);
        Ring = (ImageButton) findViewById(R.id.main_ib_ring);
        ll_time = (LinearLayout) findViewById(R.id.main_lv_time);
        tv_no_signal = (TextView) findViewById(R.id.tv_no_signal);
        iv_no_signal_detail = (ImageView) findViewById(R.id.main_iv_no_signal_detail);
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.bind(this);
        dataManager = new DataStoreFunc(this);

        mHandler = new Handler();



        System.out.println("----------------------id:"+dataManager.getBleID());

        setUi(NO_SIGNAL,0);
        //test();

        Log.d("SSSSSSSS", Database.getOption());


        // 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("이 앱은 위치 정보 접근 권한을 필요로합니다");
                builder.setMessage("비콘 기능을 활성화 하기 위해서는 권한 설정에 동의해주세요");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

                    }
                    }
                });
                builder.show();
            }
        }


        sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int imgsrc = 0;

        if (Database.isTotalAlarm()) {
            imgsrc = R.drawable.ic_alarm;
        } else {
            imgsrc = R.drawable.group;
        }

        Ring.setImageResource(imgsrc);


        Permission permission = new Permission(getApplicationContext(), this);
        permission.checkPermission();

        final Intent intent = new Intent(this, InfoMain.class);
        ib_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);

            }
        });


        String channelId = "channel";
        String channelName = "Channel Name";

        NotificationManager notifManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);

            notifManager.createNotificationChannel(mChannel);

        }

//        NotificationCompat.Builder builder =
//                new NotificationCompat.Builder(getApplicationContext(), channelId);
//
//        Intent notificationIntent = new Intent(getApplicationContext()
//                , MainActivity.class);
//
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//        int requestID = (int) System.currentTimeMillis();
//
//        PendingIntent pendingIntent
//                = PendingIntent.getActivity(getApplicationContext()
//                , requestID
//                , notificationIntent
//                , PendingIntent.FLAG_UPDATE_CURRENT);
//
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
//        remoteViews.setTextViewText(R.id.noti_tv_light, "초록불");
//
//
//        builder.setContentTitle("Title") // required
//                .setContentText("Content")  // required
//                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
//                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
//                .setSound(RingtoneManager
//                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setSmallIcon(android.R.drawable.btn_star)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources()
//                        , R.drawable.group_7));
//                .setBadgeIconType(R.drawable.group_8)
//
//                .setContent(remoteViews);
//
//
////
////        builder.setContent(remoteViews)
////                .setContentIntent(createPendingIntent());
////        builder.setContentIntent(createPendingIntent());
////        builder.setAutoCancel(true)
//        notifManager.notify(0, builder.build());


        Ring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Database.isTotalAlarm()) {

                    Database.setAlarmSound(false);
                    Database.setAlarmbVibration(false);
                    Database.setAlarmPush(false);
                    Database.setTotalAlarm(false);
                } else {
                    Database.setAlarmSound(true);
                    Database.setAlarmbVibration(true);
                    Database.setAlarmPush(true);
                    Database.setTotalAlarm(true);
                }

                editor.putString("V", String.valueOf(Database.isAlarmbVibration()));
                editor.putString("S", String.valueOf(Database.isAlarmSound()));
                editor.putString("P", String.valueOf(Database.isAlarmPush()));
                editor.putString("Total", String.valueOf(Database.isTotalAlarm()));
                editor.commit();

                int imgsrc = 0;

                if (Database.isTotalAlarm()) {
                    imgsrc = R.drawable.ic_alarm;
                } else {
                    imgsrc = R.drawable.group;
                }

                Ring.setImageResource(imgsrc);

            }
        });


    }




    boolean duplicate = false;
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
            if(collection.size()>0) {
                Iterator<Beacon> iterator = collection.iterator();
                items = new ArrayList<>();
                while (iterator.hasNext()) {
                    Beacon beacon = iterator.next();
                    String address = beacon.getBluetoothAddress();
                    double rssi = beacon.getRssi();
                    int txPower = beacon.getTxPower();
                    double distance = Double.parseDouble(decimalFormat.format(beacon.getDistance()));
                    final int light = beacon.getId2().toInt();
                    int second = beacon.getId3().toInt();

                    if(!duplicate) {

                        Item item = new Item(address, rssi, txPower, distance, light, second);
                        items.add(item);

                        int time = second;

                        if (dataManager.getWhouare() != 4) {
                            time = (int) (second * ratio[dataManager.getWhouare()]);
                        }


                        // 신호등에 메시지 전송
                        if (distance <= 5) {
                            duplicate = true;
                            SendMessage sendMessage = new SendMessage(dataManager.getBleID(), address,
                                    dataManager.getDegree(), time, light);
                            sendMessage.send();

                            final int f_time = time;

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setUi(light, f_time);
                                }
                            });
                        }

                    }
                }

                    //Toast.makeText(MainActivity.this, "신호 : "+light+" 초 : "+second, Toast.LENGTH_SHORT).show();
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {

        }
    }


    public void setUi(int light, int sec){
        allotted_time = sec*1000;
        time_left = allotted_time;

        switch (light){
            case RED:
                tv_no_signal.setVisibility(View.INVISIBLE);
                iv_no_signal_detail.setVisibility(View.INVISIBLE);
                ll_time.setVisibility(View.VISIBLE);
                tv_light.setVisibility(View.VISIBLE);
                tv_light_detail.setVisibility(View.VISIBLE);
                test_btn_sign.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_red_light,getApplicationContext().getTheme()));
                tv_light.setText(getResources().getText(R.string.str_red_light));
                tv_light_detail.setText(getResources().getText(R.string.str_red_detail));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pb_progress.setProgressTintList(getColorStateList(R.color.color_red_light));
                }
                else{
                    pb_progress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }

                tv_time_left.setText(String.valueOf(time_left/1000));

                //timer
                timer=new CountDownTimer(allotted_time,ONE_SECOND) {
                    @Override
                    public void onTick(long l) {
                        pb_progress.setProgress((int)(100*l/allotted_time));
                        if(dataManager.getSOption() && time_left<=vibrate_start && time_left>0){
                            tts.speak(tts_pattern_green[time_left/1000],TextToSpeech.QUEUE_FLUSH,null,"myUtteranceID");
                            System.out.println(tts_pattern_green[time_left/1000]);
                        }

                        tv_time_left.setText(String.valueOf(time_left/1000));
                        if(dataManager.getVOption() && vibrate_flag==VIBRATE_OFF && time_left<=vibrate_start){
                            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createWaveform(vibrate_pattern,0));
                            }
                            else{
                                vibrator.vibrate(new long[]{100,10,100,10},0);
                            }
                            vibrate_flag = VIBRATE_ON;
                        }
                        time_left-=1000;

                    }

                    @Override
                    public void onFinish() {
                        pb_progress.setProgress(0);
                        if(dataManager.getVOption()) {
                            tts.speak(tts_pattern_red[0], TextToSpeech.QUEUE_FLUSH, null, "myUtteranceID");
                        }
                        tv_time_left.setText(String.valueOf(0));
                        if(vibrator!=null) {
                            vibrator.cancel();
                        }
                        vibrate_flag = VIBRATE_OFF;
                        time_left=0;
                        test_btn_sign.setImageDrawable(getResources().
                                getDrawable(R.drawable.ic_green_light,getApplicationContext().getTheme()));
                        tv_light.setText(getResources().getText(R.string.str_green_light));
                        tv_light_detail.setText(getResources().getText(R.string.str_green_detail));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            pb_progress.setProgressTintList(getColorStateList(R.color.color_red_light));
                        }
                        duplicate = false;
                    }
                }.start();

                // tts
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status!= TextToSpeech.ERROR){
                            tts.setLanguage(Locale.KOREAN);
                        }
                    }
                });

                break;
            case GREEN:
                tv_no_signal.setVisibility(View.INVISIBLE);
                iv_no_signal_detail.setVisibility(View.INVISIBLE);
                ll_time.setVisibility(View.VISIBLE);
                tv_light.setVisibility(View.VISIBLE);
                tv_light_detail.setVisibility(View.VISIBLE);
                test_btn_sign.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_green_light,getApplicationContext().getTheme()));
                tv_light.setText(getResources().getText(R.string.str_green_light));
                tv_light_detail.setText(getResources().getText(R.string.str_green_detail));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pb_progress.setProgressTintList(getColorStateList(R.color.color_green_light));
                }

                else{
                    pb_progress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }

                tv_time_left.setText(String.valueOf(time_left/1000));

                //timer
                timer=new CountDownTimer(allotted_time,ONE_SECOND) {
                    @Override
                    public void onTick(long l) {
                        pb_progress.setProgress((int)(100*l/allotted_time));
                        if(dataManager.getSOption() && time_left<=vibrate_start && time_left>0){
                            tts.speak(tts_pattern_green[time_left/1000],TextToSpeech.QUEUE_FLUSH,null,"myUtteranceID");
                            System.out.println(tts_pattern_green[time_left/1000]);
                        }

                        tv_time_left.setText(String.valueOf(time_left/1000));
                        if(dataManager.getVOption() && vibrate_flag==VIBRATE_OFF && time_left<=vibrate_start){
                            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createWaveform(vibrate_pattern,0));
                            }
                            else{
                                vibrator.vibrate(new long[]{100,10,100,10},0);
                            }
                            vibrate_flag = VIBRATE_ON;
                        }
                        time_left-=1000;

                    }

                    @Override
                    public void onFinish() {
                        pb_progress.setProgress(0);
                        if(dataManager.getSOption()) {
                            tts.speak(tts_pattern_green[0], TextToSpeech.QUEUE_FLUSH, null, "myUtteranceID");
                        }
                        tv_time_left.setText(String.valueOf(0));
                        if(vibrator!=null) {
                            vibrator.cancel();
                        }
                        vibrate_flag = VIBRATE_OFF;
                        time_left=0;
                        test_btn_sign.setImageDrawable(getResources().
                                getDrawable(R.drawable.ic_red_light,getApplicationContext().getTheme()));
                        tv_light.setText(getResources().getText(R.string.str_red_light));
                        tv_light_detail.setText(getResources().getText(R.string.str_red_detail));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            pb_progress.setProgressTintList(getColorStateList(R.color.color_red_light));
                        }
                        else{
                            pb_progress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        }
                        duplicate = false;
                    }
                }.start();

                // tts
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status!= TextToSpeech.ERROR){
                            tts.setLanguage(Locale.KOREAN);
                        }
                    }
                });
                break;

            case NO_SIGNAL:
                test_btn_sign.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_no_signal,getApplicationContext().getTheme()));
                ll_time.setVisibility(View.INVISIBLE);
                tv_light.setVisibility(View.INVISIBLE);
                tv_light_detail.setVisibility(View.INVISIBLE);
                tv_no_signal.setVisibility(View.VISIBLE);
                iv_no_signal_detail.setVisibility(View.VISIBLE);
                break;

        }
    }


    // for test
    public void test(){
        final int mode =1;
        // 초록불모드
        test_btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUi(mode,7);
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}