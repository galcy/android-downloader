package com.sunger.utils.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Message;
import android.util.Log;

public class DownloadRequest implements IRepsone {
	/**
	 * 
	 * 利用Http协议进行多线程下载具体实践类
	 */

	private static final String TAG = DownloadRequest.class.getSimpleName();

	private int threadCount;// 线程数量
	private String urlstr;// URL地址
	private Context mContext;
	//使用WeakHandler防止内存泄漏
	private WeakHandler mHandler;
	private List<DownloadEntity> downloadInfos;// 保存下载信息的类

	private String localPath;// 目录
	private String fileName;// 文件名
	private int fileSize;
	private Dao dao;// 文件信息保存的数据库操作类

	public int getOnceSize() {
		return onceSize;
	}

	public void setOnceSize(int onceSize) {
		this.onceSize = onceSize;
	}

	/**
	 * 一次写入的数据，默认写入4k，如果一次写入文件太小，写入时间容易出错
	 */
	private int onceSize = 1024 * 4;

	private enum Download_State {
		Downloading, Pause, Ready;// 利用枚举表示下载的三种状态
	}

	private Download_State state = Download_State.Ready;// 当前下载状态
	// 所有线程下载的总数
	private int globalCompelete = 0;

	public DownloadRequest(int threadCount, String urlString, String localPath,
			String fileName, Context context, WeakHandler handler) {
		super();
		this.threadCount = threadCount;
		this.urlstr = urlString;
		this.localPath = localPath;
		this.mContext = context;
		this.mHandler = handler;
		this.fileName = fileName;
		dao = new Dao(mContext);
	}

	/**
	 * 在开始下载之前需要调用ready方法进行配置,主要用于初始化每个线程下载长度的
	 */
	public void ready() {
		Log.w(TAG, "ready");
		globalCompelete = 0;
		downloadInfos = dao.getInfos(urlstr);
		if (downloadInfos.size() == 0) {
			initFirst();
		} else {
			File file = new File(localPath + "/" + fileName);
			if (!file.exists()) {
				dao.delete(urlstr);
				initFirst();
			} else {
				fileSize = downloadInfos.get(downloadInfos.size() - 1)
						.getEndPos();
				for (DownloadEntity info : downloadInfos) {
					globalCompelete += info.getCompeleteSize();
				}
				Log.w(TAG, "globalCompelete:::" + globalCompelete);
			}
		}
	}

	/**
	 * 开始下载
	 */
	public void start() {
		Log.w(TAG, "start");
		if (downloadInfos != null) {
			if (state == Download_State.Downloading) {
				return;
			}
			state = Download_State.Downloading;
			for (DownloadEntity info : downloadInfos) {
				Log.v(TAG, "startThread");
				new DownloadThread(info.getThreadId(), info.getStartPos(),
						info.getEndPos(), info.getCompeleteSize(),
						info.getUrl()).start();
			}
		}
	}

	/**
	 * 暂停下载
	 */
	public void pause() {
		state = Download_State.Pause;
		dao.closeDb();
	}

	/**
	 * 删除下载
	 */
	public void delete() {
		compelete();
		File file = new File(localPath + "/" + fileName);
		file.delete();
	}

	/**
	 * 结束下载
	 */
	public void compelete() {
		dao.delete(urlstr);
		dao.closeDb();
	}

	/**
	 * 获取文件大小
	 * 
	 * @return
	 */
	public int getFileSize() {
		return fileSize;
	}

	/**
	 * 获取已经下载完成的长度
	 * 
	 * @return
	 */
	public int getCompeleteSize() {
		return globalCompelete;
	}

