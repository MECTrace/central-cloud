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
import com.pentasecurity.edge.model.DataTrace;
import com.pentasecurity.edge.model.response.ApiResponse;
import com.pentasecurity.edge.model.response.DataUseApiResponse;
import com.pentasecurity.edge.service.EdgeNodeService;
import com.pentasecurity.edge.util.EdgeLogUtil;

@Controller
@RequestMapping("/api/edge")
public class ApiController {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Value("${edge.edge-id}")
	public String edgeId;

    @Autowired
    EdgeNodeService edgeNodeService;

    @PostMapping("/upload/traceOn")
    @ResponseBody
    public ApiResponse uploadTraceOn(@RequestBody DataInfo dataInfo) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	EdgeLogUtil.log(edgeId, "in", dataInfo.getDeviceId(), edgeId, "/api/edge/upload/traceOn", dataInfo.toJson(), true);

    	try {
    		edgeNodeService.registerTaskUpload(dataInfo, true);

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

    	EdgeLogUtil.log(edgeId, "in", dataInfo.getDeviceId(), edgeId, "/api/edge/upload/traceOff", dataInfo.toJson(), false);

    	try {
    		edgeNodeService.registerTaskUpload(dataInfo, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/copy/traceOn")
    @ResponseBody
    public ApiResponse copyTraceOn(@RequestBody DataTrace dataTrace) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	EdgeLogUtil.log(edgeId, "in", dataTrace.getFromId(), edgeId, "/api/edge/copy/traceOn", dataTrace.toJson(), true);

    	try {
    		edgeNodeService.registerTaskCopy(dataTrace, true);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/copy/traceOff")
    @ResponseBody
    public ApiResponse copyTraceOff(@RequestBody DataInfo dataInfo) {
    	ApiResponse apiResponse = new ApiResponse(-99, "error");

    	EdgeLogUtil.log(edgeId, "in", "", edgeId, "/api/edge/copy/traceOff", dataInfo.toJson(), false);

    	try {
    		edgeNodeService.registerTaskCopy(dataInfo, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }


    @PostMapping("/use/traceOn")
    @ResponseBody
    public DataUseApiResponse useTraceOn(@RequestBody DataInfo req) {
    	DataUseApiResponse apiResponse = new DataUseApiResponse(-99, "error");

    	EdgeLogUtil.log(edgeId, "in", req.getDeviceId(), edgeId, "/api/edge/use/traceOn", req.toJson(), true);

    	try {
    		DataInfo dataInfo = edgeNodeService.registerTaskUse(req, true);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
        	apiResponse.setDataInfo(dataInfo);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }

    @PostMapping("/use/traceOff")
    @ResponseBody
    public DataUseApiResponse useTraceOff(@RequestBody DataInfo req) {
    	DataUseApiResponse apiResponse = new DataUseApiResponse(-99, "error");

    	EdgeLogUtil.log(edgeId, "in", req.getDeviceId(), edgeId, "/api/edge/use/traceOff", req.toJson(), false);

    	try {
    		DataInfo dataInfo = edgeNodeService.registerTaskUse(req, false);

        	apiResponse.setCode(0);
        	apiResponse.setMessage("OK");
        	apiResponse.setDataInfo(dataInfo);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        return apiResponse;
    }
}