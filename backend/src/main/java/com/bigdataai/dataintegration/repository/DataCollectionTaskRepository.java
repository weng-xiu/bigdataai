package com.bigdataai.dataintegration.repository;

import com.bigdataai.dataintegration.model.DataCollectionTask;
import com.bigdataai.dataintegration.model.DataSource;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataCollectionTaskRepository extends JpaRepository<DataCollectionTask, Long> {
    List<DataCollectionTask> findByDataSource(DataSource dataSource);
}