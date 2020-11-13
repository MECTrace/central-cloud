package com.pentasecurity.edge.model.request;

import com.pentasecurity.edge.model.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class HistoryFromEdgeToGatewayApiRequest extends BaseModel {
	public HistoryFromEdgeToGatewayApiRequest(DataFromEdgeToEdgeApiRequest apiRequest, String edgeId) {
		dataId = apiRequest.getDataId();
		fromType = "edge";
		fromId = apiRequest.getFromId();
		toType = "edge";
		toId = edgeId;
		trace = "copy";
		receivedTime = System.currentTimeMillis();
	}

    String dataId;
    String fromType;
    String fromId;
    String toType;
    String toId;
    String trace;
    long receivedTime;
}