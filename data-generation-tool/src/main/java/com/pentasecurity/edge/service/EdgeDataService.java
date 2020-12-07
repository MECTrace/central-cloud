package com.pentasecurity.edge.service;

import java.util.ArrayList;
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
import com.pentasecurity.edge.model.ServerInfo;
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

    static public ArrayList<ServerInfo> nodes = null;

    static ConcurrentHashMap<String, DataTask> taskStorage = new ConcurrentHashMap<String, DataTask>();

    @PostConstruct
    public void init() {
    	nodes = new ArrayList<ServerInfo>();

    	if ( !StringUtils.isEmpty(nodeList) ) {
        	for(String node : nodeList.split(";")) {
        		if ( !StringUtils.isEmpty(node) ) {
        			String[] info = node.split(",");
        			if ( info.length == 2 ) {
        				nodes.add(new ServerInfo(info[0], info[1]));
        			}
        		}
        	}
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
    		ServerInfo node = nodes.get((int)Math.floor(Math.random()*nodes.size()));

    		DataInfo dataInfo = new DataInfo(deviceId, data);

    		logger.debug("create data : "+dataInfo.getDataId());

			String url = node.getServerUrl() + "/api/edge/upload" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			EdgeLogUtil.log(deviceId, "call", deviceId, node.getServerId(), url, dataInfo.toJson(), dataTask.isOnTrace());

			HttpUtil.post(url, dataInfo.toJson());

			dataTask.increaseSendCount();
		}
	}

	private void downloadFromEdge(DataTask dataTask) {
		if ( dataTask.checkDownload() ) {
    		ServerInfo node = nodes.get((int)Math.floor(Math.random()*nodes.size()));
    		String responseBody = null;

    		DataInfo dataInfo = new DataInfo(deviceId);
			String url = node.getServerUrl() + "/api/edge/download" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			EdgeLogUtil.log(deviceId, "call", deviceId, node.getServerId(), url, dataTask.toJson(), dataTask.isOnTrace());

			responseBody = HttpUtil.post(url, dataInfo.toJson());

    		DataUseApiResponse response = DataUseApiResponse.fromJson(responseBody, DataUseApiResponse.class);

			dataTask.increaseSendCount();

			EdgeLogUtil.log(deviceId, "download", deviceId, node.getServerId(), url, response.toJson(), dataTask.isOnTrace());
		}
	}

	private void removeDataTask(DataTask dataTask) {
		if ( dataTask.isDone() ) {
			taskStorage.remove(dataTask.getTaskId());
		}
	}
}