package com.knst.calendar;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.knst.calendar.App;

import org.json.JSONException;
import org.json.JSONObject;

//import co.ronash.pushe.Pushe;
import com.pushpole.sdk.PushPole;
import me.cheshmak.android.sdk.core.push.CheshmakFirebaseMessagingService;
import my.DialogPush;

import static me.cheshmak.android.sdk.advertise.Banner.TAG;

public class MyFcmServicePushe extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "Date: 111111111111111111111");

        if (!App.checkTime(this)) {
            Log.e(TAG, "Date: today < checktime");
            return;
        }

        if (remoteMessage == null)
            return;

        Log.e(TAG, "FirstShowData: " + remoteMessage.getData().toString());


        if (PushPole.getFcmHandler(this).onMessageReceived(remoteMessage)) {
            // Message belongs to Pushe, no further action needed
            return;
        }
        CheshmakFirebaseMessagingService Cheshmak = new CheshmakFirebaseMessagingService();
        if (Cheshmak.isCheshmakMessage(remoteMessage)) {
            Cheshmak.onMessageReceived(remoteMessage);
            return;
        }

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {

                JSONObject bodyObjects = new JSONObject(remoteMessage.getData());

                if(bodyObjects.has("type")) {
                    DialogPush.setJson(bodyObjects);

                    Intent i = new Intent(this, DialogPush.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    return;
                }
                /*else {
                    JSONObject json = new JSONObject(remoteMessage.getData());
                    handleDataMessage(json);
                }*/
                return;
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            //handlePushNotification(remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        // Message does not belong to Pushe, process message...
    }




    @Override
    public void onMessageSent(String s) {
        PushPole.getFcmHandler(this).onMessageSent(s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        PushPole.getFcmHandler(this).onSendError(s, e);
    }

    @Override
    public void onDeletedMessages() {
        PushPole.getFcmHandler(this).onDeletedMessages();
    }
}
