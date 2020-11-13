package com.pentasecurity.edge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pentasecurity.edge.model.request.DataFromDeviceToEdgeApiRequest;
import com.pentasecurity.edge.model.request.DataFromEdgeToEdgeApiRequest;
import com.pentasecurity.edge.model.response.ApiResponse;
import com.pentasecurity.edge.task.EdgeTaskExecutor;

@Controller
@RequestMapping("/api/edge")
public class ApiController {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    EdgeTaskExecutor edgeTaskExecutor;

    @PostMapping("/upload")
    @ResponseBody
    public ApiResponse upload(@RequestBody DataFromDeviceToEdgeApiRequest apiRequest) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeTaskExecutor.upload(apiRequest);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/copy")
    @ResponseBody
    public ApiResponse copy(@RequestBody DataFromEdgeToEdgeApiRequest apiRequest) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		edgeTaskExecutor.copy(apiRequest);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}