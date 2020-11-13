package com.pentasecurity.edge.model.request;

import com.pentasecurity.edge.model.BaseModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataFromEdgeToEdgeApiRequest extends BaseModel {
	public DataFromEdgeToEdgeApiRequest(DataFromDeviceToEdgeApiRequest apiRequest, String edgeId) {
		dataId = apiRequest.getDataId();
		dataFormat = apiRequest.getDataFormat();
	    deviceId = apiRequest.getDeviceId();
	    fromId = edgeId;
	    createTime = apiRequest.getCreateTime();
	    data = apiRequest.getData();
	}

	public DataFromEdgeToEdgeApiRequest(DataFromEdgeToEdgeApiRequest apiRequest, String edgeId) {
		dataId = apiRequest.getDataId();
		dataFormat = apiRequest.getDataFormat();
	    deviceId = apiRequest.getDeviceId();
	    fromId = edgeId;
	    createTime = apiRequest.getCreateTime();
	    data = apiRequest.getData();
	}

    String dataId;
    String dataFormat;
    String deviceId;
    String fromId;
    long createTime;
    String data;
}