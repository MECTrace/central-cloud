package com.pentasecurity.edge.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pentasecurity.edge.model.DataHistory;
import com.pentasecurity.edge.model.DataInfo;
import com.pentasecurity.edge.model.DataStorageItem;
import com.pentasecurity.edge.model.DataTask;
import com.pentasecurity.edge.model.DataTrace;
import com.pentasecurity.edge.util.EdgeLogUtil;
import com.pentasecurity.edge.util.HttpUtil;

@Service
public class EdgeNodeService {
	Logger logger = LoggerFactory.getLogger("mainLogger");

	static final public int DATA_TASK_TYPE_UPLOAD = 1;
	static final public int DATA_TASK_TYPE_COPY = 2;
	static final public int DATA_TASK_TYPE_DOWNLOAD = 3;

	@Value("${edge.edge-id}")
	public String edgeId;
    @Value("${edge.storage-path}")
    public String storagePath;
    @Value("${edge.node-list}")
    private String nodeList;
    @Value("${edge.gate-list}")
    private String gateList;
    @Value("${edge.copy-delay-time}")
    public int copyDelayTime;
    @Value("${edge.copy-2nd-rate}")
    public double copy2ndRate;
    @Value("${edge.expire-delay-time}")
    public int expireDelayTime;
    @Value("${edge.max-copy-node}")
    public int maxCopyNode;

	static public String EDGE_ID;
    static public String STORAGE_PATH;
    static public int COPY_DELAY_TIME;
    static public double COPY_2ND_RATE;
    static public int EXPIRE_DELAY_TIME;
    static public int MAX_COPY_NODE;

    static public String[] nodes = null;
    static public String[] gates = null;

    @Autowired
    AmoWalletService amoWalletService;

    static ConcurrentHashMap<String, DataTask> taskStorage = new ConcurrentHashMap<String, DataTask>();
    static ConcurrentHashMap<String, DataStorageItem> dataStorage = new ConcurrentHashMap<String, DataStorageItem>();

    @PostConstruct
    public void init() {
    	if ( !StringUtils.isEmpty(nodeList) ) {
        	nodes = nodeList.split(",");
    	} else {
    		nodes = new String[0];
    	}

    	if ( !StringUtils.isEmpty(gateList) ) {
        	gates = gateList.split(",");
    	} else {
    		gates = new String[0];
    	}

    	EDGE_ID = edgeId;
    	STORAGE_PATH = storagePath;
    	COPY_DELAY_TIME = copyDelayTime;
    	COPY_2ND_RATE = copy2ndRate;
    	EXPIRE_DELAY_TIME = expireDelayTime;
    	MAX_COPY_NODE = maxCopyNode;
    }

