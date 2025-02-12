package com.practice.log_stream_poc.repository;

import com.practice.log_stream_poc.model.entity.ApiLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends MongoRepository<ApiLog, String> {
}
