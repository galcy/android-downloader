这是一个文件下载库功能如下：

1.使用开源Weak Handler防止内存泄漏。

2.支持大文件多线程下载;

3.断点续传（下载信息保存在sqlite数据库）,支持暂停，开启，重新下载,

4.支持现在进度和下载速度（瞬时速度👍，不是平均速度）。

使用方法：
//文件下载地址
String urlString = "http://gdown.baidu.com/data/wisegame/7810ca9719335544/weibo_1790.apk";
//文件绝对路径
String localPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/local";
Downloader	downloader = new Downloader(this, urlString, localPath,
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
					
					}

					@Override
					public void onSpeed(String speed) {
				

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




