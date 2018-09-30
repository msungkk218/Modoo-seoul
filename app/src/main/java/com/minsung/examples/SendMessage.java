package com.minsung.examples;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by minsung on 2018-09-30.
 */

public class SendMessage {
    private String myId;
    private String yourId;
    private String degree;
    private int time;
    private int light;

    SendMessage(String myId,String yourId,String degree,int time, int light){
        this.myId = myId;
        this.yourId = yourId;
        this.degree = degree;
        this.time = time;
        this.light = light;
    }

    public void send(){
        SendMsgTask sendMsgTask = new SendMsgTask();
        sendMsgTask.execute(Server.SEND_URL);
    }

    public class SendMsgTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            RequestBody body = new FormBody.Builder()
                    .add("myId",myId)
                    .add("yourId",yourId)
                    .add("degree", degree)
                    .add("time", String.valueOf(time))
                    .add("light", String.valueOf(light))
                    .build();

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try {
                Response response=client.newCall(request).execute();

                return response.body().string();

            } catch (IOException e) {
                Log.d("updatePush",e.getMessage());

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("Y")){
                System.out.println("push 전송 완료");
            }
        }
    }
}
