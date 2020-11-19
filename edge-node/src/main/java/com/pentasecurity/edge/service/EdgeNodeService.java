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
import com.pentasecurity.edge.model.DataInfoAndHistory;
import com.pentasecurity.edge.model.DataTask;
import com.pentasecurity.edge.util.HttpUtil;

@Service
public class EdgeNodeService {
	Logger logger = LoggerFactory.getLogger("mainLogger");

	static final public int DATA_TASK_TYPE_UPLOAD = 1;
	static final public int DATA_TASK_TYPE_COPY = 2;
	static final public int DATA_TASK_TYPE_DOWNLOAD = 3;
	static final public int DATA_TASK_TYPE_DELETE = 4;

	@Value("${edge.edge-id}")
    private String edgeId;
    @Value("${edge.node-list}")
    private String nodeList;
    @Value("${edge.gate-list}")
    private String gateList;
    @Value("${edge.storage-path}")
    private String storagePath;
    @Value("${edge.copy-delay-time}")
    private int copyDelayTime;
    @Value("${edge.copy-2nd-rate}")
    private double copy2ndRate;
    @Value("${edge.expire-delay-time}")
    private int expireDelayTime;

    private String[] nodes = null;
    private String[] gates = null;

    @Autowired
    AmoWalletService amoWalletService;

