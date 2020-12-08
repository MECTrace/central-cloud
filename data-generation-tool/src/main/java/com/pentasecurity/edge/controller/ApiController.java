package com.pentasecurity.edge.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pentasecurity.edge.model.response.ApiResponse;
import com.pentasecurity.edge.service.EdgeDataService;

@Controller
@RequestMapping("/api/device")
public class ApiController {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    EdgeDataService edgeDataService;

    @GetMapping("/create/data/traceOn")
    @ResponseBody
    public ApiResponse createDataTraceOn(HttpServletRequest request, int maxSendCount, int delay, int minSize, int maxSize) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		logger.debug(System.currentTimeMillis()+"|start|"+maxSendCount);

    		edgeDataService.registerUploadTask(maxSendCount, delay, minSize, maxSize, true);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/create/data/traceOff")
    @ResponseBody
    public ApiResponse createDataTraceOff(HttpServletRequest request, int maxSendCount, int delay, int minSize, int maxSize) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		logger.debug(System.currentTimeMillis()+"|start|"+maxSendCount);

    		edgeDataService.registerUploadTask(maxSendCount, delay, minSize, maxSize, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/use/data/traceOn")
    @ResponseBody
    public ApiResponse useDataTraceOn(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeDataService.useData(true);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/use/data/traceOff")
    @ResponseBody
    public ApiResponse useDataTraceOff(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeDataService.useData(false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}