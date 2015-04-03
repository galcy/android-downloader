package com.sunger.utils.download;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 将下载方法封装在此类 提供下载，暂停，删除，以及重置的方法
 */
public class Downloader implements IRepsone {

	private DownloadRequest mDownloadHttpTool;
	private OnDownloadListener onDownloadListener;

	private int total;
	private int completeSize = 0;
	private DecimalFormat df = new DecimalFormat("######0.00");

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MsgState.DOWNLOAD_ERROT:
				sendErrorMsg((String) msg.obj);
				break;
			case MsgState.DOWNLOAD_PROGRESS:
				sendProgress((Integer) msg.obj);
				break;
			case MsgState.DOWNLOAD_SPEED:
				sendSpeedMsg((Long) msg.obj);
				break;
			case MsgState.DOWNLOAD_FINISH:
				sendFinishMsg((String) msg.obj);
				break;
			default:
				break;
			}
		}

	};

	public Downloader(int threadCount, String filePath, String filename,
			String urlString, Context context) {

		mDownloadHttpTool = new DownloadRequest(threadCount, urlString,
				filePath, filename, context, mHandler);
	}

	// 下载之前首先异步线程调用ready方法获得文件大小信息，之后调用开始方法
	public void start() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				mDownloadHttpTool.ready();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				total = mDownloadHttpTool.getFileSize();
				completeSize = mDownloadHttpTool.getCompeleteSize();
				Log.w("Tag", "downloadedSize::" + completeSize);
				if (onDownloadListener != null) {
					onDownloadListener.downloadStart();
				}
				mDownloadHttpTool.start();
			}
		}.execute();
	}

	public void pause() {
		mDownloadHttpTool.pause();
	}

	public void delete() {
		mDownloadHttpTool.delete();
	}

	public void reset() {
		mDownloadHttpTool.delete();
		start();
	}

	public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
		this.onDownloadListener = onDownloadListener;
	}

	@Override
	public void sendErrorMsg(String msg) {
		onDownloadListener.downloadError(msg);
	}

	@Override
	public void sendSpeedMsg(long used_time) {
		if (used_time == 0)
			return;
		Log.d("下载所用时间", used_time + "");
		double speed = mDownloadHttpTool.getOnceSize() / used_time;
		String speedStr = "";
		if (speed < 1024) {
			speedStr = df.format(speed) + "B/s";
		} else if (speed <= 1024 * 1024) {
			speedStr = df.format(speed / 1024) + "KB/s";
		} else {
			speedStr = df.format(speed / 1024 / 1024) + "MB/s";
		}
		onDownloadListener.downloadSpeed(speedStr);
	}

	@Override
	public void sendProgress(int current_length) {
		synchronized (this) {
			completeSize += current_length;
		}
		int percent =(int) ((float) completeSize /(float) total * 100);
		onDownloadListener.downloadProgress(percent, total, completeSize);
		if (completeSize >= total) {
			mDownloadHttpTool.compelete();
			sendFinishMsg("文件下载完成");
		}
	}

	@Override
	public void sendFinishMsg(String msg) {
		onDownloadListener.downloadFinish(msg);
	}

}