package com.example.powerreceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CustomReceiver mReceiver = new CustomReceiver();
    IntentFilter filter = new IntentFilter();
    static final String ACTION_CUSTOM_BROADCAST =
            BuildConfig.APPLICATION_ID + ".ACTION_CUSTOM_BROADCAST";
    private TextView number;
    private int randomNumber;
    // Constants for the notification actions buttons.
    private static final String ACTION_UPDATE_NOTIFICATION =
            "com.android.example.notifyme.ACTION_UPDATE_NOTIFICATION";
    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    // Notification ID.
    private static final int NOTIFICATION_ID = 0;

    private Button button_notify;
    private Button button_cancel;
    private Button button_update;

    private NotificationManager mNotifyManager;

    private NotificationReceiver myReceiver = new NotificationReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);

        // Register the receiver using the activity context.
        this.registerReceiver(mReceiver, filter);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver,
                        new IntentFilter(ACTION_CUSTOM_BROADCAST));

        number = findViewById(R.id.number);
        Random random = new Random();
        randomNumber = random.nextInt(20) + 1;
        number.setText(String.valueOf(randomNumber));

        // Create the notification channel.
        createNotificationChannel();

        // Register the broadcast receiver to receive the update action from
        // the notification.
        registerReceiver(myReceiver,
                new IntentFilter(ACTION_UPDATE_NOTIFICATION));

        // Add onClick handlers to all the buttons.
        button_notify = findViewById(R.id.notify);
        button_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send the notification
                mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                sendNotification();
            }
        });

        button_update = (Button) findViewById(R.id.update);
        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update the notification.
                updateNotification();
            }
        });

        button_cancel = (Button) findViewById(R.id.cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cancel the notification.
                cancelNotification();
            }
        });

        // Reset the button states. Enable only Notify button and disable
        // update and cancel buttons.
        setNotificationButtonState(true, false, false);
    }

    public void sendCustomBroadcast(View view) {
        Intent customBroadcastIntent = new Intent(ACTION_CUSTOM_BROADCAST);
        customBroadcastIntent.putExtra("randomNumber", randomNumber);
        LocalBroadcastManager.getInstance(this).sendBroadcast(customBroadcastIntent);
    }

    @Override
    protected void onDestroy() {
        // Unregister the receiver.
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mReceiver);
    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendNotification() {
        // Sets up the pending intent to update the notification.
        // Corresponds to a press of the Update Me! button.
        Intent updateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this,
                NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT);

        // Create the InboxStyle.
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle()
                        .addLine("Here is the first one")
                        .addLine("This is the second one")
                        .addLine("This is third message")
                        .setSummaryText("+3 more");

        // Build the notification with all of the parameters using helper
        // method.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        // Set the style of the notification to InboxStyle.
        notifyBuilder.setStyle(inboxStyle);

        // Add the action button using the pending intent.
        notifyBuilder.addAction(R.drawable.ic_update,
                getString(R.string.update), updatePendingIntent);

        // Deliver the notification.
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

        // Enable the update and cancel buttons but disables the "Notify
        // Me!" button.
        setNotificationButtonState(false, true, true);
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        // Set up the pending intent that is delivered when the notification
        // is clicked.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity
                (this, NOTIFICATION_ID, notificationIntent,
                        PendingIntent.FLAG_ONE_SHOT);

        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_android)
                .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return notifyBuilder;
    }

    public void updateNotification() {
        // Load the drawable resource into the a bitmap image.
        Bitmap androidImage = BitmapFactory.decodeResource(getResources(), R.drawable.mascot_1);

        // Build the notification with all of the parameters using helper method.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        // Update the notification style to BigPictureStyle.
        notifyBuilder.setStyle(new NotificationCompat.InboxStyle()
                .setBigContentTitle(getString(R.string.notification_updated))
                .addLine("Here is the first one")
                .addLine("This is the second one")
                .addLine("This is third message")
                .setSummaryText("+3 more"));

        // Set the large icon.
        notifyBuilder.setLargeIcon(androidImage);

        // Deliver the notification.
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

        // Disable the update button, leaving only the cancel button enabled.
        setNotificationButtonState(false, false, true);
    }

    public void cancelNotification() {
        // Cancel the notification.
        mNotifyManager.cancel(NOTIFICATION_ID);

        // Reset the buttons.
        setNotificationButtonState(true, false, false);
    }

    void setNotificationButtonState(Boolean isNotifyEnabled, Boolean
            isUpdateEnabled, Boolean isCancelEnabled) {
        button_notify.setEnabled(isNotifyEnabled);
        button_update.setEnabled(isUpdateEnabled);
        button_cancel.setEnabled(isCancelEnabled);
    }

    public class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}