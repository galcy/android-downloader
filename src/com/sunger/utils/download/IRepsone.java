package com.sunger.utils.download;

public interface IRepsone {

	void sendSpeedMsg(long used_time);

	void sendProgress(int current_length);

	void sendErrorMsg(String msg);

	void sendFinishMsg(String msg);
}
