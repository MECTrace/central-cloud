package com.pentasecurity.edge.model.request;

import com.pentasecurity.edge.model.BaseModel;
import com.pentasecurity.edge.util.Sha256Util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataFromDeviceToEdgeApiRequest extends BaseModel {
	public DataFromDeviceToEdgeApiRequest(String deviceId, String data) {
		this.dataId = Sha256Util.sha256(data);
		this.dataFormat = "JSON";
		this.deviceId = deviceId;
		this.createTime = System.currentTimeMillis();
		this.data = data;
	}

    String dataId;
    String dataFormat;
    String deviceId;
    long createTime;
    String data;
}