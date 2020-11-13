package com.pentasecurity.edge.model.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.pentasecurity.edge.model.BaseModel;
import com.pentasecurity.edge.model.request.DataFromEdgeToGatewayApiRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class Master extends BaseModel {
	public Master(DataFromEdgeToGatewayApiRequest apiRequest) {
		this.dataId = apiRequest.getDataId();
		this.sourceId = apiRequest.getDeviceId();
		this.createTime = new Date(apiRequest.getCreateTime());
		this.dataFormat = apiRequest.getDataFormat();
		this.data = apiRequest.getData();
	}

    @Id
    String dataId;
    String sourceId;
    Date createTime;
    String dataFormat;
    String data;
}