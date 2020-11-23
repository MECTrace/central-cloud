package com.pentasecurity.edge.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataInfo extends BaseModel {
	String dataId;
    String dataFormat;
    String deviceId;
    long createTime;
    String data;
}