package kaist.alarm;

/**
 * Created by q on 2017-07-31.
 */


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Created by q on 2017-07-29.
 */

public class AudioAlarm extends AppCompatActivity {

    TextView txv, txv2, txv3;
    String rm, rm2;
    final int REQUEST_AUDIO = 1;
    SpeechRecognizer mRecognizer;
    Intent i;
    private static PowerManager.WakeLock sCpuWakeLock;
    Context context = this;
    int pos; // 재생 멈춘 시점
    ArrayList<String> list;
    int j;
    public MediaPlayer mMediaPlayer;

    Uri mu;
    String musicType;
    String ringType;
    Vibrator vibrator;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_alarm);

        final Activity activity_c = this;
        Button ib = (Button) findViewById(R.id.mike);
        txv2 = (TextView) findViewById(R.id.answer2);
        txv3 = (TextView) findViewById(R.id.question);

        String json = parseJSON(); // JSON 파일 가져오기

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

        // 음성인식 기능
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        // 말하기
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(activity_c, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity_c, Manifest.permission.RECORD_AUDIO)){

                    }
                    else{
                        ActivityCompat.requestPermissions(activity_c, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                    }
                }
                else{
                    Log.i("왜", "안꺼ㅏ쪄*************");
                    pos = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.pause();
                    mRecognizer.startListening(i);
                    Log.i("왜2", "안꺼ㅏ쪄*************");
                }
            }
        });



        Intent intent = getIntent();
        musicType = intent.getStringExtra("music");
        ringType = intent.getStringExtra("ring");
        if (ringType.equals("벨소리")) {
            musicSelect();
        } else if(ringType.equals("진동")){
            vibrate();
        } else{
            musicSelect();
            vibrate();
        }

        try{
            JSONArray jarray = new JSONArray(json); //JSONArray 생성
            list = new ArrayList<String>(jarray.length());
            for(int i =0; i<jarray.length(); i++){
                list.add(jarray.getJSONObject(i).getString("writing"));
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        double randomValue = Math.random();
        j = (int)(randomValue*7);
        txv3.setText(list.get(j).toString());

        Log.d("뭐지", list.get(j).toString());
    }

    // 음성인식 permission
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case REQUEST_AUDIO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mRecognizer.startListening(i);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Audio Permission denied",Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    // 음성인식 listener
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }
        @Override
        public void onBeginningOfSpeech() {
        }
        @Override
        public void onRmsChanged(float rmsdB) {
        }
        @Override
        public void onBufferReceived(byte[] buffer) {
        }
        @Override
        public void onEndOfSpeech() {
        }
        @Override
        public void onError(int error) {
            if(error == mRecognizer.ERROR_SPEECH_TIMEOUT){
                Toast.makeText(getApplicationContext(),"아무 음성도 듣지 못함",Toast.LENGTH_SHORT).show();
                mMediaPlayer.setLooping(true);
                mMediaPlayer.seekTo(pos);
                mMediaPlayer.start();
            }
            else if(error == mRecognizer.ERROR_NO_MATCH){
                Toast.makeText(getApplicationContext(),"적당한 결과를 찾지 못함",Toast.LENGTH_SHORT).show();
                mMediaPlayer.setLooping(true);
                mMediaPlayer.seekTo(pos);
                mMediaPlayer.start();
            }
            else if(error == mRecognizer.ERROR_RECOGNIZER_BUSY){
                Toast.makeText(getApplicationContext(),"인스턴스가 바쁨",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onResults(Bundle results) {

            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            txv2.setText(""+rs[0]);
            rm = rs[0].replaceAll(" ","");
            rm2 = list.get(j).toString().replaceAll(" ", "");
            if (rm.equals(rm2)){
                resetAlarm();
            }
            else{
                mMediaPlayer.setLooping(true);
                mMediaPlayer.seekTo(pos);
                mMediaPlayer.start();
            }

        }
        @Override
        public void onPartialResults(Bundle partialResults) {
        }
        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

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

    public String parseJSON() {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open("wise_saying.json"); // contact.json file에서 pasring
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
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
