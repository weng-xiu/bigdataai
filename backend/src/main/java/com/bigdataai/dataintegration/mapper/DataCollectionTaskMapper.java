package com.bigdataai.dataintegration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdataai.dataintegration.model.DataCollectionTask;
import com.bigdataai.dataintegration.model.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据采集任务Mapper接口
 */
@Mapper
public interface DataCollectionTaskMapper extends BaseMapper<DataCollectionTask> {
    
    /**
     * 根据数据源查找数据采集任务列表
     * 
     * @param dataSource 数据源对象
     * @return 数据采集任务列表
     */
    @Select("SELECT * FROM data_collection_task WHERE data_source_id = #{dataSourceId}")
    List<DataCollectionTask> findByDataSource(@Param("dataSourceId") Long dataSourceId);
}