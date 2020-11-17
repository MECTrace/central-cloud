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
import com.pentasecurity.edge.service.CreateDataService;

@Controller
@RequestMapping("/api/device")
public class ApiController {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    CreateDataService createDataService;

    @GetMapping("/create/data")
    @ResponseBody
    public ApiResponse createData(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		createDataService.createData();

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");


    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @GetMapping("/download/data")
    @ResponseBody
    public ApiResponse downloadData(HttpServletRequest request) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	try {
    		createDataService.downloadData();

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");


    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}