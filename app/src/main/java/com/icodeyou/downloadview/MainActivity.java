package com.icodeyou.downloadview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.icodeyou.library.DownloadView;

public class MainActivity extends AppCompatActivity {

    private DownloadView mDownLoadView;
    private int mProgress = 10;
    private Button mBtnPause, mBtnCancel;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mDownLoadView.isLoading())
                mDownLoadView.setProgress(mProgress++);
            if (mProgress <= 100)
                mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownLoadView = (DownloadView) findViewById(R.id.downloadView);
        mBtnPause = (Button) findViewById(R.id.btnPause);
        mBtnCancel = (Button) findViewById(R.id.btnCancel);

        mDownLoadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownLoadView.start();
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDownLoadView.isLoading()) {
                    mDownLoadView.pause();
                }
                else {
                    mDownLoadView.resume();
                }
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownLoadView.cancel();
            }
        });

        mDownLoadView.setOnProgressChangeListener(new DownloadView.OnProgressChangeListener() {
            @Override
            public void onPause() {
                mBtnPause.setText("继续");
            }

            @Override
            public void onContinue() {
                mBtnPause.setText("暂停");
            }

            @Override
            public void onCancel() {
                mBtnPause.setText("暂停");
            }
        });

        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(0);
    }
}