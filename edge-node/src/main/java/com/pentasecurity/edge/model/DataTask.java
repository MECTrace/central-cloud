package com.pentasecurity.edge.model;

import com.pentasecurity.edge.service.EdgeNodeService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataTask extends DataInfo {
	public DataTask(DataInfo dataInfo, int taskType) {
		dataId = dataInfo.getDataId();
		dataFormat = dataInfo.getDataFormat();
	    deviceId = dataInfo.getDeviceId();
	    fromId = dataInfo.getFromId();
	    createTime = dataInfo.getCreateTime();
	    data = dataInfo.getData();
		timestamp = System.currentTimeMillis();
		this.taskType = taskType;
		historyStatus = 0;
		copyStatus = 0;
	}

	public DataTask(DataInfo dataInfo, int taskType, String toId) {
		this(dataInfo, taskType);
		this.toId = toId;
	}

	public DataTask(String dataId, String fromId, int taskType) {
		this.dataId = dataId;
		dataFormat = "";
	    deviceId = "";
	    fromId = "";
	    createTime = 0;
	    data = "";
		timestamp = System.currentTimeMillis();
		this.taskType = taskType;
		historyStatus = 0;
		copyStatus = 0;
	}

	long timestamp;
	int taskType;
	String toId;
	int historyStatus;
	int copyStatus;

	public boolean checkHistoryStatus(int gateCount) {
		return historyStatus == 0 && gateCount > 0;
	}

	public boolean checkCopyStatus(int nodeCount, int copyDelayTime) {
		if ( (taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_COPY) && copyStatus < nodeCount ) {
			long now = System.currentTimeMillis();
			long due = timestamp + ((copyDelayTime * 1000) * (1+copyStatus));

			return now > due;
		}

		return false;
	}

	public void increaseCopyStatus() {
		copyStatus++;
	}

	public boolean isDone(int gateCount, int nodeCount) {
		if ( gateCount != 0 && historyStatus == 0 ) {
			return false;
		}

		if ( (taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_COPY) ) {
			if ( nodeCount != 0 && copyStatus != nodeCount ) {
				return false;
			}
		}

		return true;
	}

	public boolean isExpired(int gateCount, int nodeCount, int expireDelayTime) {
		if ( taskType == EdgeNodeService.DATA_TASK_TYPE_DOWNLOAD ) {
			return isDone(gateCount, nodeCount);
		} else if ( taskType == EdgeNodeService.DATA_TASK_TYPE_UPLOAD || taskType == EdgeNodeService.DATA_TASK_TYPE_COPY ) {
			// 데이터 만료 시점은 노드가 데이터를 복제받은 시점이 아니라. 데이터가 생성된 시간을 기준으로 한다.
			// (모든 노드에서 동시에 데이터가 무효화됨.)
			long now = System.currentTimeMillis();
			long due = createTime + (expireDelayTime * 1000);

			return now > due;
		} else {
			return true;
		}
	}
}