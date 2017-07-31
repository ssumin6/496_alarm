package kaist.alarm;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.content.ContentResolverCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FriendAddActivity extends AppCompatActivity {
    private ListView friendList;
    private Button saveButton;
    private Button refreshButton;
    private TextView selectFriend;
    private FriendAdapter myAdapter;
    private ArrayList<String> numbers = new ArrayList<>();
    private ArrayList<Friend> friend_user = new ArrayList<>();
    private ArrayList<Friend> selected = new ArrayList<>();

    private String my_Phone; //내 핸드폰 전화번호

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);

        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        my_Phone = mgr.getLine1Number();

        final Uri ContentURI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String phoneNumber;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContentURI, null, null, null, null);
        if (cursor.getCount() >0){
            while(cursor.moveToNext()){
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber>0){
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID+" = ?", new String[]{contact_id}, null);
                    while(phoneCursor.moveToNext()){
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        numbers.add(phoneNumber.replaceAll("\\D",""));
                    }
                    phoneCursor.close();
                }
            }
        }
        cursor.close();
        new NetworkTask().execute("http://52.79.200.191:8080/user");
        friendList = (ListView)findViewById(R.id.friendList);
        myAdapter = new FriendAdapter(getApplicationContext(),friend_user);
        friendList.setAdapter(myAdapter);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parentView, View clickedView, int position, long id){
                Friend who = (Friend) parentView.getItemAtPosition(position);
                if (!selected.contains(who)){
                    selected.add(who);
                    String tp ="";

                    if (selected!= null){
                        for (int i=0; i< selected.size(); i++){
                            tp = tp +" "+selected.get(i).getName();
                        }
                    }
                    Log.d("Select",tp);
                }else{
                    selected.remove(who);
                    Log.d("Removed","okay"+selected.size());
                }
            }
        });
        selectFriend = (TextView)findViewById(R.id.textView6);

        String temp ="";

        if (selected!= null){
            for (int i=0; i< selected.size(); i++){
                temp = temp +" "+selected.get(i).getName();
            }
        }
        selectFriend.setText(temp);

        refreshButton = (Button)findViewById(R.id.button3);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter.notifyDataSetChanged();
            }
        });

        saveButton = (Button)findViewById(R.id.button2);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("group_list",selected);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    class NetworkTask extends AsyncTask<String, Void, ArrayList<Friend>>{
        @Override
        protected ArrayList<Friend> doInBackground(String... params){
            try{
                String stringurl = params[0];
                URL url = new URL(stringurl);
                HttpURLConnection search = (HttpURLConnection)url.openConnection();
                search.setRequestMethod("GET");
                Log.v("NETWORK",Integer.toString(search.getResponseCode()));
                search.setConnectTimeout(10000);
                search.setReadTimeout(10000);
                if (search.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
                BufferedReader br = new BufferedReader(new InputStreamReader(search.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output= br.readLine())!= null){
                    sb.append(output);
                }
                search.disconnect();
                JSONArray list = new JSONArray(sb.toString());

                for (int i=0; i<list.length(); i++){
                    String number = list.getJSONObject(i).getString("phonenumber");
                    number = number.replace("+82","0");
                    if (number.equals(my_Phone)){
                        String thumb = list.getJSONObject(i).getString("thumbnail");
                        String name = list.getJSONObject(i).getString("nickname");
                        Friend myself = new Friend(thumb,name,number);
                        selected.add(myself);
                    }

                    if (numbers.contains(number)){
                        String thumb = list.getJSONObject(i).getString("thumbnail");
                        String name= list.getJSONObject(i).getString("nickname");
                        String token = list.getJSONObject(i).getString("tokenid");
                        Friend one = new Friend(thumb, name,number);
                        one.setToken(token);
                        friend_user.add(one);
                    }
                }
                return friend_user;
            }catch(MalformedURLException e){
                e.printStackTrace();
                return null;
            }catch(IOException e){
                e.printStackTrace();
                return null;
            }catch(JSONException e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }
}
