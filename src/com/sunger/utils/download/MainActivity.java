package com.sunger.utils.download;

import com.sunger.downloader.R;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private ProgressBar mProgressBar;
	private Button start;
	private Button pause;
	private Button delete;
	private Button reset;
	private TextView tv_total;
	private TextView tv_speed;

	private Downloader mDownloadUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		start = (Button) findViewById(R.id.button_start);
		pause = (Button) findViewById(R.id.button_pause);
		delete = (Button) findViewById(R.id.button_delete);
		reset = (Button) findViewById(R.id.button_reset);
		tv_total = (TextView) findViewById(R.id.textView_total);
		tv_speed = (TextView) findViewById(R.id.tv_speed);
		String urlString = "http://gdown.baidu.com/data/wisegame/7810ca9719335544/weibo_1790.apk";
		String localPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/local";
		mDownloadUtil = new Downloader(2, localPath, "weibo_1790.apk",
				urlString, this);
		mDownloadUtil.setOnDownloadListener(new OnDownloadListener() {

			@Override
			public void downloadFinish(String msg) {

			}

			@Override
			public void downloadError(String msg) {

			}

			@Override
			public void downloadProgress(int percent, int total,
					int completeSize) {
				mProgressBar.setProgress(percent);
				tv_total.setText(completeSize + "/" + total);
			}

			@Override
			public void downloadSpeed(String speed) {
				super.downloadSpeed(speed);
				tv_speed.setText("下载速度" + speed);

			}

		});

		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.start();
			}
		});
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.pause();
			}
		});
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.delete();
			}
		});
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.reset();
			}
		});
	}

}
