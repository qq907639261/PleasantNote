package com.xhbb.qinzl.pleasantnote;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.GlideApp;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.util.concurrent.ExecutionException;

public class PlayWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Context applicationContext = context.getApplicationContext();
        Intent service = MusicService.newIntent(applicationContext, MusicService.ACTION_SEND_MUSIC_TO_PLAY_WIDGET);
        applicationContext.startService(service);
    }

    private void executeUpdateAppWidgetTask(
            final Context applicationContext, final AppWidgetManager widgetManager,
            final int widgetId, final Music music, final boolean musicPlayed) {

        new AsyncTask<Void, Void, RemoteViews>() {
            @Override
            protected RemoteViews doInBackground(Void... voids) {
                RemoteViews views = new RemoteViews(applicationContext.getPackageName(), R.layout.widget_play);

                Intent playService;
                if (musicPlayed) {
                    views.setImageViewResource(R.id.playButton, R.drawable.ic_pause);
                    playService = MusicService.newIntent(applicationContext, MusicService.ACTION_PAUSE_MUSIC);
                } else {
                    views.setImageViewResource(R.id.playButton, R.drawable.ic_play);
                    playService = MusicService.newIntent(applicationContext, MusicService.ACTION_PLAY_MUSIC);
                }

                Intent mainActivityIntent = MainActivity.newIntent(applicationContext);
                Intent nextService = MusicService.newIntent(applicationContext,
                        MusicService.ACTION_PLAY_NEXT_MUSIC);

                if (music.getName() != null) {
                    views.setOnClickPendingIntent(R.id.playButton,
                            getServicePendingIntent(applicationContext, playService));
                    views.setOnClickPendingIntent(R.id.nextButton,
                            getServicePendingIntent(applicationContext, nextService));
                } else {
                    views.setOnClickPendingIntent(R.id.playButton,
                            getActivityPendingIntent(applicationContext, mainActivityIntent));
                    views.setOnClickPendingIntent(R.id.nextButton,
                            getActivityPendingIntent(applicationContext, mainActivityIntent));
                }

                views.setOnClickPendingIntent(R.id.root,
                        getActivityPendingIntent(applicationContext, mainActivityIntent));

                views.setTextViewText(R.id.musicNameText, music.getName());
                views.setTextViewText(R.id.singerNameText, music.getSingerName());

                try {
                    Bitmap bitmap = GlideApp.with(applicationContext)
                            .asBitmap()
                            .load(music.getSmallPictureUrl())
                            .error(R.drawable.my_icon_default)
                            .submit()
                            .get();

                    views.setImageViewBitmap(R.id.musicImage, bitmap);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                return views;
            }

            private PendingIntent getActivityPendingIntent(Context context, Intent intent) {
                return TaskStackBuilder.create(context)
                        .addNextIntent(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            private PendingIntent getServicePendingIntent(Context context, Intent service) {
                return PendingIntent.getService(
                        context,
                        0,
                        service,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
            }

            @Override
            protected void onPostExecute(RemoteViews views) {
                super.onPostExecute(views);
                widgetManager.updateAppWidget(widgetId, views);
            }
        }.execute();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean musicPlayed;
        switch (intent.getAction()) {
            case Contracts.ACTION_MUSIC_STOPPED:
                musicPlayed = false;
                break;
            case Contracts.ACTION_MUSIC_PLAYED:
                musicPlayed = true;
                break;
            default:
                super.onReceive(context, intent);
                return;
        }

        Context applicationContext = context.getApplicationContext();

        ComponentName componentName = new ComponentName(applicationContext, PlayWidget.class);

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(applicationContext);
        int[] widgetIds = widgetManager.getAppWidgetIds(componentName);
        Music music = intent.getParcelableExtra(Contracts.EXTRA_MUSIC);

        if (music == null) {
            music = new Music();
        }

        for (int widgetId : widgetIds) {
            executeUpdateAppWidgetTask(applicationContext, widgetManager, widgetId, music,
                    musicPlayed);
        }
    }
}

