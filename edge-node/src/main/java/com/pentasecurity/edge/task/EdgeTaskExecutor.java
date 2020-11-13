package com.pentasecurity.edge.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pentasecurity.edge.model.request.DataFromDeviceToEdgeApiRequest;
import com.pentasecurity.edge.model.request.DataFromEdgeToEdgeApiRequest;
import com.pentasecurity.edge.model.request.DataFromEdgeToGatewayApiRequest;
import com.pentasecurity.edge.model.request.HistoryFromEdgeToGatewayApiRequest;
import com.pentasecurity.edge.util.HttpUtil;

@Component
public class EdgeTaskExecutor {
	Logger logger = LoggerFactory.getLogger(EdgeTaskExecutor.class);

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


	@Async("threadPoolTaskExecutor")
	public void upload(DataFromDeviceToEdgeApiRequest apiRequest) {
		// 이미 데이터를 받은 경우
		if ( !write(apiRequest.getDataId(), apiRequest.getData()) ) {
			return;
		}


		try {
			// 게이트로 데이터 업로드 상태를 전송한다.
	    	String[] gates = gateList.split(",");
	    	String gate = "";
	    	if ( gates.length > 0 ) {
	    		gate = gates[(int)Math.floor(Math.random()*gates.length)];
	    	}

	    	DataFromEdgeToGatewayApiRequest apiRequest2 = new DataFromEdgeToGatewayApiRequest(apiRequest, edgeId);
	    	HttpUtil.post(gate+"/api/gw/upload", apiRequest2.toJson());

	    	// 이웃 노드로 데이터를 복사한다.
	    	if ( !StringUtils.isEmpty(nodeList) ) {
		    	String[] nodes = nodeList.split(",");
		    	if ( nodes.length > 0 ) {
		    		DataFromEdgeToEdgeApiRequest apiRequest3 = new DataFromEdgeToEdgeApiRequest(apiRequest, edgeId);

		        	Thread.sleep(copyDelayTime());
		        	HttpUtil.post(nodes[0]+"/api/edge/copy", apiRequest3.toJson());

		        	if ( nodes.length > 1 ) {
		        		Thread.sleep(copyDelayTime());
		        		HttpUtil.post(nodes[1]+"/api/edge/copy", apiRequest3.toJson());
		        	}
		    	}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async("threadPoolTaskExecutor")
	public void copy(DataFromEdgeToEdgeApiRequest apiRequest) {
		// 이미 데이터를 받은 경우
		if ( !write(apiRequest.getDataId(), apiRequest.getData()) ) {
			return;
		}

		try {
			// 게이트로 데이터 복사 상태를 전송한다.
        	String[] gates = gateList.split(",");
        	String gate = "";
        	if ( gates.length > 0 ) {
        		gate = gates[(int)Math.floor(Math.random()*gates.length)];
        	}

        	HistoryFromEdgeToGatewayApiRequest historyApiRequest = new HistoryFromEdgeToGatewayApiRequest(apiRequest, edgeId);
        	HttpUtil.post(gate+"/api/gw/history", historyApiRequest.toJson());


        	// 테스트용
        	// 0.1의 확률로 이웃 노드에 데이터를 복사한다.
        	if ( !StringUtils.isEmpty(nodeList) ) {
		    	String[] nodes = nodeList.split(",");
		    	if ( nodes.length > 0 && Math.random() < 0.1 ) {
		    		DataFromEdgeToEdgeApiRequest apiRequest3 = new DataFromEdgeToEdgeApiRequest(apiRequest, edgeId);

		        	Thread.sleep(copyDelayTime());
		        	HttpUtil.post(nodes[0]+"/api/edge/copy", apiRequest3.toJson());

		        	// 추가 전송 확률은 0.5, 반반이다.
		        	if ( nodes.length > 1 && Math.random() < 0.5 ) {
		        		Thread.sleep(copyDelayTime());
		        		HttpUtil.post(nodes[1]+"/api/edge/copy", apiRequest3.toJson());
		        	}
		    	}
	    	}

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

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

	private long copyDelayTime() {
		return (long)(copyDelayTime * 1000 * (1+Math.random()));
	}

	private long useDelayTime() {
		return (long)(copyDelayTime * 1000 * 10 * (1+Math.random()));
	}

	private long deleteDelayTime() {
		return (long)(copyDelayTime * 1000 * 100 * (1+Math.random()));
	}
}