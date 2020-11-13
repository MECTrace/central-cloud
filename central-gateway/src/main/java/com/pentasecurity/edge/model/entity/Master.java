package com.pentasecurity.edge.model.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.pentasecurity.edge.model.BaseModel;
import com.pentasecurity.edge.model.DataInfoAndHistory;

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
	public Master(DataInfoAndHistory dataInfo) {
		this.dataId = dataInfo.getDataId();
		this.sourceId = dataInfo.getDeviceId();
		this.createTime = new Date(dataInfo.getCreateTime());
		this.dataFormat = dataInfo.getDataFormat();
		this.data = dataInfo.getData();
	}

    @Id
    String dataId;
    String sourceId;
    Date createTime;
    String dataFormat;
    String data;
}