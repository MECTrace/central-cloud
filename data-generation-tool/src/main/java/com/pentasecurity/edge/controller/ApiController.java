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
    public ApiResponse createDataTraceOn(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeDataService.registerUploadTask(1, 10, 100, 200, true);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/create/data/traceOff")
    @ResponseBody
    public ApiResponse createDataTraceOff(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeDataService.registerUploadTask(1, 10, 100, 200, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/download/data/traceOn")
    @ResponseBody
    public ApiResponse downloadDataTraceOn(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeDataService.registerDownloadTask(1, 60, true);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/download/data/traceOff")
    @ResponseBody
    public ApiResponse downloadDataTraceOff(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeDataService.registerDownloadTask(1, 60, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}