	private File createFile() {
		File fileParent = new File(localPath);
		// 判断文件夹是否存在，如果不存在则创建目录，支持多极目录
		if (!fileParent.exists()) {
			// 文件夹创建失败
			if (!fileParent.mkdirs()) {
				sendErrorMsg("请检测您的sd卡");
				return null;
			}
		}
		File file = new File(fileParent, fileName);
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					// 文件创建失败
					sendErrorMsg("请检测您的sd卡");
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				sendErrorMsg("创建文件失败");
			}
		} else {
			// 文件已经存在，并且没有下载记录
			sendFinishMsg("文件已经存在");
			return null;
		}
		return file;

	}

	/**
	 * 第一次下载初始化,判断文件是否已经存在和获取文件大小
	 */
	private void initFirst() {
		Log.w(TAG, "initFirst");

		File file = createFile();
		if (file == null)
			return;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlstr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength();
			Log.w(TAG, "fileSize::" + fileSize);
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(fileSize);
			accessFile.close();
		} catch (IOException e) {
			sendErrorMsg("初始化下载错误");
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		int range = fileSize / threadCount;
		downloadInfos = new ArrayList<DownloadEntity>();
		for (int i = 0; i < threadCount - 1; i++) {
			DownloadEntity info = new DownloadEntity(i, i * range, (i + 1)
					* range - 1, 0, urlstr);
			downloadInfos.add(info);
		}
		DownloadEntity info = new DownloadEntity(threadCount - 1,
				(threadCount - 1) * range, fileSize - 1, 0, urlstr);
		downloadInfos.add(info);
		dao.insertInfos(downloadInfos);
	}

	// 自定义下载线程
	private class DownloadThread extends Thread {

		private int threadId;
		private int startPos;
		private int endPos;
		private int compeleteSize;
		private String urlstr;
		private int totalThreadSize;

		public DownloadThread(int threadId, int startPos, int endPos,
				int compeleteSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			totalThreadSize = endPos - startPos + 1;
			this.urlstr = urlstr;
			this.compeleteSize = compeleteSize;
		}

		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			try {
				randomAccessFile = new RandomAccessFile(localPath + "/"
						+ fileName, "rwd");
				randomAccessFile.seek(startPos + compeleteSize);
				URL url = new URL(urlstr);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Range", "bytes="
						+ (startPos + compeleteSize) + "-" + endPos);
				is = connection.getInputStream();
				byte[] buffer = new byte[onceSize];
				int length = -1;
				long time = System.currentTimeMillis();
				while ((length = is.read(buffer)) != -1) {
					randomAccessFile.write(buffer, 0, length);
					long current_time = System.currentTimeMillis();
					sendSpeedMsg(current_time - time);
					time = current_time;
					sendProgress(length);
					compeleteSize += length;
					dao.updataInfos(threadId, compeleteSize, urlstr);
					Log.w(TAG, "Threadid::" + threadId + "    compelete::"
							+ compeleteSize + "    total::" + totalThreadSize);
					if (compeleteSize >= totalThreadSize) {
						break;
					}
					if (state != Download_State.Downloading) {
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				sendErrorMsg("文件下载错误");

			} finally {
				try {
					if (is != null)
						is.close();
					if (randomAccessFile != null)
						randomAccessFile.close();
					if (connection != null)
						connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
					sendErrorMsg("下载错误");
				}
			}
		}
	}

	@Override
	public void sendFinishMsg(String msg) {
		sendResponeMsg(MsgState.DOWNLOAD_FINISH, msg);
	}

	@Override
	public void sendErrorMsg(String msg) {
		sendResponeMsg(MsgState.DOWNLOAD_ERROT, msg);
	}

	private long start_time = System.currentTimeMillis();

	@Override
	public void sendSpeedMsg(long used_time) {
		long current_time = System.currentTimeMillis();
		if (current_time - start_time < 1000)
			return;
		start_time = current_time;
		sendResponeMsg(MsgState.DOWNLOAD_SPEED, used_time);
	}

	@Override
	public void sendProgress(int current_length) {
		sendResponeMsg(MsgState.DOWNLOAD_PROGRESS, current_length);
	}

	private void sendResponeMsg(int state, Object msg) {
		Message message = Message.obtain();
		message.what = state;
		message.obj = msg;
		mHandler.sendMessage(message);
	}

}