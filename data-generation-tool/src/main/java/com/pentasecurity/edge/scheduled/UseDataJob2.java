package com.pentasecurity.edge.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pentasecurity.edge.service.EdgeDataService;

@Profile("UseDataJob2")
@Component
public class UseDataJob2 {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    EdgeDataService edgeDataService;

    /**
     * 시작후 6분 10초 뒤에 실행
     */
    @Scheduled(initialDelay = 370000, fixedDelay = 99999999)
    public void job()
    {
    	edgeDataService.registerDownloadTask(5, 20, true);
    }
}
