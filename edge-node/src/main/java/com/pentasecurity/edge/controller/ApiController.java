package com.pentasecurity.edge.controller;

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

    	logger.debug(edgeId+" : upload  from "+dataInfo.getDeviceId());

    	try {
    		edgeNodeService.putToCache(dataInfo, true);

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

    	logger.debug(edgeId+" : copy    from "+dataInfo.getFromId());

    	try {
    		edgeNodeService.putToCache(dataInfo, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}