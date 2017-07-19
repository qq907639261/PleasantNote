package com.xhbb.qinzl.pleasantnote.async;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by qinzl on 2017/7/19.
 */

public class MyJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case DeleteHistoryDataJob.TAG:
                return new DeleteHistoryDataJob();
            default:
                return null;
        }
    }
}
