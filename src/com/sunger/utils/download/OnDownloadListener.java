package com.sunger.utils.download;
/**
 * 文件下载监听
 * @author Administrator
 *
 */
public abstract class OnDownloadListener {
	/**
	 * 下载开始
	 */
	public void downloadStart() {
	}

	/**
	 * 下载进度
	 * 
	 * @param percent
	 *            下载的百分比
	 * @param total
	 *            文件总大小
	 * 
	 * @param completeSize
	 *            已下载的文件大小
	 */
	public void downloadProgress(int percent, int total, int completeSize) {
	}

	/**
	 * 下载完成
	 * 
	 * @param msg
	 *            完成的消息：两种情况：1.下载成功，2.文件已经存在
	 */
	public abstract void downloadFinish(String msg);

	/**
	 * 下载实时速度
	 * 
	 * @param speed
	 */
	public void downloadSpeed(String speed) {
	}

	/**
	 * 下载错误
	 * 
	 * @param msg
	 *            错误消息
	 */
	public abstract void downloadError(String msg);
}
