package com.bigdataai.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 * 提供SqlSessionFactory和SqlSessionTemplate
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.bigdataai.**.mapper")
public class MyBatisConfig {

    /**
     * 创建SqlSessionFactory
     * 
     * @param dataSource 数据源
     * @return SqlSessionFactory实例
     * @throws Exception 如果创建过程中发生错误
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:/mapper/**/*.xml"));
        factoryBean.setTypeAliasesPackage("com.bigdataai.**.model");
        return factoryBean.getObject();
    }

    /**
     * 创建SqlSessionTemplate
     * 
     * @param sqlSessionFactory SqlSessionFactory实例
     * @return SqlSessionTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 创建事务管理器
     * 
     * @param dataSource 数据源
     * @return 事务管理器实例
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}