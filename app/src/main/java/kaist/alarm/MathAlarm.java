package kaist.alarm;

/**
 * Created by q on 2017-07-31.
 */

import android.app.ActivityManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static kaist.alarm.AddActivity.mMediaPlayer;

/**
 * Created by q on 2017-07-29.
 */

public class MathAlarm extends AppCompatActivity {

    TextView txv, txv2, txv3;
    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    int num1, num2, ans;


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

        Button c = (Button) findViewById(R.id.reset2);
        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetAlarm();
            }
        });

        Button d = (Button) findViewById(R.id.send);
        d.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Integer.parseInt(edittext.getText().toString()) == ans){
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    finish();
                }else{
                    txv2.setText("false!");
                }
            }
        });


        if (mMediaPlayer == null){
            mMediaPlayer = MediaPlayer.create(this, R.raw.guitar);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } else {
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.start();
                }
            });
            mMediaPlayer.prepareAsync();
        }

        double randomValue = Math.random();
        double randomValue2 = Math.random();
        num1 = (int)(randomValue*100) + 1;
        num2 = (int)(randomValue2*100) + 1;
        ans =  num1 + num2;

        txv3.setText(num1 +" + "+ num2);

    }



    // 알람의 해제
    private void resetAlarm(){
        mMediaPlayer.stop();
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


}
