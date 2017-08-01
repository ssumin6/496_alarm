package kaist.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

/**
 * Created by q on 2017-07-28.
 */

public class AddActivity extends Activity implements CompoundButton.OnCheckedChangeListener, TimePicker.OnTimeChangedListener{

    private GregorianCalendar mCalendar;
    private ImageButton addFriends ;
    private Button addMusic, saveButton;
    private int mathproblem_level, shakeit;
    private CheckBox group_allow_box;
    private TimePicker timePicker;
    private Button alarmSelector, alarmSelector2,alarmSelector3;
    private AlarmManager mManager;
    private ArrayList<Friend> group_friend;
    public MediaPlayer mMediaPlayer;

    private int  h,m;
    private int requestCode;
    private final int FRIEND_ADD =1083;
    private final int MUSIC_ADD = 2017;
    private String alarm_kind="기본알람", alarm_kind2;
    private String my_Phone;//내 핸드폰 전화번호
    private String tem;//시간 표시
    private String ring;
    private String mu;
    private String room_id;
    private PendingIntent pi;
    private boolean isGroup = false;
    private int level;
    private int cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        my_Phone = mgr.getLine1Number();

        Intent getIntent = getIntent();//requestCode is for pendingIntent
        if (getIntent != null) {
            requestCode = getIntent.getIntExtra("request_code", 0);
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        mCalendar = new GregorianCalendar();
        mCalendar.set(GregorianCalendar.SECOND, 0);
        mCalendar.set(GregorianCalendar.HOUR_OF_DAY, Calendar.HOUR);
        mCalendar.set(GregorianCalendar.MINUTE, Calendar.MINUTE);

        mManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /////////////////////////////////////////////////////////////////////////////////////////////////

        final CharSequence[] items = {"기본알람", "음성알람", "수학문제", "흔들기"};
        alarmSelector = (Button) findViewById(R.id.alarmtype);
        alarmSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AddActivity.this);
                alertBuilder.setTitle("항목중에 하나를 선택하세요.");
                // 제목셋팅
                alertBuilder.setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                if (items[id].equals("흔들기")) {
                                    // 이 함수 실행후 shakeit에 선택 결과가 저장됨.
                                    ShowDialog_shake();
                                }
                                if (items[id].equals("수학문제")) {
                                    // 이 함수 실행후 mathproblem_level에 선택 결과가 저장됨. (0,1,2)
                                    ShowDialog_mathproblem();
                                }
                                alarm_kind = (String) items[id];
                                String s = "  알람 해제 방법\n   " + (String) items[id] + "  ";
                                SpannableString ss1 = new SpannableString(s);
                                ss1.setSpan(new RelativeSizeSpan(0.7f), 11, 18, 0);
                                alarmSelector.setText(ss1);
                                dialog.cancel();
                            }
                        });
                // 다이얼로그 생성
                alertBuilder.show();
            }
        });
        ////////////////////////////////////////////////////////////////////////////////

        final CharSequence[] items2 = {"벨소리", "진동", "벨소리+진동"};
        ring = "벨소리";
        alarmSelector2 = (Button) findViewById(R.id.belltype);
        alarmSelector2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AddActivity.this);
                alertBuilder.setTitle("항목중에 하나를 선택하세요.");
                // 제목셋팅
                alertBuilder.setItems(items2,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (items2[id].equals("벨소리")){
                                    ring = "벨소리";
                                }else if (items2[id].equals("진동")) {
                                    ring = "진동";
                                }else{
                                    ring = "벨소리+진동";
                                }
                                // 프로그램을 종료한다
                                Toast.makeText(getApplicationContext(),
                                        items2[id] + " 선택했습니다.",
                                        Toast.LENGTH_SHORT).show();
                                alarm_kind2 = (String) items2[id];
                                String s = "  벨/진동 설정\n   " + (String) items2[id] + "    ";
                                SpannableString ss1 = new SpannableString(s);
                                ss1.setSpan(new RelativeSizeSpan(0.7f), 10, 19, 0);
                                alarmSelector2.setText(ss1);
                                dialog.dismiss();
                            }
                        });

                // 다이얼로그 생성
                alertBuilder.show();
            }
        });


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
                returnIntent.putExtra("alarm_type",alarm_kind);
                returnIntent.putExtra("group",isGroup);
                if (isGroup){
                    returnIntent.putExtra("room",room_id);
                }
                setResult(Activity.RESULT_OK,returnIntent);
                finish();//이 액티비티 종료. 아까 main으로 돌아감
            }
        });

        // 일시설정 클래스로 현재 시각을 설정
        alarmSelector3 = (Button) findViewById(R.id.timePickerButton);
        final TimePickerDialog tpd = new TimePickerDialog(this, TimePickerDialog.THEME_HOLO_LIGHT, listener,mCalendar.get(GregorianCalendar.HOUR_OF_DAY), mCalendar.get(GregorianCalendar.MINUTE), true);
        alarmSelector3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tpd.show();
            }
        });

        // 내장 음악 선택
        addMusic = (Button) findViewById(R.id.bellpick);
        addMusic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,MUSIC_ADD);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==FRIEND_ADD){
            if (resultCode==Activity.RESULT_OK){
                group_friend = (ArrayList<Friend>) data.getSerializableExtra("group_list");
            }
        } else if(requestCode==MUSIC_ADD){
            if(resultCode==Activity.RESULT_OK){
                Uri uriSound=data.getData();
                mu = uriSound.toString();
            }
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

    private TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            h = hourOfDay;
            m = minute;
            String s = "  시간 설정\n   " + h + "시 " + m + "분         ";
            SpannableString ss1 = new SpannableString(s);
            ss1.setSpan(new RelativeSizeSpan(0.7f),8,19,0);
            alarmSelector3.setText(ss1);
            Toast.makeText(getApplicationContext(), h + "시 " + m + "분", Toast.LENGTH_SHORT).show();
        }
    };

    // 알람 저장
    public void saveAlarm(){
        //Calendar의 값을 string으로 변경
        int hour, min;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            hour = h;
            Log.d("HOUR",""+hour);
            min = m;
        }else{
            hour = timePicker.getCurrentHour();
            min = timePicker.getCurrentMinute();
        }
        mCalendar.set(Calendar.HOUR, hour);
        mCalendar.set(Calendar.MINUTE, min);

        DateFormat format = new SimpleDateFormat("aa hh:mm");
        Date date = mCalendar.getTime();

        tem = format.format(date);

        Log.d("Time",tem);
        //여기서 pendingIntent 생성, requestCode로 무조건 생성.
        //manager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent);

        if(group_friend!= null &&group_allow_box.isChecked()&& group_friend.size()>1){
            sendGroupAlarm(alarm_kind, tem);
            isGroup = true;//group 알람인 경우 생성된다.
        }
        // spinner에 따라 다른 알람이 울림

        if (alarm_kind.equals("기본알람")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent1());
            mManager.setAlarmClock(info, pi);
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());

        } else if (alarm_kind.equals("음성알람")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent2());
            mManager.setAlarmClock(info,pi);
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());

        } else if (alarm_kind.equals("수학문제")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent3());
            mManager.setAlarmClock(info, pi);
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());
        } else if (alarm_kind.equals("흔들기")) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(mCalendar.getTimeInMillis(), pendingIntent4());
            mManager.setAlarmClock(info, pi);
            Log.i("HelloAlarmActivity", mCalendar.getTime().toString());
        }
    }

    //기능에 따라 서로 다른 pendingintent 설정
    private PendingIntent pendingIntent1() {

        Intent i = new Intent(getApplicationContext(), BasicAlarm.class);
        i.putExtra("music", mu);
        i.putExtra("ring", ring);
        i.putExtra("group",isGroup);
        i.putExtra("phone",my_Phone);
        try{
            JSONObject json = new JSONObject(room_id);
            room_id = (String)json.get("m");
            Log.d("WHOAREYOU", room_id);
        }catch(Exception e){
            e.printStackTrace();
            room_id = null;
        }
        i.putExtra("room", room_id);
        pi = PendingIntent.getActivity(this, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    private PendingIntent pendingIntent2() {
        Intent i = new Intent(getApplicationContext(), AudioAlarm.class);
        i.putExtra("music", mu);
        i.putExtra("ring", ring);
        pi = PendingIntent.getActivity(this, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    private PendingIntent pendingIntent3() {
        Intent i = new Intent(getApplicationContext(), MathAlarm.class);
        i.putExtra("music", mu);
        i.putExtra("ring", ring);
        i.putExtra("level", level);
        Log.d("왜그러니2", ""+level);
        pi = PendingIntent.getActivity(this, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    private PendingIntent pendingIntent4() {
        Intent i = new Intent(getApplicationContext(), SensitiveAlarm.class);
        i.putExtra("music", mu);
        i.putExtra("ring", ring);
        i.putExtra("cnt", cnt);
        pi = PendingIntent.getActivity(this, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
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
        try {
            room_id = new NetworkTask().execute(url, alarm_type, calToTime).get();
        }catch(Exception e){
            e.printStackTrace();
        }
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
                    temp.put("wakeup",false);
                    userlist.put(temp);
                }
                values.put("userlist",userlist);

                my_Phone = my_Phone.replace("+82","0");
                values.put("manager",my_Phone);

                }catch(JSONException e){
                e.printStackTrace();
            }
            Log.d("serverConnection","JSONObject"+values.toString());
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            try{
                result = requestHttpURLConnection.request(url, values);
                room_id= result;
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
    public void ShowDialog_mathproblem() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(2);
        popDialog.setTitle("난이도를 선택하세요");
        popDialog.setMessage("하                                  중                                  상");
        popDialog.setView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Do something here with new value
                level = progress;
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }

    public void ShowDialog_shake() {
        final AlertDialog.Builder popDialog2 = new AlertDialog.Builder(this);
        final SeekBar seek2 = new SeekBar(this);
        seek2.setMax(20);
        popDialog2.setTitle("횟수를 선택하세요");
        popDialog2.setMessage("5                     10                     15                     20");
        popDialog2.setView(seek2);
        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Do something here with new value
                cnt = progress + 5;

            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        popDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog2.create();
        popDialog2.show();
    }
}
