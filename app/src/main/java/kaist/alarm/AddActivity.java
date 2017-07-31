package kaist.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.widget.TimePicker.OnTimeChangedListener;

/**
 * Created by q on 2017-07-28.
 */

public class AddActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, OnTimeChangedListener {

    private Calendar mCalendar;
    private ImageButton addFriends, addMusic;
    private Button saveButton;
    private CheckBox group_allow_box;
    private TimePicker timePicker;
    private Spinner alarmSelector, alarmSelector2;
    private AlarmManager mManager;
    private ArrayList<Friend> group_friend;
    public static MediaPlayer mMediaPlayer;

    private int requestCode;
    private final int FRIEND_ADD =1083;
    private String alarm_kind, alarm_kind2;
    private String my_Phone;//내 핸드폰 전화번호
    private String tem;//시간 표시

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        my_Phone = mgr.getLine1Number();

        Intent getIntent = getIntent();//requestCode is for pendingIntent
        if (getIntent!= null){
            requestCode = getIntent.getIntExtra("request_code",0);
        }else{
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        // 캘린더 설정
        mCalendar = new GregorianCalendar();
        mCalendar.set(GregorianCalendar.SECOND, 0);

        // 알람매니저
        mManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // 기능 알람 선택
        alarmSelector = (Spinner)findViewById(R.id.spinner);
        alarmSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                alarm_kind = parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> parent){
                alarm_kind = null;
            }
        });

        // 벨/진동 선택
        alarmSelector2 = (Spinner) findViewById(R.id.spinner2);
        alarmSelector2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                alarm_kind2 = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                alarm_kind2 = null;
            }
        });

        // 단체 알람 선택
        addFriends = (ImageButton)findViewById(R.id.imageButton);
        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FriendAddActivity.class);
                startActivityForResult(intent,FRIEND_ADD);
            }
        });
        addFriends.setEnabled(false);

        group_allow_box = (CheckBox)findViewById(R.id.checkBox);
        group_allow_box.setOnCheckedChangeListener(this);

        // 알람 생성
        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarm();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("time_text",tem);
                returnIntent.putExtra("alarm_request_code",requestCode);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();//이 액티비티 종료. 아까 main으로 돌아감
            }
        });

        // 일시설정 클래스로 현재 시각을 설정
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setHour(mCalendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(mCalendar.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(this);

        // 내장 음악 선택
        addMusic = (ImageButton) findViewById(R.id.imageButton2);
        addMusic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,10);
            }
        });

    }

    // 서버 or 음악
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==FRIEND_ADD){
            if (resultCode==Activity.RESULT_OK){
                group_friend = (ArrayList<Friend>) data.getSerializableExtra("group_list");
            }
        } else if(requestCode==10){
            if(resultCode==Activity.RESULT_OK){
                Uri uriSound=data.getData();
                getMusic(this, uriSound);
            }
        }
    }

    // 내장미디어에서 음악 얻어오기
    private void getMusic(Context context, Uri uri) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, uri);
            Log.d("재생중?", "*************************제발*******************");
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonview, boolean isChecked){
        if (group_allow_box.isChecked()){
            addFriends.setEnabled(true);
        }else{
            addFriends.setEnabled(false);
        }
    }

    // 알람 저장
    public void saveAlarm(){
        mCalendar = Calendar.getInstance();
        int hour, min;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            hour = timePicker.getHour();
            Log.d("HOUR",""+hour);
            min = timePicker.getMinute();
        }else{
            hour = timePicker.getCurrentHour();
            min = timePicker.getCurrentMinute();
        }
        mCalendar.set(Calendar.HOUR, hour);
        mCalendar.set(Calendar.MINUTE, min);
        //Calendar의 값을 string으로 변경
        if (hour > 12){
            tem = "PM";
            hour -= 12;
        }else{
            tem = "AM";
        }
        if (hour <10){
            tem = tem +" 0"+hour+":";
        }else{
            tem = tem +" "+hour+":";
        }
        if (min<10){
            tem = tem +"0"+min;
        }else{
            tem = tem +min;
        }

        Log.d("Time",tem);
        //여기서 pendingIntent 생성, requestCode로 무조건 생성.
        //manager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent);

        if(group_friend!= null &&group_allow_box.isChecked()&& group_friend.size()>1){
            sendGroupAlarm(alarm_kind, mCalendar.toString());//group 알람인 경우 생성된다.
        }

        // spinner에 따라 다른 알람이 울림

        if (alarm_kind.equals("기본알람")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent1());
            mManager.setAlarmClock(info, pendingIntent1());
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());

        } else if (alarm_kind.equals("음성알람")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent2());
            mManager.setAlarmClock(info, pendingIntent2());
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());

        } else if (alarm_kind.equals("수학문제")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent3());
            mManager.setAlarmClock(info, pendingIntent3());
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());
        } else if (alarm_kind.equals("흔들기")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent4());
            mManager.setAlarmClock(info, pendingIntent4());
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());
        }
    }


    //기능에 따라 서로 다른 pendingintent 설정
    private PendingIntent pendingIntent1() {
        Intent i = new Intent(getApplicationContext(), BasicAlarm.class);
        PendingIntent pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    private PendingIntent pendingIntent2() {
        Intent i = new Intent(getApplicationContext(), AudioAlarm.class);
        PendingIntent pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    private PendingIntent pendingIntent3() {
        Intent i = new Intent(getApplicationContext(), MathAlarm.class);
        PendingIntent pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    private PendingIntent pendingIntent4() {
        Intent i = new Intent(getApplicationContext(), SensitiveAlarm.class);
        PendingIntent pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    // 시각 설정 클래스의 상태변화 리스너
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        Log.i("HelloAlarmActivity", mCalendar.getTime().toString());
    }

    private void sendGroupAlarm(String alarm_type, String calToTime){
        String url = "http://52.79.200.191:8080/room";
        new NetworkTask().execute(url, alarm_type, calToTime);
    }


    class NetworkTask extends AsyncTask<String, Void, String> {

        private String url;

        @Override
        protected String doInBackground(String... params){
            Log.d("serverConnection","doInBackground()");
            url = params[0];
            JSONObject values = new JSONObject();
            try{
                for (int i=1; i<params.length;i++){
                    if (i==1){
                        values.put("alarmtype",params[i]);
                    }if (i==2){
                        values.put("time",params[i]);
                    }
                }
                JSONArray userlist = new JSONArray();
                for (int i=0; i<group_friend.size(); i++){
                    JSONObject temp = new JSONObject();
                    temp.put("nickname",group_friend.get(i).getName());
                    temp.put("thumbnail",group_friend.get(i).getThumbImage());
                    temp.put("tokenid",group_friend.get(i).getToken());
                    temp.put("phonenumber",group_friend.get(i).getPhonenumber());
                    userlist.put(temp);
                }
                values.put("userlist",userlist);

                values.put("manager",my_Phone);

                }catch(JSONException e){
                e.printStackTrace();
            }
            Log.d("serverConnection","JSONObject"+values.toString());
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            try{
                result = requestHttpURLConnection.request(url, values);
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

