package com.pentasecurity.edge.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pentasecurity.edge.service.EdgeDataService;

@Profile("AutoDataCreator2")
@Component
public class CreateDataJob2 {
    Logger logger = LoggerFactory.getLogger("mainLogger");

    @Value("${edge.data-download-rate}")
    private double dataDownloadRate;
    @Value("${edge.data-delete-rate}")
    private double dataDeleteRate;

    @Autowired
    EdgeDataService edgeDataService;

    /**
     * 1시간마다 실행
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron="0 30 * * * *")
    public void job()
    {
    	edgeDataService.createData();
    }

    /**
     * 1시간마다 실행
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron="0 38 * * * *")
    public void job3()
    {
    	if ( Math.random() < (dataDeleteRate/100.0) ) {
    		edgeDataService.deleteData();
    	}
    }

    /**
     * 1시간마다 실행
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron="0 5 * * * *")
    public void job2()
    {
    	if ( Math.random() < (dataDownloadRate/100.0) ) {
    		edgeDataService.downloadData();
    	}
    }
}
