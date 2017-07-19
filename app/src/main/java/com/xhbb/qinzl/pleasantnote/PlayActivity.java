package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayActivity extends AppCompatActivity {

    public static void start(Context context) {
        context.startActivity(newIntent(context));
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PlayActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: 2017/7/19 启动服务，接收音乐数据广播
    }
}
