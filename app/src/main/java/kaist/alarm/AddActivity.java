package kaist.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import java.util.TimeZone;

/**
 * Created by q on 2017-07-28.
 */

public class AddActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    private Calendar mCalendar;
    private ImageButton addFriends;
    private Button saveButton;
    private CheckBox group_allow_box;
    private TimePicker timePicker;
    private Spinner alarmSelector;
    private AlarmManager manager;
    private ArrayList<Friend> group_friend;

    private int requestCode;
    private final int FRIEND_ADD =1083;
    private String alarm_kind;
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

        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

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
        timePicker = (TimePicker)findViewById(R.id.timePicker);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==FRIEND_ADD){
            if (resultCode==Activity.RESULT_OK){
                group_friend = (ArrayList<Friend>) data.getSerializableExtra("group_list");
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
