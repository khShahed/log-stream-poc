package com.practice.log_stream_poc.service;

import com.practice.log_stream_poc.model.entity.ApiLog;
import com.practice.log_stream_poc.repository.ApiLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiLogService {
    private final ApiLogRepository apiLogRepository;

    public void saveLog(ApiLog apiLog) {
        log.info("Saving log: {}", apiLog);
        apiLogRepository.save(apiLog);
    }

    public void saveLogs(List<ApiLog> apiLogs) {
        log.info("Saving {} logs", apiLogs.size());
        apiLogRepository.saveAll(apiLogs);
    }

    public long count() {
        log.info("Counting logs");
        return apiLogRepository.count();
    }
}
