package com.pentasecurity.edge.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pentasecurity.edge.model.DataInfo;
import com.pentasecurity.edge.model.DataTask;
import com.pentasecurity.edge.model.response.DataUseApiResponse;
import com.pentasecurity.edge.util.DataUtil;
import com.pentasecurity.edge.util.EdgeLogUtil;
import com.pentasecurity.edge.util.HttpUtil;

@Service
public class EdgeDataService
{
	static final public int DATA_TASK_TYPE_UPLOAD = 1;
	static final public int DATA_TASK_TYPE_COPY = 2;
	static final public int DATA_TASK_TYPE_DOWNLOAD = 3;
	static final public int DATA_TASK_TYPE_DELETE = 4;

    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Value("${edge.device-id}")
    private String deviceId;
    @Value("${edge.node-list}")
    private String nodeList;

    private String[] nodes = null;

    static ConcurrentHashMap<String, DataTask> taskStorage = new ConcurrentHashMap<String, DataTask>();

    @PostConstruct
    public void init() {
    	if ( !StringUtils.isEmpty(nodeList) ) {
        	nodes = nodeList.split(",");
    	} else {
    		nodes = new String[0];
    	}
    }

    public void registerUploadTask(int maxSendCount, int delay, int minSize, int maxSize, boolean isOnTrace) {
    	DataTask dataTask = new DataTask(DATA_TASK_TYPE_UPLOAD, maxSendCount, delay, minSize, maxSize, isOnTrace);

    	EdgeLogUtil.log(deviceId, "task1 start : "+dataTask.getTaskId(), dataTask.isOnTrace());

    	taskStorage.put(dataTask.getTaskId(), dataTask);
    };

    public void registerDownloadTask(int maxSendCount, int delay, boolean isOnTrace) {
    	DataTask dataTask = new DataTask(DATA_TASK_TYPE_DOWNLOAD, maxSendCount, delay, 0, 0, isOnTrace);

    	EdgeLogUtil.log(deviceId, "task3 start : "+dataTask.getTaskId(), dataTask.isOnTrace());

    	taskStorage.put(dataTask.getTaskId(), dataTask);
    };

	/**
	 * taskStorage에 저장된 task들을 처리한다.
	 */
	public void processTask() {
		try {
			Set<String> taskIdSet = taskStorage.keySet();

			for(String taskId : taskIdSet) {
				DataTask dataTask = taskStorage.get(taskId);

				if ( dataTask != null ) {
					uploadToEdge(dataTask);
					downloadFromEdge(dataTask);
					removeDataTask(dataTask);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uploadToEdge(DataTask dataTask) {
		if ( dataTask.checkUpload() ) {
			String data = DataUtil.make(dataTask.getRandomDataSize());

			int nodeNo = (int)Math.floor(Math.random()*nodes.length);
    		String node = nodes[nodeNo];

    		DataInfo dataTrace = new DataInfo(deviceId, data);
			String url = node + "/api/edge/upload" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			EdgeLogUtil.log(deviceId, "call", deviceId, url, dataTrace.toJson(), dataTask.isOnTrace());

			HttpUtil.post(url, dataTrace.toJson());

			dataTask.increaseSendCount();
		}
	}

	private void downloadFromEdge(DataTask dataTask) {
		if ( dataTask.checkUpload() ) {
			int nodeNo = (int)Math.floor(Math.random()*nodes.length);
    		String node = nodes[nodeNo];
    		String responseBody = null;

    		DataInfo dataInfo = new DataInfo(deviceId);
			String url = node + "/api/edge/download" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			EdgeLogUtil.log(deviceId, "call", deviceId, url, dataTask.toJson(), dataTask.isOnTrace());

			responseBody = HttpUtil.post(url, dataInfo.toJson());

    		DataUseApiResponse response = DataUseApiResponse.fromJson(responseBody, DataUseApiResponse.class);

			dataTask.increaseSendCount();

			EdgeLogUtil.log(deviceId, "data download done - "+response.getData().size(), dataTask.isOnTrace());
		}
	}

	private void removeDataTask(DataTask dataTask) {
		if ( dataTask.isDone() ) {
			taskStorage.remove(dataTask.getTaskId());

			EdgeLogUtil.log(deviceId, "task"+dataTask.getTaskType()+" done : "+dataTask.getTaskId(), dataTask.isOnTrace());
		}
	}
}