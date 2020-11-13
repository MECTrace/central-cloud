package com.pentasecurity.edge.repository;

import org.springframework.data.repository.CrudRepository;

import com.pentasecurity.edge.model.entity.ApiLog;

public interface ApiLogRepository extends CrudRepository<ApiLog, Integer> {
    
}
