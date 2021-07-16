package com.mackenzie.firebaseproject.Services;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private Handler handler = new Handler();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Looper.prepare();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String msg = remoteMessage.getNotification().getTitle();
                // String body = remoteMessage.getNotification().getBody();
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
        Looper.loop();
    }
}
