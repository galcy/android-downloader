这是一个基于HttpURLConnection网络请求和sqlite3数据存储的文件下载库。
主要有以下功能：

1.使用开源库android-weak-handler解决handler内存泄漏问题；

2.支持断电续传（实时保存下载信息），暂停下载，重新下载，删除下载；

3.支持下载速度和文件下载进度实时回调；

4.支持自动创建多级目录；

使用方法如下：

		String urlString = "url";
		String localPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/local";
		downloader = new Downloader(this, urlString, localPath,
				"weibo_1790.apk", new OnDownloadListener() {

					@Override
					public void onFinish(String msg) {

					}

					@Override
					public void onError(String msg) {

					}

					@Override
					public void onProgress(int percent, int total,
							int completeSize) {
						mProgressBar.setProgress(percent);
						tv_total.setText(completeSize + "/" + total);
					}

					@Override
					public void onSpeed(String speed) {
						super.onSpeed(speed);
						tv_speed.setText("下载速度" + speed);

					}

				});
			//开始下载
			downloader.start();
			//暂停下载
			downloader.pause();
			//删除下载
			downloader.delete();
			//重新下载
			downloader.reset();
	
