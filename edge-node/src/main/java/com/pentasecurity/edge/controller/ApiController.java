package com.pentasecurity.edge.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pentasecurity.edge.model.DataInfo;
import com.pentasecurity.edge.model.response.ApiResponse;
import com.pentasecurity.edge.model.response.DataUseApiResponse;
import com.pentasecurity.edge.service.EdgeNodeService;

@Controller
@RequestMapping("/api/edge")
public class ApiController {
    Logger logger = LoggerFactory.getLogger("mainLogger");

	@Value("${edge.edge-id}")
    private String edgeId;

    @Autowired
    EdgeNodeService edgeNodeService;

    @PostMapping("/upload")
    @ResponseBody
    public ApiResponse upload(@RequestBody DataInfo dataInfo) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	logger.debug(edgeId+" : upload   from "+dataInfo.getDeviceId());

    	try {
    		edgeNodeService.putToCache(dataInfo, EdgeNodeService.DATA_TASK_TYPE_UPLOAD);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/copy")
    @ResponseBody
    public ApiResponse copy(@RequestBody DataInfo dataInfo) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	logger.debug(edgeId+" : copy     from "+dataInfo.getFromId());

    	try {
    		edgeNodeService.putToCache(dataInfo, EdgeNodeService.DATA_TASK_TYPE_COPY);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/download")
    @ResponseBody
    public DataUseApiResponse download(@RequestBody HashMap<String, String> request) {
    	DataUseApiResponse apiResponse = new DataUseApiResponse(-99, "error");

    	logger.debug(edgeId+" : download to   "+request.get("deviceId"));

    	try {
    		ArrayList<String> data = edgeNodeService.download(request.get("deviceId"));

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
        	apiResponse.setData(data);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}