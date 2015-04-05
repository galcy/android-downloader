这是一个文件下载库功能如下：

1.使用开源Weak Handler防止内存泄漏。

2.支持大文件多线程下载;

3.断点续传（下载信息保存在sqlite数据库）,支持暂停，开启，重新下载,

4.支持现在进度和下载速度（瞬时速度👍，不是平均速度）。

使用方法：

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

		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				downloader.start();
			}
		});
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				downloader.pause();
			}
		});
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				downloader.delete();
			}
		});
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				downloader.reset();
			}
		});

