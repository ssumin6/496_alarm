package kaist.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    public RecyclerViewAdapter mAdapter;
    private final int ALARM_ADD_SUCCESS = 414;
    private int alarm_request_code = 0;
    private String nickname;
    private String thumbnail;
    private String phonenumber;
    private String dirPath;
    private String room_id;
    private String alarm_type;
    private String time;
    private AlarmManager mManager;
    private boolean isClicked = false;
    // 뀨뀨뀨뀨뀨뀨뀨뀨뀨뀨뀨뀨
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String dirPath = getFilesDir().getAbsolutePath();
        File file = new File(dirPath);

        ArrayList<Alarm> toPut= new ArrayList<>();
        // 일치하는 폴더가 없으면 생성
        if( !file.exists() ) {
            file.mkdirs();
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }else {
            // 파일 내용 읽어오기
            String str = "test2.txt";
            Log.d("FILE",str.toString());
            String loadPath = dirPath + "/" + str;
            try {
                FileInputStream fis = new FileInputStream(loadPath);
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
                String temp;
                int last;
                while ((temp = bufferReader.readLine()) != null) {
                    String[] tokens= temp.split("&");
                    int sese = Integer.parseInt(tokens[2]);
                    last = sese;
                    boolean what= Boolean.parseBoolean(tokens[1]);
                    Alarm one = new Alarm(sese,tokens[0]);
                    one.setOpen(what);
                    toPut.add(one);
                alarm_request_code = last+1;
                Log.v(null, "" + toPut.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new RecyclerViewAdapter(getApplicationContext(), toPut);
        mRecyclerView.setAdapter(mAdapter);

        mManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter, getApplicationContext()));
        helper.attachToRecyclerView(mRecyclerView);

        FloatingActionButton mAdd = (FloatingActionButton)findViewById(R.id.floatingActionButton3);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClicked){
                    isClicked = true;
                    Intent addIntent = new Intent(getApplicationContext(), AddActivity.class);
                    addIntent.putExtra("request_code",alarm_request_code);
                    alarm_request_code +=1;
                    startActivityForResult(addIntent,ALARM_ADD_SUCCESS);
                    isClicked= false;
                }
            }
        });

        Intent kakao = getIntent();
        nickname= kakao.getStringExtra("nickname");
        thumbnail = kakao.getStringExtra("thumbnail");
        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        phonenumber = mgr.getLine1Number();
        phonenumber = phonenumber.replace("+82","0");
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("token",token);
        ServerConnection(nickname, thumbnail, phonenumber,token);

        //setting in order to get intent.
        int setting = -1;
        room_id = null;
        time="";
        alarm_type="";
        String message="";


        Intent alertss = getIntent();
        if (alertss != null){
            setting = alertss.getIntExtra("AlertSET",0);//1인경우 initial setting, 3인 경우 modified by manager.
            if (setting!= 0){
                room_id = alertss.getStringExtra("room_id");
                message=alertss.getStringExtra("message");
                alarm_type = alertss.getStringExtra("alarm_type");
                time = alertss.getStringExtra("time");
            }
        }

        if(setting==1) {//alert Dialog
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
            alert_confirm.setMessage("알람을 수락하시겠습니까?\n"+message).setCancelable(false).setPositiveButton("수락",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'YES'
                            //알람매니저로 세팅도 해야함
                            SimpleDateFormat sdf = new SimpleDateFormat("aa hh:mm");
                            try {
                                Date date = sdf.parse(time);
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);
                                PendingIntent pi;
                                Intent i;

                                if (alarm_type.equals("기본알람")) {
                                    i= new Intent(getApplicationContext(), BasicAlarm.class);
                                    pi = PendingIntent.getActivity(getApplicationContext(), alarm_request_code,i, 0);
                                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), pi);
                                    mManager.setAlarmClock(info, pi);
                                    Log.i("HelloAlarmActivity", cal.getTime().toString());

                                } else if (alarm_type.equals("음성알람")) {
                                    i= new Intent(getApplicationContext(), AudioAlarm.class);
                                    pi = PendingIntent.getActivity(getApplicationContext(), alarm_request_code,i, 0);
                                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(),pi);
                                    mManager.setAlarmClock(info, pi);
                                    Log.i("HelloAlarmActivity", cal.getTime().toString());

                                } else if (alarm_type.equals("수학문제")) {
                                    i= new Intent(getApplicationContext(), MathAlarm.class);
                                    pi = PendingIntent.getActivity(getApplicationContext(), alarm_request_code,i, 0);
                                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), pi);
                                    mManager.setAlarmClock(info, pi);
                                    Log.i("HelloAlarmActivity", cal.getTime().toString());
                                } else if (alarm_type.equals("흔들기")) {
                                    i= new Intent(getApplicationContext(), SensitiveAlarm.class);
                                    pi = PendingIntent.getActivity(getApplicationContext(), alarm_request_code,i, 0);
                                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), pi);
                                    mManager.setAlarmClock(info, pi);
                                    Log.i("HelloAlarmActivity", cal.getTime().toString());
                                }

                            }catch(ParseException e){
                                e.printStackTrace();
                            }
                            Alarm new_one = new Alarm(alarm_request_code, time);
                            new_one.setAlarm_type(alarm_type);
                            mAdapter.add(new_one);
                            mAdapter.notifyDataSetChanged();

                            alarm_request_code+=1;
                        }
                    }).setNegativeButton("거절",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = "http://52.79.200.191:8080/room_user";
                            new NetworkTask2().execute(url, room_id, phonenumber);
                            return;
                        }
                    });
            AlertDialog alert = alert_confirm.create();
            alert.show();
        }

    }
    protected void ServerConnection(String nickname, String thumbnail, String phonenumber, String token){
        String url = "http://52.79.200.191:8080/user";
        new NetworkTask().execute(url, nickname, thumbnail, phonenumber, token);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == ALARM_ADD_SUCCESS){
            if (resultCode == Activity.RESULT_OK){
                int code = data.getIntExtra("alarm_request_code",0);
                String te = data.getStringExtra("time_text");
                Alarm newAlarm = new Alarm(code, te);
                Log.d("REQUESTCODe",""+code);
                mAdapter.add(newAlarm);
                mAdapter.notifyDataSetChanged();
            }
            if (resultCode == Activity.RESULT_CANCELED){
                Log.d("ALARM_ADD","FAILED");
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mAdapter.notifyDataSetChanged();
        //여기서 refresh 필요함
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("Life cycle", "onPause");
        // txt 파일 생성
        ArrayList<Alarm> temp = mAdapter.getAlarmList();
        String testStr;
        dirPath = getFilesDir().getAbsolutePath();
        if (temp!= null){
            testStr= "";
            for (int i=0; i<temp.size(); i++){
                Alarm who = temp.get(i);
                testStr = testStr + who.time_text+"&"+Boolean.toString(who.open)+"&"+Integer.toString(who.pending_list_index)+"\n";
            }
        }else{
            testStr = "";
        }
        File savefile = new File(dirPath + "/test2.txt");
        try {
            FileOutputStream fos = new FileOutputStream(savefile);
            fos.write(testStr.getBytes());
            fos.close();
        } catch (IOException e) {
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("Life cycle","onDestroy");

    }
    class NetworkTask extends AsyncTask<String, Void, String>{

        private String url;

        @Override
        protected String doInBackground(String... params){
            Log.d("serverConnection","doInBackground()");
            url = params[0];
            JSONObject values = new JSONObject();
            try{
                for (int i=1; i<params.length;i++){
                    if (i==1){
                        values.put("nickname",params[i]);
                    }if (i==2){
                        values.put("thumbnail",params[i]);
                    }if (i==3){
                        values.put("phonenumber",params[i]);
                    }if (i==4){
                        values.put("tokenid",params[i]);
                    }
                }}catch(JSONException e){
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

    class NetworkTask2 extends AsyncTask<String, Void, String>{

        private String url;

        @Override
        protected String doInBackground(String... params){
            Log.d("serverConnection","doInBackground()");
            url = params[0];
            url = url+"/"+params[1]+"&&"+params[2];
            Log.d("WHOAREYOU", url);
            Log.d("serverConnection","NetworkTask2 in MainActivity.class");
            String result;
            RequestHttpGeneral requestHttpURLConnection = new RequestHttpGeneral();
            try{
                result = requestHttpURLConnection.request(url, "DELETE");
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
