package com.pentasecurity.edge;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskExecutorConfig {

  @Bean(name = "threadPoolTaskExecutor")
  public Executor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(6);
    taskExecutor.setMaxPoolSize(100);
    taskExecutor.setQueueCapacity(100);
    taskExecutor.setThreadNamePrefix("Task-");
    taskExecutor.initialize();
    return taskExecutor;
  }
}
