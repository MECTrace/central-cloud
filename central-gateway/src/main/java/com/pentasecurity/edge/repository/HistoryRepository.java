package com.pentasecurity.edge.repository;

import org.springframework.data.repository.CrudRepository;

import com.pentasecurity.edge.model.entity.History;

public interface HistoryRepository extends CrudRepository<History, Integer> {

}
