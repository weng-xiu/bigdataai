# 大数据系统

## 项目介绍

这是一个具备高可扩展性与高性能的大数据系统，整体架构分为数据接入层、数据存储层、数据处理层、服务层以及前端展示层。系统旨在解决海量数据的采集、存储、处理和可视化分析等问题，为企业提供全方位的大数据解决方案。

### 项目价值

- **高性能**：采用分布式架构，支持水平扩展，能够处理PB级数据
- **高可用**：关键组件均支持集群部署，消除单点故障
- **实时性**：支持数据实时采集、处理和分析，满足业务实时决策需求
- **易扩展**：模块化设计，便于功能扩展和定制化开发
- **安全可靠**：提供完善的数据安全保障机制

## 系统架构

![系统架构图](系统架构图URL)

系统采用分层架构设计，各层职责明确，相互协作：

### 数据接入层

- **功能**：负责从多样化的数据源收集数据，支持全量数据导入和增量数据实时同步
- **组件**：Kafka、Flume、Logstash、自定义数据采集器
- **特点**：支持批量导入、实时采集、数据清洗和转换

### 数据存储层

- **功能**：采用分布式文件系统HDFS、HBase和Elasticsearch存储不同类型的数据
- **组件**：
  - **HDFS**：存储原始数据和大文件
  - **HBase**：存储结构化和半结构化数据
  - **Elasticsearch**：存储需要全文检索的数据
  - **MySQL**：存储元数据和业务数据
  - **MongoDB**：存储文档型数据

### 数据处理层

- **功能**：基于Apache Spark构建数据处理引擎，支持批处理和流处理
- **组件**：
  - **Spark Core**：分布式数据处理
  - **Spark SQL**：结构化数据处理
  - **Spark Streaming**：实时数据处理
  - **Spark MLlib**：机器学习算法库

### 服务层

- **功能**：基于Spring Boot和Spring Cloud构建微服务架构
- **组件**：
  - **服务注册与发现**：Eureka/Nacos
  - **服务网关**：Spring Cloud Gateway
  - **负载均衡**：Ribbon
  - **服务熔断**：Hystrix/Sentinel
  - **配置中心**：Spring Cloud Config/Nacos

### 前端展示层

- **功能**：采用Vue.js构建用户界面，实时展示处理后的数据
- **组件**：
  - **Vue 3**：前端框架
  - **ElementPlus**：UI组件库
  - **Echarts**：数据可视化
  - **Axios**：HTTP客户端

## 技术栈

### 前端
- **框架**：Vue 3 + Vue Router + Vuex
- **UI库**：ElementPlus组件库
- **可视化**：Echarts数据可视化
- **HTTP客户端**：Axios
- **构建工具**：Vite
- **CSS预处理器**：Sass
- **状态管理**：Vuex 4
- **响应式设计**：适配多种设备屏幕

### 后端
- **基础框架**：Java 8 + Spring Boot 2.7 + Spring Cloud 2021
- **API设计**：RESTful API
- **ORM框架**：MyBatis + MyBatis-Plus
- **数据库**：
  - **关系型**：MySQL 8.0
  - **NoSQL**：MongoDB 4.6
  - **列式存储**：HBase 2.4
  - **搜索引擎**：Elasticsearch 7.17
- **消息队列**：Kafka 3.1
- **大数据处理**：
  - **分布式计算**：Spark 3.2
  - **分布式存储**：Hadoop 3.3

## 功能模块

### 用户管理
- 用户注册、登录、权限管理
- 基于RBAC的权限控制
- 支持OAuth2.0第三方登录
- JWT令牌认证

### 数据管理
- 数据上传、下载、删除和修改
- 数据源管理和配置
- 数据质量监控
- 数据血缘追踪

### 数据分析
- 数据筛选、排序、分组和聚合
- 自定义分析模型
- 支持SQL查询和可视化查询构建器
- 分析结果导出（Excel、PDF等）

### 实时监控
- 系统运行状态和数据变化监控
- 自定义监控指标和告警规则
- 监控大屏展示
- 异常事件推送

### 数据安全
- 数据传输加密、敏感数据加密存储
- 数据访问控制和审计
- 数据脱敏和水印
- 数据备份和恢复

## 安装部署

### 环境要求

- JDK 1.8+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+
- Docker & Docker Compose (可选)

### 后端部署

```bash
# 克隆项目
git clone https://gitee.com/wengxiulin/bigdataai.git

# 进入后端目录
cd bigdataai/backend

# 编译打包
mvn clean package -DskipTests

# 运行应用
java -jar target/bigdata-ai-backend-1.0.0.jar
```

### 前端部署

```bash
# 进入前端目录
cd bigdataai/frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产环境构建
npm run build
```

### Docker部署（可选）

```bash
# 在项目根目录执行
docker-compose up -d
```

## 项目截图

### 登录界面
![登录界面](登录界面URL)

### 数据分析
![数据分析](数据分析URL)

### 监控大屏
![监控大屏](监控大屏URL)

## 贡献指南

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

- 项目维护者：您的名字
- 邮箱：your.email@example.com
- 项目链接：[https://github.com/yourusername/bigdataai](https://github.com/yourusername/bigdataai)