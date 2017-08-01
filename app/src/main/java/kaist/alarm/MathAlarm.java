package kaist.alarm;

/**
 * Created by q on 2017-07-31.
 */

import android.*;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;


/**
 * Created by q on 2017-07-29.
 */

public class MathAlarm extends AppCompatActivity {

    TextView txv, txv2, txv3;
    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    int num1, num2, num3, ans;
    public MediaPlayer mMediaPlayer;
    int level;

    Uri mu;
    String musicType;
    String ringType;
    Vibrator vibrator;


    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.math_alarm);

        txv2 = (TextView) findViewById(R.id.answer2);
        txv3 = (TextView) findViewById(R.id.question);
        final EditText edittext = (EditText) findViewById(R.id.edittext);

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

        Button d = (Button) findViewById(R.id.send);
        d.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Integer.parseInt(edittext.getText().toString()) == ans){
                    resetAlarm();
                }else{
                    txv2.setText("틀려부렀어!");
                }
            }
        });


        Intent intent = getIntent();
        musicType = intent.getStringExtra("music");
        ringType = intent.getStringExtra("ring");
        level = intent.getIntExtra("level", 2);
        Log.d("레벨 받았니?", ""+level);

        if (ringType.equals("벨소리")) {
            musicSelect();
        } else if(ringType.equals("진동")){
            vibrate();
        } else{
            musicSelect();
            vibrate();
        }

        double randomValue = Math.random();
        double randomValue2 = Math.random();
        double randomValue3 = Math.random();

        if(level == 2) {
            num1 = (int) (randomValue * 100) + 1;
            num2 = (int) (randomValue2 * 100) + 1;
            num3 = (int) (randomValue3 * 9) + 2;
            ans = (num1 + num2) * num3;

            txv3.setText(" ( " + num1 + " + " + num2 + " ) " + " * " + num3);

        } else if(level == 1){
            num1 = (int) (randomValue * 80) + 21;
            num2 = (int) (randomValue2 * 80) + 21;
            ans = num1 + num2;

            txv3.setText(num1 + " + " + num2);
        } else if(level == 0){
            num1 = (int) (randomValue * 20) + 1;
            num2 = (int) (randomValue2 * 20) + 1;
            ans = num1 + num2;

            txv3.setText(num1 + " + " + num2);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
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

    private void vibrate(){

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{500, 2000},0);

    }



}
