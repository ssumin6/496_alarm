package kaist.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by q on 2017-07-28.
 */

public class AddActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TimePicker.OnTimeChangedListener{

    private GregorianCalendar mCalendar;
    private ImageButton addFriends;
    private Button addMusic;
    private Button saveButton;
    private int mathproblem_level, shakeit;
    private CheckBox group_allow_box;
    private TimePicker timePicker;
    private Button alarmSelector, alarmSelector2,alarmSelector3;
    private AlarmManager mManager;
    private ArrayList<Friend> group_friend;
    public static MediaPlayer mMediaPlayer;
    private PendingIntent pi;

    private int  h=12,m=12;
    private int requestCode;
    private final int FRIEND_ADD =1083;
    private final int MUSIC_ADD = 2017;
    private String alarm_kind="기본알람", alarm_kind2;
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

        mCalendar = new GregorianCalendar();
        mCalendar.set(GregorianCalendar.SECOND, 0);

        mManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        /////////////////////////////////////////////////////////////////////////////////////////////////

        final CharSequence[] items = { "기본알람", "음성알람", "수학문제", "흔들기" };
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

                                if(items[id].equals("흔들기")){
                                    // 이 함수 실행후 shakeit에 선택 결과가 저장됨.
                                    ShowDialog_shake();
                                }
                                if(items[id].equals("수학문제")){
                                    // 이 함수 실행후 mathproblem_level에 선택 결과가 저장됨. (0,1,2)
                                    ShowDialog_mathproblem();
                                }
                                alarm_kind = (String) items[id];
                                String s = "  알람 해제 방법\n   " + (String) items[id]+"  ";
                                SpannableString ss1 = new SpannableString(s);
                                ss1.setSpan(new RelativeSizeSpan(0.7f),11,18,0);
                                alarmSelector.setText(ss1);
                                dialog.cancel();
                            }
                        });
                // 다이얼로그 생성
                alertBuilder.show();
            }
        });
        ////////////////////////////////////////////////////////////////////////////////

        final CharSequence[] items2 = { "벨", "진동", "벨/진동"};
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

                                // 프로그램을 종료한다
                                Toast.makeText(getApplicationContext(),
                                        items2[id] + " 선택했습니다.",
                                        Toast.LENGTH_SHORT).show();
                                alarm_kind2 = (String) items2[id];
                                String s = "  벨/진동 설정\n   " +(String) items2[id]+"    ";
                                SpannableString ss1 = new SpannableString(s);
                                ss1.setSpan(new RelativeSizeSpan(0.7f),10,18,0);
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
                setResult(Activity.RESULT_OK,returnIntent);
                finish();//이 액티비티 종료. 아까 main으로 돌아감
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 일시설정 클래스로 현재 시각을 설정
        alarmSelector3 = (Button) findViewById(R.id.timePickerButton);
        final TimePickerDialog tpd = new TimePickerDialog(this, listener, mCalendar.HOUR_OF_DAY, mCalendar.MINUTE, true);
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
        /////////////////////////////////////////////////////////////////////////////////

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

        Log.d("WHOAREYOU",mCalendar.toString());

        DateFormat format = new SimpleDateFormat("aa hh:mm");
        Date date = mCalendar.getTime();

        tem = format.format(date);

        Log.d("Time",tem);
        //여기서 pendingIntent 생성, requestCode로 무조건 생성.

        if(group_friend!= null &&group_allow_box.isChecked()&& group_friend.size()>1){
            sendGroupAlarm(alarm_kind, tem);//group 알람인 경우 생성된다.
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
        pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    private PendingIntent pendingIntent2() {
        Intent i = new Intent(getApplicationContext(), AudioAlarm.class);
        pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    private PendingIntent pendingIntent3() {
        Intent i = new Intent(getApplicationContext(), MathAlarm.class);
        pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    private PendingIntent pendingIntent4() {
        Intent i = new Intent(getApplicationContext(), SensitiveAlarm.class);
        pi = PendingIntent.getActivity(this, requestCode, i, 0);
        return pi;
    }

    // 시각 설정 클래스의 상태변화 리스너
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
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
    public void ShowDialog_mathproblem()
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(2);
        popDialog.setTitle("난이도를 선택하세요");
        popDialog.setMessage("하                           중                           상");
        popDialog.setView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Do something here with new value
                mathproblem_level = progress;
            }
            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }
    public void ShowDialog_shake()
    {
        final AlertDialog.Builder popDialog2 = new AlertDialog.Builder(this);
        final SeekBar seek2 = new SeekBar(this);
        seek2.setMax(20);
        popDialog2.setTitle("횟수를 선택하세요");
        popDialog2.setMessage("0            5            10           15            20");
        popDialog2.setView(seek2);
        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Do something here with new value
                shakeit = progress;

            }
            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        popDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                });
        popDialog2.create();
        popDialog2.show();
    }
}
