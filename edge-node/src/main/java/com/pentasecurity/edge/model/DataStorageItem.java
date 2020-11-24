package com.pentasecurity.edge.model;

import java.util.concurrent.ConcurrentHashMap;

import com.pentasecurity.edge.service.EdgeNodeService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataStorageItem extends BaseModel {
	public DataStorageItem(DataInfo dataInfo, boolean isOnTrace) {
		this.dataInfo = dataInfo;
		this.timestamp = System.currentTimeMillis();
		this.isOnTrace = isOnTrace;
		this.usedDeviceIdMap = new ConcurrentHashMap<String, String>();
	}

	DataInfo dataInfo;
	long timestamp;
	boolean isOnTrace;
	ConcurrentHashMap<String, String> usedDeviceIdMap;

	public boolean checkDownload(String deviceId, boolean isOnTrace) {
		if ( this.isOnTrace == isOnTrace ) {
			if ( deviceId.equals(dataInfo.getDeviceId()) ) {
				return false;
			} else if ( usedDeviceIdMap.containsKey(deviceId) ) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public void use(String deviceId) {
		usedDeviceIdMap.put(deviceId, deviceId);
	}

	public boolean isExpired() {
		long createTime = dataInfo.getCreateTime();
		long now = System.currentTimeMillis();
		long due = createTime + (EdgeNodeService.EXPIRE_DELAY_TIME * 1000);

		return now > due;
	}
}