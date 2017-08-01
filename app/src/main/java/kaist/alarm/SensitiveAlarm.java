package kaist.alarm;

import android.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
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
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;


/**
 * Created by q on 2017-07-31.
 */

public class SensitiveAlarm extends Activity implements SensorEventListener{

    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    public MediaPlayer mMediaPlayer;

    Uri mu;
    String musicType;
    String ringType;
    Vibrator vibrator;

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

        Intent intent = getIntent();
        musicType = intent.getStringExtra("music");
        ringType = intent.getStringExtra("ring");
        COUNT = intent.getIntExtra("cnt", 0);
        Log.d("숫자", ""+COUNT);

        if (ringType.equals("벨소리")) {
            musicSelect();
        } else if(ringType.equals("진동")){
            vibrate();
        } else{
            musicSelect();
            vibrate();
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
                    if(count < COUNT) {
                        Log.i("kmsTest", "Shake");
                        count ++;
                        txv.setText(Integer.toString(COUNT)+"번 흔들어!\n\n" + Integer.toString(count)+"번 : 계속 흔들어!");
                    } else{
                        Log.i("엥벌써나옴?","?");
                        resetAlarm();
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

}
