package com.xhbb.qinzl.pleasantnote.async;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadMusicService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void downloadMusic() {

    }
}
