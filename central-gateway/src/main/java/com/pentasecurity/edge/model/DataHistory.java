package com.pentasecurity.edge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataHistory extends BaseModel {
	String dataId;
    String fromType;
    String fromId;
    String toType;
    String toId;
    String trace;
    long receivedTime;
}