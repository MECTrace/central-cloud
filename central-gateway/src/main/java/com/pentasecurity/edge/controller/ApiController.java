package com.pentasecurity.edge.controller;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pentasecurity.edge.model.DataHistory;
import com.pentasecurity.edge.model.DataInfo;
import com.pentasecurity.edge.model.entity.History;
import com.pentasecurity.edge.model.entity.Master;
import com.pentasecurity.edge.model.response.ApiResponse;
import com.pentasecurity.edge.repository.HistoryRepository;
import com.pentasecurity.edge.repository.MasterRepository;
import com.pentasecurity.edge.util.EdgeLogUtil;

@Controller
@RequestMapping("/api/gw")
public class ApiController {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Value("${edge.gateway-id}")
    private String gatewayId;
    @Value("${edge.storage-path}")
    private String storagePath;

    @Autowired
    MasterRepository masterRepository;
    @Autowired
    HistoryRepository historyRepository;

    @PostMapping("/upload/traceOn")
    @ResponseBody
    public ApiResponse uploadTraceOn(@RequestBody DataInfo dataInfo) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		EdgeLogUtil.log(gatewayId, "in", "", gatewayId, "/api/gw/upload/traceOn", dataInfo.toJson(), true);
    		logger.debug(System.currentTimeMillis()+"|"+dataInfo.getDataId()+"|recv");

    		Master master = new Master(dataInfo);
    		masterRepository.save(master);

    		File file = new File(storagePath+"/"+dataInfo.getDataId()+".data");

			FileUtils.forceMkdirParent(file);
			FileUtils.writeStringToFile(file, dataInfo.toJson(), "UTF-8");

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/upload/traceOff")
    @ResponseBody
    public ApiResponse uploadTraceOff(@RequestBody DataInfo dataInfo) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		EdgeLogUtil.log(gatewayId, "in", "", gatewayId, "/api/gw/upload/traceOff", dataInfo.toJson(), false);

    		Master master = new Master(dataInfo);
    		masterRepository.save(master);

    		File file = new File(storagePath+"/"+dataInfo.getDataId()+".data");

			FileUtils.forceMkdirParent(file);
			FileUtils.writeStringToFile(file, dataInfo.toJson(), "UTF-8");

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/history/traceOn")
    @ResponseBody
    public ApiResponse historyTraceOn(@RequestBody DataHistory dataHistory) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	EdgeLogUtil.log(gatewayId, "in", dataHistory.getToId(), gatewayId, "/api/gw/history/traceOn", dataHistory.toJson(), true);
    	logger.debug(System.currentTimeMillis()+"|"+dataHistory.getDataId()+"|hist|"+dataHistory.getFromId()+"|"+dataHistory.getToId()+"|"+gatewayId);

    	try {
    		History history = new History(dataHistory);
    		historyRepository.save(history);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/history/traceOff")
    @ResponseBody
    public ApiResponse history(@RequestBody DataHistory dataHistory) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	EdgeLogUtil.log(gatewayId, "in", "", gatewayId, "/api/gw/history/traceOff", dataHistory.toJson(), false);

    	try {
    		History history = new History(dataHistory);
    		historyRepository.save(history);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}