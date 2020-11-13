package com.pentasecurity.edge.model.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.pentasecurity.edge.model.BaseModel;
import com.pentasecurity.edge.model.request.DataFromEdgeToGatewayApiRequest;
import com.pentasecurity.edge.model.request.HistoryFromEdgeToGatewayApiRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class History extends BaseModel {
	public History(DataFromEdgeToGatewayApiRequest apiRequest) {
		this.dataId = apiRequest.getDataId();
		this.fromType = apiRequest.getFromType();
		this.fromId = apiRequest.getDeviceId();
		this.toType = apiRequest.getToType();
		this.toId = apiRequest.getToId();
		this.trace = "new";
		this.receivedTime = new Date(apiRequest.getCreateTime());
	}

	public History(HistoryFromEdgeToGatewayApiRequest apiRequest) {
		this.dataId = apiRequest.getDataId();
		this.fromType = apiRequest.getFromType();
		this.fromId = apiRequest.getFromId();
		this.toType = apiRequest.getToType();
		this.toId = apiRequest.getToId();
		this.trace = apiRequest.getTrace();
		this.receivedTime = new Date(apiRequest.getReceivedTime());
	}

    @Id
    int historyId;
    String dataId;
    String fromType;
    String fromId;
    String toType;
    String toId;
    String trace;
    Date receivedTime;
}