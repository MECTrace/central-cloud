package com.pentasecurity.edge.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.pentasecurity.edge.service.EdgeNodeService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataTask extends BaseModel {
	public DataTask(DataInfo dataInfo, int taskType, boolean isOnTrace) {
		this.taskId = UUID.randomUUID().toString();
		this.dataInfo = dataInfo;
		this.timestamp = System.currentTimeMillis();
		this.taskType = taskType;
		this.uploadStatus = 0;
		this.copyCount = 0;
		this.historyStatus = 0;
		this.isOnTrace = isOnTrace;
		this.usedDeviceIdList = new ArrayList<String>();

		if ( taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_DOWNLOAD ) {
			this.fromType = "device";
			this.fromId = dataInfo.getDeviceId();
		}

		if ( taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_COPY ) {
			copyNodeList = new ArrayList<String>(Arrays.asList(EdgeNodeService.nodes));
			Collections.shuffle(copyNodeList);
		}
	}

	public DataTask(DataTrace dataTrace, int taskType, boolean isOnTrace) {
		this(dataTrace.getDataInfo(), taskType, isOnTrace);
		this.fromType = dataTrace.getFromType();
		this.fromId = dataTrace.getFromId();
	}

	String taskId;
	DataInfo dataInfo;
	String fromId;
	String fromType;
	long timestamp;
	int taskType;
	int uploadStatus;
	int copyCount;
	int historyStatus;
	boolean isOnTrace;
	ArrayList<String> copyNodeList;
	ArrayList<String> usedDeviceIdList;

	public boolean checkUploadStatus() {
		return uploadStatus == 0;
	}

	public void setUploadStatusDone() {
		uploadStatus = 1;
	}

	public boolean checkCopyStatus() {
		if ( (taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || (taskType == EdgeNodeService.DATA_TASK_TYPE_COPY && (EdgeNodeService.COPY_2ND_RATE < Math.random()*100))) ) {
			if ( copyCount < EdgeNodeService.nodes.length && copyCount < EdgeNodeService.MAX_COPY_NODE ) {
				long now = System.currentTimeMillis();
				long due = timestamp + ((EdgeNodeService.COPY_DELAY_TIME * 1000) * (1+copyCount));

				return now > due;
			}
		}

		return false;
	}

	public String getCopyNode() {
		return copyNodeList.get(copyCount);
	}

	public void increaseCopyStatus() {
		copyCount++;
	}

	public boolean checkHistoryStatus() {
		return isOnTrace && historyStatus == 0;
	}

	public DataHistory getDataHistory() {
		DataHistory dataHistory = null;
		String dataId = dataInfo.getDataId();

		try {
			if ( taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD ) {
		    	// 디바이스에서 직접 올라온 데이터를 보고
		    	dataHistory = new DataHistory(dataId, fromType, fromId, "edge", EdgeNodeService.EDGE_ID, "new", timestamp);
			} else if ( taskType == EdgeNodeService.DATA_TASK_TYPE_COPY ) {
				// 이웃 엣지 노드에서 복제받은 데이터를 보고
				dataHistory = new DataHistory(dataId, fromType, fromId, "edge", EdgeNodeService.EDGE_ID, "copy", timestamp);
			} else if ( taskType == EdgeNodeService.DATA_TASK_TYPE_DOWNLOAD ) {
				// 단말기가 다운로드 받은 데이터를 보고
				dataHistory = new DataHistory(dataId, "edge", EdgeNodeService.EDGE_ID, fromType, fromId, "use", timestamp);
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

		return dataHistory;
	}

	public void setHistoryStatusDone() {
		historyStatus = 1;
	}

	public boolean isUsed(String deviceId) {
		if ( deviceId.equals(dataInfo.getDeviceId()) ) {
			return false;
		} else if ( usedDeviceIdList.indexOf(deviceId) > -1 ) {
			return false;
		}
		return true;
	}

	public void use(String deviceId) {
		usedDeviceIdList.add(deviceId);
	}

	public boolean isDone() {
		if ( taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD && uploadStatus == 0 ) {
			return false;
		}

		if ( isOnTrace && historyStatus == 0 ) {
			return false;
		}

		if ( (taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_COPY) ) {
			if ( copyCount < EdgeNodeService.nodes.length && copyCount < EdgeNodeService.MAX_COPY_NODE ) {
				return false;
			}
		}

		return true;
	}

	public boolean isExpired() {
		if ( taskType == EdgeNodeService.DATA_TASK_TYPE_DOWNLOAD ) {
			return isDone();
		} else if ( taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_COPY ) {
			// 데이터 만료 시점은 노드가 데이터를 복제받은 시점이 아니라. 데이터가 생성된 시간을 기준으로 한다.
			// (모든 노드에서 동시에 데이터가 무효화됨.)
			long createTime = dataInfo.getCreateTime();
			long now = System.currentTimeMillis();
			long due = createTime + (EdgeNodeService.EXPIRE_DELAY_TIME * 1000);

			return now > due;
		} else {
			return true;
		}
	}
}