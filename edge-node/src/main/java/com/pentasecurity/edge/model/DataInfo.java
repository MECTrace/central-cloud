package com.pentasecurity.edge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataInfo extends BaseModel {
	public DataInfo(DataTask dataTask, String edgeId) {
		dataId = dataTask.getDataId();
		dataFormat = dataTask.getDataFormat();
	    deviceId = dataTask.getDeviceId();
	    fromId = edgeId;
	    createTime = dataTask.getCreateTime();
	    data = dataTask.getData();
	}

	String dataId;
    String dataFormat;
    String deviceId;
    String fromId;
    long createTime;
    String data;
}