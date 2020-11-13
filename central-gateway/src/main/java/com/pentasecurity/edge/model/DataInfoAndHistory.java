package com.pentasecurity.edge.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataInfoAndHistory extends BaseModel {
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