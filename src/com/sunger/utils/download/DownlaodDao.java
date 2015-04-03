package com.sunger.utils.download;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * 数据库操作工具类
 */
public class DownlaodDao {
    private DownLoadHelper dbHelper;

    public DownlaodDao(Context context) {
        dbHelper = new DownLoadHelper(context);
    }

    /**
     * 创建下载的具体信息
     */
    public void insertInfos(List<DownloadEntity> infos) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (DownloadEntity info : infos) {
            String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,url) values (?,?,?,?,?)";
            Object[] bindArgs = { info.getThreadId(), info.getStartPos(),
                    info.getEndPos(), info.getCompeleteSize(), info.getUrl() };
            database.execSQL(sql, bindArgs);
        }
    }

    /**
     * 得到下载具体信息
     */
    public List<DownloadEntity> getInfos(String urlstr) {
        List<DownloadEntity> list = new ArrayList<DownloadEntity>();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String sql = "select thread_id, start_pos, end_pos,compelete_size,url from download_info where url=?";
        Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
        while (cursor.moveToNext()) {
            DownloadEntity info = new DownloadEntity(cursor.getInt(0),
                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
                    cursor.getString(4));
            list.add(info);
        }
        return list;
    }

    /**
     * 更新数据库中的下载信息
     */
    public void updataInfos(int threadId, int compeleteSize, String urlstr) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String sql = "update download_info set compelete_size=? where thread_id=? and url=?";
        Object[] bindArgs = { compeleteSize, threadId, urlstr };
        database.execSQL(sql, bindArgs);
    }
    /**
     * 关闭数据库
     */
    public void closeDb() {
        dbHelper.close();
    }

    /**
     * 下载完成后删除数据库中的数据
     */
    public void delete(String url) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete("download_info", "url=?", new String[] { url });
    }
}