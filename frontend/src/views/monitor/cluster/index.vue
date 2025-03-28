<template>
  <div class="cluster-monitor-container">
    <!-- 节点列表区域 -->
    <div class="node-list-section">
      <h2>集群节点列表</h2>
      <el-table :data="nodeList" style="width: 100%">
        <el-table-column prop="name" label="节点名称" width="180" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="cpu" label="CPU使用率" />
        <el-table-column prop="memory" label="内存使用率" />
      </el-table>
    </div>

    <!-- 资源监控图表区域 -->
    <div class="resource-chart-section">
      <h2>资源监控</h2>
      <div class="chart-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="chart-wrapper">
              <cpu-chart :data="cpuData" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-wrapper">
              <memory-chart :data="memoryData" />
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 拓扑图区域 -->
    <div class="topology-section">
      <h2>集群拓扑图</h2>
      <topology-view :nodes="topologyNodes" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'ClusterMonitor',
  components: {
    CpuChart: () => import('./components/CpuChart'),
    MemoryChart: () => import('./components/MemoryChart'),
    TopologyView: () => import('./components/TopologyView')
  },
  data() {
    return {
      nodeList: [],
      cpuData: [],
      memoryData: [],
      topologyNodes: []
    }
  },
  created() {
    this.fetchClusterData()
  },
  methods: {
    async fetchClusterData() {
      try {
        // 获取节点列表数据
        const nodeRes = await this.$api.get('/cluster/nodes')
        this.nodeList = nodeRes.data

        // 获取CPU和内存数据
        const metricsRes = await this.$api.get('/cluster/metrics')
        this.cpuData = metricsRes.data.cpu
        this.memoryData = metricsRes.data.memory

        // 获取拓扑数据
        const topologyRes = await this.$api.get('/cluster/topology')
        this.topologyNodes = topologyRes.data
      } catch (error) {
        this.$message.error('获取集群数据失败: ' + error.message)
      }
    }
  }
}
</script>

<style scoped>
.cluster-monitor-container {
  padding: 20px;
}

.node-list-section,
.resource-chart-section,
.topology-section {
  margin-bottom: 30px;
}

.chart-container {
  margin-top: 20px;
}

.chart-wrapper {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>