package com.pentasecurity.edge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pentasecurity.edge.model.request.DataFromDeviceToEdgeApiRequest;
import com.pentasecurity.edge.util.DataUtil;
import com.pentasecurity.edge.util.HttpUtil;

@Service
public class CreateDataService
{
	Logger logger = LoggerFactory.getLogger(CreateDataService.class);

    @Value("${edge.device-id}")
    private String deviceId;
    @Value("${edge.node-list}")
    private String nodeList;

	public void createData() {
		try {
			String data = DataUtil.make(100);
			DataFromDeviceToEdgeApiRequest apiRequest = new DataFromDeviceToEdgeApiRequest(deviceId, data);

        	String[] nodes = nodeList.split(",");
        	String node = "";
        	if ( nodes.length > 0 ) {
        		node = nodes[(int)Math.floor(Math.random()*nodes.length)];
        	}

        	HttpUtil.post(node+"/api/edge/upload", apiRequest.toJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}