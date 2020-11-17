package com.pentasecurity.edge.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pentasecurity.edge.service.CreateDataService;

@Profile("AutoDataCreator2")
@Component
public class CreateDataJob2 {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Autowired
    CreateDataService createDataService;

    /**
     * 1시간마다 실행
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron="0 30 * * * *")
    public void job()
    {
    	createDataService.createData();
    }

    /**
     * 1시간마다 실행
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron="0 5 * * * *")
    public void job2()
    {
    	createDataService.downloadData();
    }
}
