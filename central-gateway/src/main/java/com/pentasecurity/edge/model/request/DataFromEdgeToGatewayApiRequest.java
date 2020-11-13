package com.pentasecurity.edge.model.request;

import com.pentasecurity.edge.model.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataFromEdgeToGatewayApiRequest extends BaseModel {
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