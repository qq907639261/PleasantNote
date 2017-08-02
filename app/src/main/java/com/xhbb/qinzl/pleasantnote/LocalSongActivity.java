package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LocalSongActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, LocalSongActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_song);
    }
}
