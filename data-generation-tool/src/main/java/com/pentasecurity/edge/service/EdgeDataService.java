package com.pentasecurity.edge.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
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

    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Value("${edge.storage-path}")
    public String storagePath;

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

    public void useData(boolean isOnTrace) {
    	String dataId = getRandomDataId();
    	ServerInfo node = nodes.get((int)Math.floor(Math.random()*nodes.size()));
    	DataInfo req = new DataInfo(dataId, "", deviceId, 0, "");

    	logger.debug(System.currentTimeMillis()+"|"+dataId+"|req|"+node.getServerId());

    	String url = node.getServerUrl() + "/api/edge/use" + (isOnTrace ? "/traceOn" : "/traceOff");
    	String responseBody = HttpUtil.post(url, req.toJson());
    	DataUseApiResponse response = DataUseApiResponse.fromJson(responseBody, DataUseApiResponse.class);

    	EdgeLogUtil.log(deviceId, "call", deviceId, node.getServerId(), url, req.toJson(), isOnTrace);

    	logger.debug(System.currentTimeMillis()+"|"+dataId+"|use|"+response.getDataInfo().toJson());
    }

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

    		logger.debug(System.currentTimeMillis()+"|"+dataInfo.getDataId()+"|create");

			String url = node.getServerUrl() + "/api/edge/upload" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			EdgeLogUtil.log(deviceId, "call", deviceId, node.getServerId(), url, dataInfo.toJson(), dataTask.isOnTrace());

			logger.debug(System.currentTimeMillis()+"|"+dataInfo.getDataId()+"|send|"+deviceId+"|"+node.getServerId());

			write(dataInfo);

			HttpUtil.post(url, dataInfo.toJson());

			dataTask.increaseSendCount();
		}
	}

	private void removeDataTask(DataTask dataTask) {
		if ( dataTask.isDone() ) {
			taskStorage.remove(dataTask.getTaskId());
			logger.debug(System.currentTimeMillis()+"|done");
		}
	}


	/**
	 * 데이터를 지정된 위치에 파일로 생성한다.
	 * @param dataId
	 * @param data
	 * @return
	 */
	private boolean write(DataInfo dataInfo) {
		try {
			File file = new File(storagePath+"/"+dataInfo.getDataId()+".data");
			if ( !file.exists() ) {
				FileUtils.forceMkdirParent(file);
				FileUtils.writeStringToFile(file, dataInfo.toJson(), "UTF-8");
				return true;
			}
		} catch (Exception e) {
    		e.printStackTrace();
    	}
		return false;
	}

	/**
	 * 저장된 파일들에서 임의의 데이터ID를 하나 가져온다.
	 * @param dataId
	 * @param data
	 * @return
	 */
	private String getRandomDataId() {
		try {
			File dir = new File(storagePath);
			String[] filenames = dir.list();
			if ( filenames != null && filenames.length > 0 ) {
				int idx = (int)Math.floor(Math.random()*filenames.length);
				String filename = filenames[idx];
				String dataId = filename.replace(".data", "");

				return dataId;
			}

		} catch (Exception e) {
    		e.printStackTrace();
    	}
		return "";
	}
}