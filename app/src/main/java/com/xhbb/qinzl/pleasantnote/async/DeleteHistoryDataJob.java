package com.xhbb.qinzl.pleasantnote.async;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by qinzl on 2017/7/19.
 */

public class DeleteHistoryDataJob extends Job {

    public static final String TAG = "DeleteHistoryDataJob";

    public static void scheduleJob() {
        new JobRequest.Builder(TAG)
                .setPeriodic(TimeUnit.HOURS.toMillis(2))
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    static void cancelJob() {
        JobManager.instance().cancelAllForTag(TAG);
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        MainTasks.deleteHistoryMusicIfOutRange(getContext());
        return Result.SUCCESS;
    }
}
