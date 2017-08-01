package kaist.alarm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;


/**
 * Created by q on 2017-07-31.
 */

public class BasicAlarmG extends AppCompatActivity{

    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    public MediaPlayer mMediaPlayer;
    private String room;
    private String phone;

    Uri mu;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_alarm);

        // 휴대폰 작동 상태
        if (sCpuWakeLock != null) {
            return;
        }

        // 잠근 화면 위로 activity 실행
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON // 스크린을 켜진 상태로 유지
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD // Keyguard를 해지
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON // 스크린 켜기
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED); // 잠긴 화면 위로 실행

        // 잠든 화면 깨우기
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "hi");

        sCpuWakeLock.acquire();

        // 릴리즈
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }

        Button c = (Button) findViewById(R.id.reset2);
        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AWAKEME();
                resetAlarm();
            }
        });


        Intent intent = getIntent();
        String musicType = intent.getStringExtra("music");
        room = intent.getStringExtra("room");
        phone = intent.getStringExtra("phone");
        if (musicType != null) {
            mu = Uri.parse(musicType);
        } else{

        }

        mMediaPlayer = new MediaPlayer();
        if (mu != null) {
            try {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    }
                    else{
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                else{
                    mMediaPlayer.setDataSource(context, mu);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            mp.start();

                        }
                    });
                    mMediaPlayer.prepareAsync();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            mMediaPlayer = MediaPlayer.create(this, R.raw.guitar);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }

    }
    private void AWAKEME(){
        String url = "http://52.79.200.191:8080/room_user";
        new NetworkTask2().execute(url,room, phone);
    }

    private void resetAlarm(){
        mMediaPlayer.stop();
        mMediaPlayer.release();
        finish();
    }
    class NetworkTask2 extends AsyncTask<String, Void, String> {

        private String url;

        @Override
        protected String doInBackground(String... params){
            Log.d("serverConnection","doInBackground()");
            url = params[0];
            url = url+"/"+params[1]+"&&"+params[2];
            Log.d("WHOAREYOU", url);
            Log.d("serverConnection","NetworkTask2 in BASICALARMG.class");
            String result;
            RequestHttpGeneral requestHttpURLConnection = new RequestHttpGeneral();
            try{
                result = requestHttpURLConnection.request(url, "POST");
                return result;
            }catch(IOException e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected  void onPreExecute(){super.onPreExecute();}

        @Override
        protected  void onPostExecute(String str){super.onPostExecute(str);}
    }
}
