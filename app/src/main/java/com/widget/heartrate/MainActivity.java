package com.widget.heartrate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer mTimer;
    private TimerTask mTask;
    private HeartRateView mChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimer= new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };
        mChartView = findViewById(R.id.chart);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mChartView.addData((int) (Math.random() * 220));
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mTimer.schedule(mTask,1,500);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();
        mHandler.removeCallbacksAndMessages(null);
    }

}