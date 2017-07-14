package com.xhbb.qinzl.pleasantnote.async;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.xhbb.qinzl.pleasantnote.MainActivity;
import com.xhbb.qinzl.pleasantnote.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by qinzl on 2017/7/14.
 */

class NotificationUtils {

    private static final String FOREGROUND_CHANNEL_ID = "FOREGROUND_CHANNEL_ID";
    private static final String FOREGROUND_CHANNEL_NAME = "正在播放音乐";

    static Notification getForegroundNotification(Context context) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, MainActivity.newIntent(context), PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    FOREGROUND_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);

            NotificationManager manager = (NotificationManager)
                    context.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            notification = new Notification.Builder(context, FOREGROUND_CHANNEL_ID)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_notifications_active)
                    .setContentText(context.getText(R.string.music_playing_notification_content))
                    .setContentIntent(pendingIntent)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(context)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_notifications_active)
                    .setContentText(context.getText(R.string.music_playing_notification_content))
                    .setContentIntent(pendingIntent)
                    .setLights(0, 0, 0)
                    .setSound(null)
                    .setVibrate(new long[]{0})
                    .build();
        }

        return notification;
    }
}
