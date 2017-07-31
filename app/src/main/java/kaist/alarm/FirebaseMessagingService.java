package kaist.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by q on 2017-07-31.
 */

public class FirebaseMessagingService extends  com.google.firebase.messaging.FirebaseMessagingService {
    private final int MESSAGE_REQUEST = 27496;
    private Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        intent = new Intent(this, MainActivity.class);
        String message = remoteMessage.getData().get("message]");
        sendNotification(message);
        String alarm_type = remoteMessage.getData().get("alarmtype]");
        String time = remoteMessage.getData().get("time]");
        String room_id = remoteMessage.getData().get("id]");

        intent.putExtra("room_id",room_id);
        intent.putExtra("time", time);
        intent.putExtra("alarm_type",alarm_type);
        intent.putExtra("message", message);
        if (message == null){
            intent.putExtra("AlertSET",3);
        }else{
            intent.putExtra("AlertSET",1);
        }
        startActivity(intent);
    }

    private void sendNotification(String messageBody){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, MESSAGE_REQUEST,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //알림의 모양을 수정하는 부분.
        NotificationCompat.Builder notificationbuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("GROUP ALARM REQUEST").setContentText(messageBody).setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(MESSAGE_REQUEST, notificationbuilder.build());
    }
}
