package com.Xtian.Blindroid;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.Xtian.Blindroid.Gcm.GcmBroadcastReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;


public class NotificationIntentService extends IntentService {

    final static String GROUP_BLINDROID = "group_blindroid";


    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null)
        {
            String messageType = intent.getStringExtra("gcmType");
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    sendNotification(this, null, null, "Send error",null);
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification(this, null, null, "Deleted messages on server",null);
                    break;
                default:
                    String msg = intent.getStringExtra(Commons.MSG);
                    String senderPhone = intent.getStringExtra(Commons.FROM).trim();
                    Contact contact = Commons.getContactByPhone(senderPhone);
                    contact.register(this);
                    contact.unreadMessage(this);
                    String receiverPhone = intent.getStringExtra(Commons.TO);
                    Message message =new Message(msg,senderPhone,receiverPhone,DataProvider.MessageType.INCOMING.ordinal());
                    message.register(this);


                    sendNotification(this, senderPhone, contact.getFullName(), msg, receiverPhone);
                    Log.i("receiver", senderPhone + " - " + contact.getFullName() + " - " + msg);
                    break;
            }
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void sendNotification(Context context, String senderPhone ,String name,String msg, String receiverPhone) {

        long senderId=DataProvider.getProfileId(context,senderPhone);

        // Intent for open chat on touch
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Commons.PROFILE_ID, senderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // intent for phone reply
        Intent phoneReplyIntent = new Intent(context, RecognitionActivity.class);
        phoneReplyIntent.setAction(RecognitionActivity.PHONE_REPLY);
        phoneReplyIntent.putExtra(RecognitionActivity.REPLY_NUMBER, senderPhone);
        phoneReplyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        phoneReplyIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent phoneReplyPendingIntent = PendingIntent.getActivity(context, 0, phoneReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action phoneReplyAction =
                new NotificationCompat.Action.Builder(R.drawable.reply_dark,
                        "Responder", phoneReplyPendingIntent)
                        .build();

        // intent for quick phone reply
        Intent quickReplyIntent = new Intent(context, RecognitionActivity.class);
        quickReplyIntent.setAction(RecognitionActivity.QUICK_REPLY);
        quickReplyIntent.putExtra(RecognitionActivity.REPLY_NUMBER, senderPhone);
        quickReplyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        quickReplyIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent quickReplyPendingIntent = PendingIntent.getActivity(context, 0, quickReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action quickReplyAction =
                new NotificationCompat.Action.Builder(R.drawable.quick_reply,
                        "Resp. rapida", quickReplyPendingIntent)
                        .build();

        //bitmap for the contact image
        Bitmap bitmap= BitmapFactory.decodeStream(Commons.openPhoto(context,senderPhone));
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        dr.setCornerRadius(Math.min(dr.getMinimumWidth(),dr.getMinimumHeight()));
        bitmap = dr.getBitmap();




        //wearable reply action
        RemoteInput remoteInput = new RemoteInput.Builder(RecognitionActivity.EXTRA_VOICE_REPLY)
                .setLabel("Contestar")
                .build();

        Intent wearReplyIntent = new Intent(context, RecognitionActivity.class);
        wearReplyIntent.setAction(RecognitionActivity.WEAR_REPLY);
        wearReplyIntent.putExtra(RecognitionActivity.REPLY_NUMBER,senderPhone);
        PendingIntent wearReplyPendingIntent =
                PendingIntent.getActivity(context, 0, wearReplyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the reply action and add the remote input
        NotificationCompat.Action wearReplyAction =
                new NotificationCompat.Action.Builder(R.drawable.reply,
                        "Responder a "+name, wearReplyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();


        //conversation history for the second page.
        ArrayList<Message> messages=DataProvider.getMessages(context,senderPhone,"10");
        String bigText="";
        for (Message message:messages)
        {
            if(message.getSender().equals(senderPhone))
                bigText=bigText+"<b>"+name+"</b><br>";
            else
                bigText=bigText+"<b>Yo</b><br>";
            bigText=bigText+message.getText()+"<br><br>";

        }
        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle("Page 2")
                .bigText(Html.fromHtml(bigText));

        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(context)
                        .setStyle(secondPageStyle)
                        .build();

    /*    // Create the notification
        Notification n  = new NotificationCompat.Builder(context)
                .setContentTitle(name)
                .setContentText(msg)
                .setSmallIcon(R.drawable.blindroid_icon)
                .setContentIntent(pIntent)
                .setLargeIcon(bitmap)
                .setGroup(GROUP_BLINDROID)
                .extend(new NotificationCompat.WearableExtender()
                        .addAction(wearReplyAction)
                        .addPage(secondPageNotification)
                        .setBackground(BitmapFactory.decodeStream(
                                Commons.openPhoto(context, senderPhone))))
                .addAction(phoneReplyAction)
                .addAction(quickReplyAction)
                .build();

        n.flags |= Notification.FLAG_AUTO_CANCEL;


        NotificationManager notificationManager =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!TextUtils.isEmpty(Commons.getRingtone())) {

            n.sound= Uri.parse(Commons.getRingtone());
        }

        if(!Commons.getCurrentChat().equals(senderPhone))
        {
            notificationManager.notify((int)senderId, n);
            Log.i("senderId",String.valueOf(senderId));
        }
*/
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle(name)
                .setContentText(msg)
                .setSmallIcon(R.drawable.blindroid_icon)
                .setContentIntent(pIntent)
                .setLargeIcon(bitmap)
                .setGroup(GROUP_BLINDROID)
                .extend(new NotificationCompat.WearableExtender()
                        .addAction(wearReplyAction)
                        .addPage(secondPageNotification)
                        .setBackground(BitmapFactory.decodeStream(
                                Commons.openPhoto(context, senderPhone))))
                .addAction(phoneReplyAction)
                .addAction(quickReplyAction)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        if (!TextUtils.isEmpty(Commons.getRingtone())) {

            noti.sound= Uri.parse(Commons.getRingtone());
        }

        if(!Commons.getCurrentChat().equals(senderPhone))
        {
            notificationManager.notify((int)senderId, noti);
        }



    }


}
