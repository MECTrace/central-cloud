package com.pentasecurity.edge.service;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private String[] nodes = null;
    private String[] gates = null;

    static HashMap<String, DataTask> taskStorage = new HashMap<String, DataTask>();

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
	public void putToCache(DataInfo dataInfo, boolean fromDevice) {
		// 이미 데이터를 받은 경우
		if ( !write(dataInfo.getDataId(), dataInfo.getData()) ) {
			return;
		}

		try {
			DataTask dataTask = new DataTask(dataInfo, fromDevice);
			taskStorage.put(dataInfo.getDataId(), dataTask);
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

			if ( dataTask.isFromDevice() ) {
		    	// 디바이스에서 직접 올라온 데이터를 보고
				DataInfoAndHistory dataInfoAndHistory = new DataInfoAndHistory(dataTask, edgeId);
		    	HttpUtil.post(gate+"/api/gw/upload", dataInfoAndHistory.toJson());
			} else {
				// 이웃 엣지 노드에서 복제받은 데이터를 보고
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
			String node = nodes[dataTask.getCopyStatus()];

			DataInfo dataInfo = new DataInfo(dataTask, edgeId);
			HttpUtil.post(node+"/api/edge/copy", dataInfo.toJson());

			dataTask.increaseCopyStatus();
		}
	}

	/**
	 * 작업이 완료된 task는 삭제한다.
	 * @param dataTask
	 */
	private void removeDataTask(DataTask dataTask) {
		if ( dataTask.isDone(gates.length, nodes.length) ) {
			taskStorage.remove(dataTask.getDataId());
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