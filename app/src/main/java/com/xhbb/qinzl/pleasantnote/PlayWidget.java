package com.xhbb.qinzl.pleasantnote;

import android.app.PendingIntent;
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
        Intent service = MusicService.newIntent(context, MusicService.ACTION_SEND_MUSIC_TO_PLAY_WIDGET);
        context.getApplicationContext().startService(service);
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
                    views.setOnClickPendingIntent(R.id.playButton, getServicePendingIntent(playService));
                    views.setOnClickPendingIntent(R.id.nextButton, getServicePendingIntent(nextService));
                } else {
                    views.setOnClickPendingIntent(R.id.playButton, getActivityPendingIntent(mainActivityIntent));
                    views.setOnClickPendingIntent(R.id.nextButton, getActivityPendingIntent(mainActivityIntent));
                }

                views.setOnClickPendingIntent(R.id.root, getActivityPendingIntent(mainActivityIntent));

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

            private PendingIntent getActivityPendingIntent(Intent intent) {
                return PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            private PendingIntent getServicePendingIntent(Intent service) {
                return PendingIntent.getService(
                        applicationContext,
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
        ComponentName componentName = new ComponentName(context, PlayWidget.class);

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = widgetManager.getAppWidgetIds(componentName);
        Music music = intent.getParcelableExtra(Contracts.EXTRA_MUSIC);
        boolean musicPlayed = false;

        switch (intent.getAction()) {
            case Contracts.ACTION_MUSIC_INITED:
            case Contracts.ACTION_MUSIC_STOPPED:
                break;
            case Contracts.ACTION_MUSIC_PLAYED:
                musicPlayed = true;
                break;
            default:
                super.onReceive(context, intent);
        }

        if (music == null) {
            music = new Music();
        }

        Context applicationContext = context.getApplicationContext();
        for (int widgetId : widgetIds) {
            executeUpdateAppWidgetTask(applicationContext, widgetManager, widgetId, music,
                    musicPlayed);
        }
    }
}

