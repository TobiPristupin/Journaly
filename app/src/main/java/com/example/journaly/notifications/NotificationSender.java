package com.example.journaly.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.journaly.R;
import com.example.journaly.create_screen.CreateActivity;

public class NotificationSender {

    private Context context;
    public static final int NOTIFICATION_ID = 123;

    public NotificationSender(Context context) {
        this.context = context;
    }

    public static final String CHANNEL_ID = "com.example.journaly.reminder_notifications_channel";

    public void sendJournalReminderNotification(){
        Notification notification = buildNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification(){
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(context, CreateActivity.class);
        resultIntent.putExtra(CreateActivity.STATE_INTENT_KEY, CreateActivity.Mode.CREATE);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle("Remember to Journal")
                .setContentText("Meet your goals!")
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder notifications";
            String description = "Reminders to journal";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);


            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
