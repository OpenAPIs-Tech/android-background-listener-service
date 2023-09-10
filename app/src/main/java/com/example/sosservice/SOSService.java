package com.example.sosservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import java.util.List;

public class SOSService extends Service implements SpeechDelegate {

    public final String TAG = "SOSService";
    public final String SPEECH_TAG = "SpeechService";

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "sos_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
// Create and configure the notification channel (for Android Oreo and above)
        createNotificationChannel();
        Speech.init(this, getPackageName());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        // Create a notification for the foreground service
        Notification notification = createNotification();

        // Start the service as a foreground service
        startForeground(NOTIFICATION_ID, notification);

        startListeningSpeech();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (Speech.getInstance() != null && Speech.getInstance().isListening()) {
            Speech.getInstance().shutdown();
        }
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStartOfSpeech() {
        Log.i(SPEECH_TAG, "speech recognition is now active");
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        Log.d(SPEECH_TAG, "rms is now: " + value);
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        StringBuilder str = new StringBuilder();
        for (String res : results) {
            str.append(res).append(" ");
        }

        Log.i(SPEECH_TAG, "partial result: " + str.toString().trim());
    }

    @Override
    public void onSpeechResult(String result) {
        Log.i(SPEECH_TAG, "result: " + result);
        onSpeechResultOwn(result);

//        if(Speech.getInstance() != null) {
//            Log.d(TAG, "+" +  Speech.getInstance());
//            if (Speech.getInstance().isListening()) {
//                Log.d(TAG, "onSpeechResult: Listening Stopped");
//                Speech.getInstance().stopListening();
//            }
//            Speech.getInstance().shutdown();
//        }
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Log.e(TAG, "onSpeechResult: Error", e.getCause());
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//
////        Speech.init(this);
//        startListeningSpeech();
    }

    public void onSpeechResultOwn(String result) {
        Log.i(SPEECH_TAG, "result: " + result);
        ApiManager.getInstance().sendDataToServer(result);
        if(Speech.getInstance() != null) {
            Log.d(TAG, "+" +  Speech.getInstance());
            if (Speech.getInstance().isListening()) {
                Log.d(TAG, "onSpeechResult: Listening Stopped");
                Speech.getInstance().stopListening();
            }
            Speech.getInstance().shutdown();
        }

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Log.e(TAG, "onSpeechResult: Error", e.getCause());
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }

//        Speech.init(this);
        startListeningSpeech();
    }



    public void startListeningSpeech() {
        Log.d(TAG, "startListeningSpeech: ");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Speech.init(SOSService.this);
//                    Speech.getInstance().startListening(SOSService.this);
//                } catch (SpeechRecognitionNotAvailable | GoogleVoiceTypingDisabledException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
        try {
            Speech.init(SOSService.this);
            Speech.getInstance().startListening(SOSService.this);
        } catch (SpeechRecognitionNotAvailable | GoogleVoiceTypingDisabledException e) {
            throw new RuntimeException(e);
        }
    }

    private void createNotificationChannel() {
        // Create the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "SOS Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        // Create a notification for the foreground service
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("SOS Service")
                .setContentText("Service is running in the background")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true);

        // Return the notification
        return builder.build();
    }
}
