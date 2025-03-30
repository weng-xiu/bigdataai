package com.bigdataai.dataintegration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdataai.dataintegration.model.DataSource;
import com.bigdataai.dataintegration.model.DataSourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据源Mapper接口
 */
@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {
    
    /**
     * 根据名称查找数据源
     * 
     * @param name 数据源名称
     * @return 数据源对象
     */
    @Select("SELECT * FROM data_source WHERE name = #{name}")
    DataSource findByName(@Param("name") String name);
    
    /**
     * 检查指定名称的数据源是否存在
     * 
     * @param name 数据源名称
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) FROM data_source WHERE name = #{name}")
    boolean existsByName(@Param("name") String name);
    
    /**
     * 根据类型查找数据源列表
     * 
     * @param type 数据源类型
     * @return 数据源列表
     */
    @Select("SELECT * FROM data_source WHERE type = #{type}")
    List<DataSource> findByType(@Param("type") DataSourceType type);
}