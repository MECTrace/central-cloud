package com.pentasecurity.edge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataTask extends DataInfo {
	public DataTask(DataInfo dataInfo, boolean fromDevice) {
		dataId = dataInfo.getDataId();
		dataFormat = dataInfo.getDataFormat();
	    deviceId = dataInfo.getDeviceId();
	    fromId = dataInfo.getFromId();
	    createTime = dataInfo.getCreateTime();
	    data = dataInfo.getData();
		timestamp = System.currentTimeMillis();
		this.fromDevice = fromDevice;
		historyStatus = 0;
		copyStatus = 0;
	}

	long timestamp;
	boolean fromDevice;
	int historyStatus;
	int copyStatus;

	public boolean checkHistoryStatus(int gateCount) {
		return historyStatus == 0 && gateCount > 0;
	}

	public boolean checkCopyStatus(int nodeCount, int copyDelayTime) {
		if ( copyStatus < nodeCount ) {
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

		if ( nodeCount != 0 && copyStatus != nodeCount ) {
			return false;
		}

		return true;
	}
}