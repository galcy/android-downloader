package com.sunger.utils.download;

import java.text.DecimalFormat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 将下载方法封装在此类 提供下载，暂停，删除，以及重置的方法
 */
public class Downloader implements IRepsone, Handler.Callback {

	private DownloadRequest httprequest;
	private int total;
	private int completeSize = 0;
	//用户格式化下载速度的。
	private DecimalFormat df = new DecimalFormat("######0.00");

	private WeakHandler mHandler = new WeakHandler(this);
	private OnDownloadListener listener;

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param threadCount
	 *            开启下载文件的线程数量
	 * @param urlString
	 *            文件url
	 * @param filePath
	 *            文件保存在sd卡的路径
	 * @param filename
	 *            文件名
	 * @param listener
	 *            下载监听
	 */
	public Downloader(Context context, int threadCount, String urlString,
			String filePath, String filename, OnDownloadListener listener) {
		this.listener = listener;
		httprequest = new DownloadRequest(threadCount, urlString, filePath,
				filename, context, mHandler);
	}

	/**
	 * 默认开启五个线程下载
	 * 
	 * @param context
	 *            上下文
	 * @param urlString
	 *            文件url
	 * @param filePath
	 *            文件保存在sd卡的路径
	 * @param filename
	 *            文件名
	 * @param listener
	 *            下载监听
	 */
	public Downloader(Context context, String urlString, String filePath,
			String filename, OnDownloadListener listener) {
		this(context, 5, urlString, filePath, filename, listener);
	}

	/**
	 * 开始下载
	 */
	public void start() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				httprequest.ready();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				total = httprequest.getFileSize();
				completeSize = httprequest.getCompeleteSize();
				Log.w("Tag", "downloadedSize::" + completeSize);
				if (listener != null) {
					listener.onStart();
				}
				httprequest.start();
			}
		}.execute();
	}

	/**
	 * 暂停下载
	 */
	public void pause() {
		httprequest.pause();
	}

	/**
	 * 删除下载
	 */
	public void delete() {
		httprequest.delete();
	}

	/**
	 * 重新下载
	 */
	public void reset() {
		httprequest.delete();
		start();
	}

	@Override
	public void sendErrorMsg(String msg) {
		listener.onError(msg);
	}

	@Override
	public void sendSpeedMsg(long used_time) {
		if (used_time == 0)
			return;
		double speed = httprequest.getOnceSize() / used_time * 1000;
		String speedStr = "";
		if (speed < 1024) {
			speedStr = df.format(speed) + "B/s";
		} else if (speed <= 1024 * 1024) {
			speedStr = df.format(speed / 1024) + "KB/s";
		} else {
			speedStr = df.format(speed / 1024 / 1024) + "MB/s";
		}
		listener.onSpeed(speedStr);
	}

	@Override
	public void sendProgress(int current_length) {
		synchronized (this) {
			completeSize += current_length;
		}
		int percent = (int) ((float) completeSize / (float) total * 100);
		listener.onProgress(percent, total, completeSize);
		if (completeSize >= total) {
			httprequest.compelete();
			sendFinishMsg("文件下载完成");
		}
	}

	@Override
	public void sendFinishMsg(String msg) {
		listener.onFinish(msg);
	}

	@Override
	public boolean handleMessage(Message msg) {
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
		return true;

	}

}