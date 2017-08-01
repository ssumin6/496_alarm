package kaist.alarm;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * Created by q on 2017-07-31.
 */

public class SensitiveAlarm extends AppCompatActivity implements SensorEventListener{

    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    public MediaPlayer mMediaPlayer;

    // 맴버변수 (마지막과 현재값을 비교하여 변위를 계산하는 방식)
    private long         m_lLastTime;
    private float        m_fSpeed;
    private float        m_fCurX,  m_fCurY,  m_fCurZ;
    private float        m_fLastX,  m_fLastY,  m_fLastZ;

    // 임계값 설정
    private static final int  SHAKE_THRESHOLD = 1300;

    // 매니저 객체
    private SensorManager    m_senMng;
    private Sensor m_senAccelerometer;

    int COUNT;
    int count = 0;
    TextView txv;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensitive_alarm);

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

        // 시스템 서비스에서 센서메니져 획득
        m_senMng = (SensorManager)getSystemService(SENSOR_SERVICE);

        // TYPE_ACCELEROMETER의 기본 센서객체를 획득
        m_senAccelerometer = m_senMng.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        txv = (TextView) findViewById(R.id.text1);

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

    // 흔들기가 시작되면 호출되는 함수
    public void onStart()
    {
        super.onStart();
        if(m_senAccelerometer != null)
            m_senMng.registerListener(this, m_senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    // 흔들기가 끝나면 호출되는 함수
    public void onStop()
    {
        super.onStop();
        if(m_senMng != null)
            m_senMng.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            long lCurTime  = System.currentTimeMillis();
            long lGabOfTime  = lCurTime - m_lLastTime;

            // 0.1초보다 오래되면 다음을 수행 (100ms)
            if(lGabOfTime > 300)
            {
                m_lLastTime = lCurTime;

                m_fCurX = event.values[SensorManager.DATA_X];
                m_fCurY = event.values[SensorManager.DATA_Y];
                m_fCurZ = event.values[SensorManager.DATA_Z];

                //  변위의 절대값에  / lGabOfTime * 10000 하여 스피드 계산
                m_fSpeed = Math.abs(m_fCurX + m_fCurY + m_fCurZ - m_fLastX - m_fLastY - m_fLastZ) / lGabOfTime * 10000;

                // 임계값보다 크게 움직였을 경우 다음을 수행
                if(m_fSpeed > SHAKE_THRESHOLD)
                {
                    if(count <= COUNT) {
                        Log.i("kmsTest", "Shake");
                        count ++;
                        txv.setText(Integer.toString(count)+"번 흔들어!" + "<br />" + "<br />" + Integer.toString(count)+"번 : 계속 흔들어!");
                    } else{
                        Log.i("엥벌써나옴?","?");
                        finish();
                    }
                }

                // 마지막 위치 저장
                // m_fLastX = event.values[0]; 그냥 배열의 0번 인덱스가 X값
                m_fLastX = event.values[SensorManager.DATA_X];
                m_fLastY = event.values[SensorManager.DATA_Y];
                m_fLastZ = event.values[SensorManager.DATA_Z];
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
