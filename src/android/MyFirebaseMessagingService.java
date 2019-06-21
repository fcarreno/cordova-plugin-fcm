package com.gae.scaffolder.plugin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
//import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
//import ar.com.cualify.R;

/**
 * Created by Felipe Echanique on 08/06/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMPlugin";
    private Random random = new Random();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");

        Map<String, Object> data = new HashMap<String, Object>();
        for (String key : remoteMessage.getData().keySet()) {
                    Object value = remoteMessage.getData().get(key);
                    Log.d(TAG, "\tKey: " + key + " Value: " + value);
            data.put(key, value);
        }

        if( remoteMessage.getNotification() != null){

          Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
          Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
          simpleNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        } else if(!data.keySet().isEmpty()){
          Log.d(TAG, "\tNotification Data: " + data.toString());
          customNotification(data.get("title").toString(), data.get("summary").toString(), data);

          // For data messages, besides showing a custom notification on status bar (above),
          // we want to push the data to the app, if it's currently running in foreground
          // (in order to store/manipulate the data/event in case the user misses the notification).
          if(data.containsKey("tryForegroundDataPush")){
            FCMPlugin.sendPushPayload( data, false );
          }
        }




    }
    // [END receive_message]

    /**
     * Create and show a custom notification containing the received FCM -data- message.
     * @param title FCM message title received.
     * @param summary FCM message body received.
     */
    private void customNotification(String title, String summary, Map<String, Object> data) {
        Intent intent = new Intent(this, FCMPluginActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // NOTE: with just above line only - it didn't work
        // When accumulating notifications in foreground (e.g.: 2 in a row), the 2nd one was not triggering the activity when tapped,
        // so it was ignored without notifying the app...
        // Copying the intent flags and using a unique int value for the 2nd parameter of pendingIntent, from here - made it work...:
        // https://stackoverflow.com/questions/36126851/pendingintent-not-working-on-notification
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


		    for (String key : data.keySet()) {
			    intent.putExtra(key, data.get(key).toString());
		    }
        int notificationId = random.nextInt();
		    intent.putExtra("id", Integer.toString(notificationId));

        // Generating current date from system time in ISO  8601 format
        // (it looks like it gets created/formated based on system timezone...so just using default...)
        // Docs: https://stackoverflow.com/questions/6782185/convert-timestamp-long-to-normal-date-format
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // Adding local timezone offset ('Z') at the end (so Angular date pipe takes it)
        //format.setTimeZone(TimeZone.getDefault()); // NOTE: user's/device local timezone offset (e.g.: '+03:00') will be added if not specified...
        intent.putExtra("date", format.format(date));

        // NOTE: using PendingIntent.FLAG_UPDATE_CURRENT instead of FLAG_ONE_SHOT so as a notification that was tapped, but did not make it to the app
        // (e.g.: user switched to another app while ours was still being opened by the notification) could be tapped again
        // generating the same intent (using FLAG_ONE_SHOT triggered the app activity only the 1st time...)
        PendingIntent pendingIntent = PendingIntent.getActivity(this, random.nextInt() , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // getApplicationContext().getResources().getIdentifier(rawResourceFileNameStringWithNoExtension, "raw", getPackageName()));
        // Might be able to use the above within .setSound to play a custom sound under res/raw?
        // Maybe this one is simpler (to get resource uri):
        // https://stackoverflow.com/questions/7976141/get-uri-of-mp3-file-stored-in-res-raw-folder-in-android

        //Test adding actions...(need to capture from local notifications plugins)
        //NotificationCompat.Action yes = new NotificationCompat.Action(R.mipmap.yes ,"Si",pendingIntent);
        //NotificationCompat.Action no = new NotificationCompat.Action(R.mipmap.no ,"No",pendingIntent);
        //NotificationCompat.Action action = new NotificationCompat.Action(getApplicationInfo().icon,"Hooolissss",pendingIntent);


        //.setSmallIcon(R.drawable.notification_icon)
        int notificationIconId = getApplicationContext().getResources().getIdentifier("notification_icon" , "drawable", getPackageName());
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, FCMPlugin.CHANNEL_ID)
                .setSmallIcon(notificationIconId)
                .setContentTitle(title)
                .setContentText(summary)
                //.setAutoCancel(true) // Notifications will be cancelled/cleared only after app confirmed its reception via notification details page... (default is false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(summary)) // For auto-expanded version of the notification (so text does not truncate)
                                                                                  // E.g.: on Android 5.0 it will not show expanded without this...
                //.addAction(yes)
                //.addAction(no)
                .setSound(defaultSoundUri)
                .setShowWhen(true) // Needs to be enforced for Android 7.0 and above:
                                   // https://developer.android.com/reference/android/app/Notification.Builder#setShowWhen(boolean)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId , notificationBuilder.build());
        //StatusBarNotification[] notifications = notificationManager.getActiveNotifications(); // Avail only from API level 23 (Android 6.0)
        //https://developer.android.com/reference/android/service/notification/StatusBarNotification#getNotification()
    }

  /**
   * Create and show a simple notification containing the received -notification- FCM message.
   *
   * @param messageBody FCM message body received.
   */
  private void simpleNotification(String title, String messageBody) {

    Intent intent = new Intent(this, FCMPluginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

    intent.putExtra("notificationOnly", "yes");


    PendingIntent pendingIntent = PendingIntent.getActivity(this, random.nextInt() , intent, PendingIntent.FLAG_UPDATE_CURRENT);
    // Using same approach as then one used for custom notification
    //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    int notificationIconId = getApplicationContext().getResources().getIdentifier("notification_icon" , "drawable", getPackageName());
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, FCMPlugin.CHANNEL_ID)
      .setSmallIcon(notificationIconId)
      .setContentTitle(title)
      .setContentText(messageBody)
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent);

    NotificationManager notificationManager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(random.nextInt(), notificationBuilder.build());
  }


}
