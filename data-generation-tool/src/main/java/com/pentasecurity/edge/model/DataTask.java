package com.pentasecurity.edge.model;

import java.util.UUID;

import com.pentasecurity.edge.service.EdgeDataService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataTask extends BaseModel {
	public DataTask(int taskType, int maxSendCount, int delay, int minSize, int maxSize, boolean isOnTrace) {
		this.taskId = UUID.randomUUID().toString();
		this.taskType = taskType;
		this.timestamp = System.currentTimeMillis();
		this.maxSendCount = maxSendCount;
		this.sendCount = 0;
		this.delay = delay;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.isOnTrace = isOnTrace;
	}

	String taskId;
	int taskType;
	long timestamp;
    int maxSendCount;
    int sendCount;
    int delay;
    int minSize;
    int maxSize;
    boolean isOnTrace;

    public boolean checkUpload() {
    	if ( taskType == EdgeDataService.DATA_TASK_TYPE_UPLOAD ) {
    		if ( sendCount < maxSendCount ) {
	    		long now = System.currentTimeMillis();
				long due = timestamp + (sendCount * delay * 1000);

				return now > due;
    		}
    	}

    	return false;
    }

    public boolean checkDownload() {
    	if ( taskType == EdgeDataService.DATA_TASK_TYPE_DOWNLOAD ) {
    		if ( sendCount < maxSendCount ) {
	    		long now = System.currentTimeMillis();
				long due = timestamp + (sendCount * delay * 1000);

				return now > due;
    		}
    	}

    	return false;
    }

    public int getRandomDataSize() {
    	return minSize + (int)(Math.floor((maxSize-minSize+1) * Math.random()));
    }

	public void increaseSendCount() {
		sendCount++;
	}

	public boolean isDone() {
		if ( taskType == EdgeDataService.DATA_TASK_TYPE_UPLOAD ) {
			return sendCount >= maxSendCount;
		} else if ( taskType == EdgeDataService.DATA_TASK_TYPE_DOWNLOAD ) {
			return sendCount >= maxSendCount;
		}

		return true;

	}
}