	public void registerTaskUpload(DataInfo dataInfo, boolean isOnTrace) {
		// 이미 데이터를 받은 경우
		if ( !write(dataInfo) ) {
			return;
		}

		try {
			DataTask dataTask = new DataTask(dataInfo, DATA_TASK_TYPE_UPLOAD, isOnTrace);
			DataStorageItem dataItem = new DataStorageItem(dataInfo, isOnTrace);
			taskStorage.put(dataTask.getTaskId(), dataTask);
			dataStorage.put(dataInfo.getDataId(), dataItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerTaskCopy(DataTrace dataTrace, boolean isOnTrace) {
		// 이미 데이터를 받은 경우
		DataInfo dataInfo = dataTrace.getDataInfo();
		if ( !write(dataInfo) ) {
			return;
		}

		try {
			DataTask dataTask = new DataTask(dataTrace, DATA_TASK_TYPE_COPY, isOnTrace);
			DataStorageItem dataItem = new DataStorageItem(dataInfo, isOnTrace);
			taskStorage.put(dataTask.getTaskId(), dataTask);
			dataStorage.put(dataInfo.getDataId(), dataItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerTaskCopy(DataInfo dataInfo, boolean isOnTrace) {
		// 이미 데이터를 받은 경우
		if ( !write(dataInfo) ) {
			return;
		}

		try {
			DataTask dataTask = new DataTask(dataInfo, DATA_TASK_TYPE_COPY, isOnTrace);
			DataStorageItem dataItem = new DataStorageItem(dataInfo, isOnTrace);
			taskStorage.put(dataTask.getTaskId(), dataTask);
			dataStorage.put(dataInfo.getDataId(), dataItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerTaskDownload(String fromId, DataInfo dataInfo, boolean isOnTrace) {
		try {
			DataTask dataTask = new DataTask(dataInfo, DATA_TASK_TYPE_DOWNLOAD, isOnTrace);
			dataTask.setFromType("device");
			dataTask.setFromId(fromId);

			taskStorage.put(dataTask.getTaskId(), dataTask);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param deviceId
	 * @return
	 */
	public ArrayList<DataInfo> download(DataInfo dataInfo, boolean isOnTrace) {
		ArrayList<DataInfo> data = new ArrayList<DataInfo>();

		try {
			Set<String> dataIdSet = dataStorage.keySet();

			for(String dataId : dataIdSet) {
				DataStorageItem dataItem = dataStorage.get(dataId);

				if ( dataItem != null && dataItem.checkDownload(dataInfo.getDeviceId(), isOnTrace) ) {
					data.add(dataItem.getDataInfo());
					dataItem.use(dataInfo.getDeviceId());

					registerTaskDownload(dataInfo.getDeviceId(), dataItem.getDataInfo(), isOnTrace);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
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
					uploadToCentralGateway(dataTask);
					copyToEdgeNode(dataTask);
					reportToCentralGateway(dataTask);
					removeDataTask(dataTask);
				}
			}

			Set<String> dataIdSet = dataStorage.keySet();

			for(String dataId : dataIdSet) {
				DataStorageItem dataItem = dataStorage.get(dataId);

				if ( dataItem != null ) {
					removeDataStorageItem(dataItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 이웃 node에 데이터를 전달한다.
	 * @param dataTask
	 */
	private void uploadToCentralGateway(DataTask dataTask) {
		if ( dataTask.checkUploadStatus() ) {
			String gate = gates[(int)Math.floor(Math.random()*gates.length)];
			String url = gate+"/api/gw/upload" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			EdgeLogUtil.log(edgeId, "call", edgeId, url, dataTask.getDataInfo().toJson(), dataTask.isOnTrace());

	    	// 디바이스에서 직접 올라온 데이터를 보고
	    	HttpUtil.post(url, dataTask.getDataInfo().toJson());

			try {
		    	amoWalletService.registerTx(dataTask.getDataInfo().getDataId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dataTask.setUploadStatusDone();
	}

	/**
	 * 이웃 node에 데이터를 전달한다.
	 * @param dataTask
	 */
	private void copyToEdgeNode(DataTask dataTask) {
		if ( dataTask.checkCopyStatus() ) {
			// device에서 업로드된 데이터는 바로 이웃 엣지로 전송
			// 이웃 엣지에서 전달받은 데이터는 일정 확률로 이웃 엣지로 전송(for test)
			String node = dataTask.getCopyNode();
			String url = node+"/api/edge/copy" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

			if ( dataTask.isOnTrace() ) {
				DataTrace dataTrace = new DataTrace(dataTask.getDataInfo(), "edge", edgeId);

				EdgeLogUtil.log(edgeId, "call", edgeId, url, dataTrace.toJson(), dataTask.isOnTrace());

				HttpUtil.post(url, dataTrace.toJson());
			} else {
				EdgeLogUtil.log(edgeId, "call", edgeId, url, dataTask.getDataInfo().toJson(), dataTask.isOnTrace());

				HttpUtil.post(url, dataTask.getDataInfo().toJson());
			}
			// copy 상태확인을 위해 status 값을 증가시킨다.
			dataTask.increaseCopyStatus();
		}
	}

	/**
	 * central gateway에 데이터 또는 이동 내역을 보고한다.
	 * @param dataTask
	 */
	private void reportToCentralGateway(DataTask dataTask) {
		// central gateway에 보고되지 않은 data를 보고한다.
		if ( dataTask.checkHistoryStatus() ) {
			// 여러개의 게이트가 등록되어 있다면 임의로 1개를 선택한다.
	    	String gate = gates[(int)Math.floor(Math.random()*gates.length)];
	    	String url = gate+"/api/gw/history" + (dataTask.isOnTrace() ? "/traceOn" : "/traceOff");

    		DataHistory dataHistory = dataTask.getDataHistory();

	    	EdgeLogUtil.log(edgeId, "call", edgeId, url, dataHistory.toJson(), dataTask.isOnTrace());

	    	HttpUtil.post(url, dataHistory.toJson());
		}
		dataTask.setHistoryStatusDone();
	}

	/**
	 * 작업이 완료된 task는 삭제한다.
	 * @param dataTask
	 */
	private void removeDataTask(DataTask dataTask) {
		if ( dataTask.isDone() ) {
			taskStorage.remove(dataTask.getTaskId());
		}
	}


	/**
	 * 시간이 만료된 데이터는 삭제한다.
	 * @param dataItem
	 */
	private void removeDataStorageItem(DataStorageItem dataItem) {
		if ( dataItem.isExpired() ) {
			dataStorage.remove(dataItem.getDataInfo().getDataId());
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
			File file = new File(STORAGE_PATH+"/"+dataInfo.getDataId()+".data");
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
}