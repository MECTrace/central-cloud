package com.pentasecurity.edge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataInfoAndHistory extends BaseModel {
	public DataInfoAndHistory(DataTask dataTask, String edgeId) {
		dataId = dataTask.getDataId();
		dataFormat = dataTask.getDataFormat();
		deviceId = dataTask.getDeviceId();
		fromType = "device";
		fromId = dataTask.getDeviceId();
		toType = "edge";
		toId = edgeId;
		createTime = dataTask.getCreateTime();
		data = dataTask.getData();
	}

	String dataId;
    String dataFormat;
    String deviceId;
    String fromType;
    String fromId;
    String toType;
    String toId;
    long createTime;
    String data;
}