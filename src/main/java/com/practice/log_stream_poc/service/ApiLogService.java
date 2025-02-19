package com.practice.log_stream_poc.service;

import com.mongodb.client.MongoClient;
import com.practice.log_stream_poc.model.entity.ApiLog;
import com.practice.log_stream_poc.repository.ApiLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiLogService {
    private final ApiLogRepository apiLogRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoClient mongo;

    public void saveLog(ApiLog apiLog) {
        log.info("Saving log: {}", apiLog);
        apiLogRepository.save(apiLog);
    }

    public void saveLogs(List<ApiLog> apiLogs) {
        log.info("Saving {} logs", apiLogs.size());

        var bulkInsertion = mongoTemplate.bulkOps(BulkMode.UNORDERED, ApiLog.class);
        var result = bulkInsertion.insert(apiLogs).execute();

        log.info("Inserted {} logs", result.getInsertedCount());
    }

    public long count() {
        log.info("Counting logs");
        return apiLogRepository.count();
    }
}
