package com.pentasecurity.edge.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pentasecurity.edge.model.DataDownloadRequest;
import com.pentasecurity.edge.model.DataInfo;
import com.pentasecurity.edge.model.DataTask;
import com.pentasecurity.edge.model.DataUploadRequest;
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

    	taskStorage.put(dataTask.getTaskId(), dataTask);
    };

    public void registerDownloadTask(int maxSendCount, int delay, boolean isOnTrace) {
    	DataTask dataTask = new DataTask(DATA_TASK_TYPE_DOWNLOAD, maxSendCount, delay, 0, 0, isOnTrace);

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

    		if ( dataTask.isOnTrace() ) {
    			DataUploadRequest request = new DataUploadRequest(deviceId, data);
    			HttpUtil.post(node+"/api/edge/upload/traceOn", request.toJson());

    			EdgeLogUtil.log(deviceId, "call", deviceId, node, request.toJson(), dataTask.isOnTrace());
    		} else {
    			DataInfo dataInfo = new DataInfo(deviceId, data);
    			HttpUtil.post(node+"/api/edge/upload/traceOff", dataInfo.toJson());

    			EdgeLogUtil.log(deviceId, "call", deviceId, node, dataInfo.toJson(), dataTask.isOnTrace());
    		}

			dataTask.increaseSendCount();
		}
	}

	private void downloadFromEdge(DataTask dataTask) {
		if ( dataTask.checkUpload() ) {
			int nodeNo = (int)Math.floor(Math.random()*nodes.length);
    		String node = nodes[nodeNo];
    		String responseBody = null;

    		if ( dataTask.isOnTrace() ) {
    			DataDownloadRequest request = new DataDownloadRequest("device", deviceId);
    			String url = node+"/api/edge/download/traceOn";
    			responseBody = HttpUtil.post(url, request.toJson());

    			EdgeLogUtil.log(deviceId, "call", deviceId, url, request.toJson(), dataTask.isOnTrace());
    		} else {
    			DataDownloadRequest request = new DataDownloadRequest(null, deviceId);
    			String url = node+"/api/edge/download/traceOff";
    			responseBody = HttpUtil.post(url, request.toJson());

    			EdgeLogUtil.log(deviceId, "call", deviceId, url, request.toJson(), dataTask.isOnTrace());
    		}

    		DataUseApiResponse response = DataUseApiResponse.fromJson(responseBody, DataUseApiResponse.class);

			dataTask.increaseSendCount();

			EdgeLogUtil.log(deviceId, "data download done - "+response.getList().size(), dataTask.isOnTrace());
		}
	}

	private void removeDataTask(DataTask dataTask) {
		if ( dataTask.isDone() ) {
			taskStorage.remove(dataTask.getTaskId());

			EdgeLogUtil.log(deviceId, "task is done : "+dataTask.getTaskId(), dataTask.isOnTrace());
		}
	}
}