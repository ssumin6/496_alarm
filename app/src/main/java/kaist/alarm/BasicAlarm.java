package kaist.alarm;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;


/**
 * Created by q on 2017-07-31.
 */

public class BasicAlarm extends Activity {

    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    public MediaPlayer mMediaPlayer;

    Uri mu;
    String musicType;
    String ringType;
    String phone;
    String room;
    boolean isGroup;
    Vibrator vibrator;

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
                if(isGroup&& room!= null){
                    String url = "http://52.79.200.191:8080/room_user/"+room+"&&"+phone;
                    Log.d("WHOAREYOU", url);
                    new NetworkTask2().execute(url,"POST");
                    ALLWAKEUP();
                }else{
                    resetAlarm();
                }
            }
        });


        Intent intent = getIntent();
        musicType = intent.getStringExtra("music");
        ringType = intent.getStringExtra("ring");
        isGroup = intent.getBooleanExtra("group",false);
        phone = intent.getStringExtra("phone");
        room = intent.getStringExtra("room");

        if (ringType.equals("벨소리")) {
            musicSelect();
        } else if(ringType.equals("진동")){
            vibrate();
        } else{
            musicSelect();
            vibrate();
        }
    }

    private void resetAlarm(){
        if(mMediaPlayer!=null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        if(vibrator != null) {
            vibrator.cancel();
        }
        finish();
    }

    private void musicSelect(){
        if (musicType != null) {
            mu = Uri.parse(musicType);
        } else {

        }

        final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        mMediaPlayer = new MediaPlayer();
        if (mu != null) {
            try {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    mMediaPlayer.setDataSource(context, mu);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            mp.start();
                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                            {
                                @Override
                                public void onCompletion(MediaPlayer mp)
                                {
                                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                                }
                            });

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
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                }
            });
        }

    }
    private void vibrate(){

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{500, 2000},0);

    }
    private void ALLWAKEUP(){
        String url = "http://52.79.200.191:8080/room_wakeup/"+room;
        try {
            String xrxr = new NetworkTask2().execute(url, "GET").get();
            JSONObject json = new JSONObject(xrxr);
            String who = (String)json.get("message");
            if (who.equals("all wake up")){
                resetAlarm();
            }else{
                Toast.makeText(getApplicationContext(), who+"\n 가 일어나지 않아서 알람을 끌 수 없습니다.", Toast.LENGTH_SHORT).show();

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    class NetworkTask2 extends AsyncTask<String, Void, String> {

        private String url;

        @Override
        protected String doInBackground(String... params){
            Log.d("serverConnection","doInBackground()");
            url = params[0];
            Log.d("WHOAREYOU", url);
            Log.d("serverConnection","NetworkTask2 in BASICALARMG.class");
            String result;
            RequestHttpGeneral requestHttpURLConnection = new RequestHttpGeneral();
            try{
                result = requestHttpURLConnection.request(url, params[1]);
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
