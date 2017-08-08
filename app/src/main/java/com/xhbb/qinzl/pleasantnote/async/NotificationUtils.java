package com.xhbb.qinzl.pleasantnote.async;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.xhbb.qinzl.pleasantnote.PlayActivity;
import com.xhbb.qinzl.pleasantnote.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by qinzl on 2017/7/14.
 */

class NotificationUtils {

    // 频道ID值最好别动，否则要根据改动前的值手动删除前一个频道，不然用户的相关设置界面会残留以前的频道名称
    private static final String CHANNEL_ID_FOREGROUND = "channel_id_01";
//    private static final String CHANNEL_ID_DOWNLOAD = "channel_id_02";

    static Notification getForegroundNotification(Context context) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                PlayActivity.newIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_FOREGROUND,
                    context.getText(R.string.background_task_channel),
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);

            NotificationManager manager = (NotificationManager)
                    context.getSystemService(NOTIFICATION_SERVICE);

            manager.createNotificationChannel(channel);

            notification = new Notification.Builder(context, CHANNEL_ID_FOREGROUND)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentText(context.getText(R.string.music_playing_notification_content))
                    .setContentIntent(pendingIntent)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(context)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentText(context.getText(R.string.music_playing_notification_content))
                    .setContentIntent(pendingIntent)
                    .setLights(0, 0, 0)
                    .setSound(null)
                    .setVibrate(null)
                    .build();
        }

        return notification;
    }

//    static void notifyDownload(Context context, int progress) {
//        NotificationManager notificationManager = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Notification notification;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_DOWNLOAD,
//                    context.getText(R.string.download_task_channel),
//                    NotificationManager.IMPORTANCE_DEFAULT);
//
//            channel.setSound(null, null);
//            channel.enableVibration(false);
//            channel.enableLights(false);
//
//            notificationManager.createNotificationChannel(channel);
//
//            notification = new Notification.Builder(context, CHANNEL_ID_DOWNLOAD)
//                    .setProgress(100, progress, false)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .build();
//        } else {
//            notification = new NotificationCompat.Builder(context)
//                    .setProgress(100, progress, false)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setSound(null)
//                    .setVibrate(null)
//                    .setLights(0, 0, 0)
//                    .build();
//        }
//
//        notificationManager.notify(1, notification);
//    }
}