    static ConcurrentHashMap<String, DataTask> taskStorage = new ConcurrentHashMap<String, DataTask>();

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
    }

	/**
	 * device 또는 node에서 전달받은 data를 taskStorage에 저장한다.
	 * @param dataInfo
	 * @param fromDevice
	 */
	public void putToCache(DataInfo dataInfo, int taskType) {
		if ( taskType == DATA_TASK_TYPE_UPLOAD || taskType == DATA_TASK_TYPE_COPY ) {
			// 이미 데이터를 받은 경우
			if ( !write(dataInfo.getDataId(), dataInfo.getData()) ) {
				return;
			}

			try {
				DataTask dataTask = new DataTask(dataInfo, taskType);
				taskStorage.put(dataInfo.getDataId(), dataTask);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param deviceId
	 * @return
	 */
	public ArrayList<String> download(String deviceId) {
		ArrayList<String> data = new ArrayList<String>();

		try {
			Set<String> dataIdSet = taskStorage.keySet();

			for(String dataId : dataIdSet) {
				DataTask dataTask = taskStorage.get(dataId);

				if ( dataTask != null && !dataTask.getDeviceId().equals(deviceId) ) {
					DataInfo dataInfo = new DataInfo(dataTask);
					DataTask newDataTask = new DataTask(dataInfo, DATA_TASK_TYPE_DOWNLOAD, deviceId);

					data.add(dataInfo.toJson());
					taskStorage.put(dataInfo.getDataId(), newDataTask);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * @param deviceId
	 * @return
	 */
	public void delete(String dataId) {
		try {
			File file = new File(storagePath+"/"+dataId+".data");
			if ( file.exists() ) {
				logger.debug(String.format("%10s %10s %5s %10s", edgeId, "delete", "data", dataId));

				file.delete();

				if ( taskStorage.contains(dataId) ) {
					DataTask dataTask = taskStorage.get(dataId);
					if ( dataTask.getTaskType() != DATA_TASK_TYPE_DELETE ) {
						taskStorage.remove(dataId);
					}
				}

				DataTask dataTask2 = new DataTask(dataId, edgeId, DATA_TASK_TYPE_DELETE);
				taskStorage.put(dataId, dataTask2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * taskStorage에 저장된 task들을 처리한다.
	 */
	public void processTask() {
		try {
			Set<String> dataIdSet = taskStorage.keySet();

			for(String dataId : dataIdSet) {
				DataTask dataTask = taskStorage.get(dataId);

				if ( dataTask != null ) {
					reportToCentralGateway(dataTask);
					copyToEdgeNode(dataTask);
					deleteToEdgeNode(dataTask);
					removeDataTask(dataTask);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * central gateway에 데이터 또는 이동 내역을 보고한다.
	 * @param dataTask
	 */
	private void reportToCentralGateway(DataTask dataTask) {
		// central gateway에 보고되지 않은 data를 보고한다.
		if ( dataTask.checkHistoryStatus(gates.length) ) {
			// 여러개의 게이트가 등록되어 있다면 임의로 1개를 선택한다.
	    	String gate = gates[(int)Math.floor(Math.random()*gates.length)];

			if ( dataTask.getTaskType() == DATA_TASK_TYPE_UPLOAD ) {
		    	logger.debug(String.format("%10s %10s %5s %10s", edgeId, "upload", "to", "gateway"));

		    	// 디바이스에서 직접 올라온 데이터를 보고
				DataInfoAndHistory dataInfoAndHistory = new DataInfoAndHistory(dataTask, edgeId);
		    	HttpUtil.post(gate+"/api/gw/upload", dataInfoAndHistory.toJson());
			} else if ( dataTask.getTaskType() == DATA_TASK_TYPE_COPY ) {
				logger.debug(String.format("%10s %10s %5s %10s", edgeId, "copy", "to", "gateway"));

				// 이웃 엣지 노드에서 복제받은 데이터를 보고
				DataHistory dataHistory = new DataHistory(dataTask, edgeId);
	        	HttpUtil.post(gate+"/api/gw/history", dataHistory.toJson());
			} else if ( dataTask.getTaskType() == DATA_TASK_TYPE_DOWNLOAD ) {
				logger.debug(String.format("%10s %10s %5s %10s", edgeId, "use", "to", "gateway"));

				// 단말기가 다운로드 받은 데이터를 보고
				DataHistory dataHistory = new DataHistory(dataTask, edgeId);
	        	HttpUtil.post(gate+"/api/gw/history", dataHistory.toJson());
			} else if ( dataTask.getTaskType() == DATA_TASK_TYPE_DELETE ) {
				logger.debug(String.format("%10s %10s %5s %10s", edgeId, "delete", "to", "gateway"));

				// 데이터 삭제를 보고
				DataHistory dataHistory = new DataHistory(dataTask, edgeId);
	        	HttpUtil.post(gate+"/api/gw/history", dataHistory.toJson());
			}
		}
		dataTask.setHistoryStatus(1);
	}

	/**
	 * 이웃 node에 데이터를 전달한다.
	 * @param dataTask
	 */
	private void copyToEdgeNode(DataTask dataTask) {
		if ( dataTask.checkCopyStatus(nodes.length, copyDelayTime) ) {
			// device에서 업로드된 데이터는 바로 이웃 엣지로 전송
			// 이웃 엣지에서 전달받은 데이터는 일정 확률로 이웃 엣지로 전송(for test)
			if( dataTask.getTaskType() == DATA_TASK_TYPE_UPLOAD || (dataTask.getTaskType() == DATA_TASK_TYPE_COPY && Math.random() < (copy2ndRate/100.0)) ) {
				logger.debug(String.format("%10s %10s %5s %10s", edgeId, "copy", "to", "node#"+dataTask.getCopyStatus()));

				String node = nodes[dataTask.getCopyStatus()];

				DataInfo dataInfo = new DataInfo(dataTask, edgeId);
				HttpUtil.post(node+"/api/edge/copy", dataInfo.toJson());
			}

			// copy 상태확인을 위해 status 값을 증가시킨다.
			dataTask.increaseCopyStatus();
		}
	}

	/**
	 * 이웃 node에 데이터를 전달한다.
	 * @param dataTask
	 */
	private void deleteToEdgeNode(DataTask dataTask) {
		if( dataTask.getTaskType() == DATA_TASK_TYPE_DELETE ) {
			for(int i=0;i<nodes.length;i++) {
				String node = nodes[i];
				logger.debug(String.format("%10s %10s %5s %10s", edgeId, "delete", "to", "node#"+i));

				DataInfo dataInfo = new DataInfo(dataTask, edgeId);
				HttpUtil.post(node+"/api/edge/delete", dataInfo.toJson());
			}
		}
	}

	/**
	 * 작업이 완료된 task는 삭제한다.
	 * @param dataTask
	 */
	private void removeDataTask(DataTask dataTask) {
		// 데이터 복제가 완료된 시점에서 dataTask에서 데이터를 삭제한다.
//		if ( dataTask.isDone(gates.length, nodes.length) ) {
//			taskStorage.remove(dataTask.getDataId());
//
//			logger.debug(edgeId+" : done");
//		}

		// 데이터 사용을 위해 데이터 복제 완료 여부와 관계없이 데이터 유지 시간이 지난 후 dataTask에서 데이터를 삭제한다.
		if ( dataTask.isExpired(gates.length, nodes.length, expireDelayTime) ) {
			taskStorage.remove(dataTask.getDataId());

			logger.debug(String.format("%10s %10s %5s %10s", edgeId, "task"+dataTask.getTaskType(), "is", "expired"));
		}
	}


	/**
	 * 데이터를 지정된 위치에 파일로 생성한다.
	 * @param dataId
	 * @param data
	 * @return
	 */
	private boolean write(String dataId, String data) {
		try {
			File file = new File(storagePath+"/"+dataId+".data");
			if ( !file.exists() ) {
				FileUtils.forceMkdirParent(file);
				FileUtils.writeStringToFile(file, data, "UTF-8");
				return true;
			}
		} catch (Exception e) {
    		e.printStackTrace();
    	}
		return false;
	}
}