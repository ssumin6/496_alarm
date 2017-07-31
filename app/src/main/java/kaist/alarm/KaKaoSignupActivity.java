package kaist.alarm;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.IDNA;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.auth.ErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by q on 2017-07-29.
 */

public class KaKaoSignupActivity extends AppCompatActivity {

    String nickname;
    String thumbnail;
    String phonenumber ="01000000000";
    String token;


    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestMe();
    }

    protected void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = " failed to get user info. msg= " + errorResult;
                Logger.d(message);
                Log.d("fAIL","FAIL");

                ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                if (result == ErrorCode.CLIENT_ERROR_CODE) {
                    finish();
                } else {
                    redirectLoginActivity();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
                Log.d("Fial","FIAL");
            }

            @Override
            public void onNotSignedUp() {

            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Logger.d("UserProfile : " + userProfile);
                Log.d("UserProfile",userProfile.toString());
                nickname = userProfile.getNickname();
                thumbnail = userProfile.getThumbnailImagePath();
                phonenumber = getPhoneNumber();
                redirectMainActivity();
            }
        });
    }
    public String getPhoneNumber(){
        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mgr.getLine1Number();
    }


    private void redirectMainActivity(){
        Intent start = new Intent(this, MainActivity.class);
        start.putExtra("nickname", nickname);
        start.putExtra("thumbnail", thumbnail);
        startActivity(start);
        finish();
    }
    protected void redirectLoginActivity(){
        final Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

}