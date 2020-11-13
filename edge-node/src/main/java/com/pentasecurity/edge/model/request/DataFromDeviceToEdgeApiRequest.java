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
public class DataFromDeviceToEdgeApiRequest extends BaseModel {
    String dataId;
    String dataFormat;
    String deviceId;
    long createTime;
    String data;
}