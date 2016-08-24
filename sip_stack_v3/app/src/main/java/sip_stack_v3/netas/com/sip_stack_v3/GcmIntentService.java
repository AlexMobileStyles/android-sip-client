package sip_stack_v3.netas.com.sip_stack_v3;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sdulger on 10-Mar-15.
 *  * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    private static GcmIntentService instance = new GcmIntentService();

    public static GcmIntentService getInstance() {
        return instance;
    }

    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";
    Intent intent;
    static String from="";
    static String msgType="";

    public String NtoneFilePath = "/sdcard/Notification_Tone.mp3";
    MediaPlayer mp = new MediaPlayer();

    IncomingCallReceiver mIncomingCallReceiver = IncomingCallReceiver.getInstance();

    public MainActivity mAct;

    NotificationManager mNotificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {

        this.intent=intent;
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */

            msgType = extras.toString();
            msgType= msgType.split("msgType=")[1].split(",")[0].trim().trim();

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                try {
                    processMsg(extras);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        intent.putExtras(extras);
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        from = msg.split("m_")[1].split(",")[0].trim().trim();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Push notification received from "+from))
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

            try {
                if (!mp.isPlaying()){
                    mp.setDataSource(NtoneFilePath);
                    mp.prepare();
                    mp.start();
                }
            }catch (Exception e){
                Log.e("Ringing tone error","error");
            }
    }


    private void processMsg(Bundle extras) throws JSONException {

        JSONObject voip = new JSONObject(extras.getString("voip_info"));
        String calling = voip.getString("calling_party");

        Intent intent = new Intent("sip_stack_v3.netas.com.sip_stack_v3.DISPLAY_MESSAGE");
        intent.putExtra("calling", calling);
        getApplicationContext().sendBroadcast(intent);

    }
}
