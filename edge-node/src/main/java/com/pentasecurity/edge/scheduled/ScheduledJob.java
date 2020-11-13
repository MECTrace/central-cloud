package com.pentasecurity.edge.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pentasecurity.edge.service.EdgeNodeService;

@Component
public class ScheduledJob {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    EdgeNodeService createDataService;

    /**
     * 10초마다 실행
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron="*/10 * * * * *")
    public void job()
    {
    	createDataService.processTask();
    }
}
