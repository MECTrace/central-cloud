package com.pentasecurity.edge.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pentasecurity.edge.service.EdgeDataService;

@Profile("AutoDataCreator1")
@Component
public class CreateDataJob1 {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    EdgeDataService edgeDataService;

    /**
     * 시작과 동시에 실행
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 99999999)
    public void job()
    {
    	edgeDataService.registerUploadTask(100, 3, 50, 100, true);
    }
}
