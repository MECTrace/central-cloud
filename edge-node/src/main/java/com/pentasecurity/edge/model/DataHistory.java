package com.pentasecurity.edge.model;

import com.pentasecurity.edge.service.EdgeNodeService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class DataHistory extends BaseModel {
	public DataHistory(DataTask dataTask, String edgeId) {
		dataId = dataTask.getDataId();
		if ( dataTask.getTaskType() == EdgeNodeService.DATA_TASK_TYPE_COPY ) {
			this.fromType = "edge";
			fromId = dataTask.getFromId();
			this.toType = "edge";
			toId = edgeId;
			this.trace = "copy";
		} else if ( dataTask.getTaskType() == EdgeNodeService.DATA_TASK_TYPE_DOWNLOAD ) {
			this.fromType = "edge";
			fromId = edgeId;
			this.toType = "device";
			toId = dataTask.getToId();
			this.trace = "use";
		}
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