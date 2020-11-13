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
public class DataFromEdgeToGatewayApiRequest extends BaseModel {
	public DataFromEdgeToGatewayApiRequest(DataFromDeviceToEdgeApiRequest apiRequest, String edgeId) {
		dataId = apiRequest.getDataId();
		dataFormat = apiRequest.getDataFormat();
		deviceId = apiRequest.getDeviceId();
		fromType = "device";
		fromId = apiRequest.getDeviceId();
		toType = "edge";
		toId = edgeId;
		createTime = apiRequest.getCreateTime();
		data = apiRequest.getData();
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