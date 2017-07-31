package kaist.alarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {

    private SessionCallback callback;
    private final int PERMISSION_REQUEST_READ = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS},PERMISSION_REQUEST_READ);
            }else{
                callback = new SessionCallback();
                Session.getCurrentSession().addCallback(callback);
            }
        }else{
            callback = new SessionCallback();
            Session.getCurrentSession().addCallback(callback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case PERMISSION_REQUEST_READ:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }else{

                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode,data)){
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private class SessionCallback implements ISessionCallback{
        @Override
        public void onSessionOpened(){
            redirectSignupActivity();
        }
        @Override
        public void onSessionOpenFailed(KakaoException exception){
            if (exception != null){
                Logger.e(exception);
                Log.d("SessionFailed","Finale");
            }
            setContentView(R.layout.activity_login);
        }
    }
    protected void redirectSignupActivity(){
        final Intent intent = new Intent(this, KaKaoSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();

    }
}
