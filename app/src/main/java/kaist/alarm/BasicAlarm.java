package kaist.alarm;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import static kaist.alarm.AddActivity.mMediaPlayer;

/**
 * Created by q on 2017-07-31.
 */

public class BasicAlarm extends AppCompatActivity{

    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState){

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
                resetAlarm();
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

    }

    private void resetAlarm(){
        mMediaPlayer.stop();
        mMediaPlayer.release();
        finish();
    }
}
