package com.swatik.docsassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


// Enable async so document ingestion runs in a background thread.
@Configuration
@EnableAsync
public class AsyncConfig {

    // A small thread pool dedicated to ingestion
    @Bean(name="ingestionExecutor")
    public TaskExecutor ingestionExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ingest-");
        executor.initialize();

        return executor;
    }
}
