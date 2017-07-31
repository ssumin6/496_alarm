package kaist.alarm;

import android.app.Activity;
import android.app.Application;

import com.kakao.auth.KakaoSDK;

/**
 * Created by q on 2017-07-29.
 */

public class GlobalApplication extends Application {
    private static volatile GlobalApplication instance = null;
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    public static Activity getCurrentActivity(){
        return currentActivity;
    }
    public static void setCurrentActivity(Activity currentActivity){
        GlobalApplication.currentActivity= currentActivity;
    }

    public static GlobalApplication getGlobalApplicationContext(){
        if (instance ==null) throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }
}
