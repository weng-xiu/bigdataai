package com.bigdataai.dataintegration.repository;

import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.model.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源仓库接口
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    
    /**
     * 根据名称查找数据源
     * 
     * @param name 数据源名称
     * @return 数据源对象
     */
    DataSource findByName(String name);
    
    /**
     * 检查指定名称的数据源是否存在
     * 
     * @param name 数据源名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByName(String name);
    
    /**
     * 根据类型查找数据源列表
     * 
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSource> findByType(DataSourceType type);
}