package kaist.alarm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by q on 2017-07-31.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh(){
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("IDService","Refreshed Token : "+token);

       // sendRegistrationToServer(token);
    }


}
