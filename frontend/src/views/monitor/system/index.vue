<template>
  <div class="monitor-system-container">
    <!-- 系统状态概览 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statusCards" :key="index">
        <el-card class="status-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <div class="icon-container" :style="{ backgroundColor: item.bgColor }">
              <el-icon :size="24" :color="'#fff'"><component :is="item.icon" /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">{{ item.title }}</div>
              <div class="status-value">{{ item.value }}</div>
              <div class="status-desc">{{ item.description }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时监控图表 -->
    <el-row :gutter="20" class="chart-row">
      <!-- CPU使用率 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>CPU使用率</span>
              <el-select v-model="cpuTimeRange" size="small" style="width: 120px">
                <el-option label="最近1小时" value="1h" />
                <el-option label="最近6小时" value="6h" />
                <el-option label="最近24小时" value="24h" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" ref="cpuChart"></div>
        </el-card>
      </el-col>

      <!-- 内存使用率 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>内存使用率</span>
              <el-select v-model="memoryTimeRange" size="small" style="width: 120px">
                <el-option label="最近1小时" value="1h" />
                <el-option label="最近6小时" value="6h" />
                <el-option label="最近24小时" value="24h" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" ref="memoryChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 磁盘使用率 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>磁盘使用率</span>
              <el-button-group size="small">
                <el-button type="primary" plain :class="{ active: diskViewType === 'usage' }" @click="diskViewType = 'usage'">使用率</el-button>
                <el-button type="primary" plain :class="{ active: diskViewType === 'io' }" @click="diskViewType = 'io'">IO</el-button>
              </el-button-group>
            </div>
          </template>
          <div class="chart-container" ref="diskChart"></div>
        </el-card>
      </el-col>

      <!-- 网络流量 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>网络流量</span>
              <el-button-group size="small">
                <el-button type="primary" plain :class="{ active: networkViewType === 'traffic' }" @click="networkViewType = 'traffic'">流量</el-button>
                <el-button type="primary" plain :class="{ active: networkViewType === 'packets' }" @click="networkViewType = 'packets'">数据包</el-button>
              </el-button-group>
            </div>
          </template>
          <div class="chart-container" ref="networkChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统日志 -->
    <el-card class="log-card">
      <template #header>
        <div class="card-header">
          <span>系统日志</span>
          <div>
            <el-select v-model="logLevel" size="small" style="width: 120px; margin-right: 10px">
              <el-option label="全部" value="all" />
              <el-option label="错误" value="error" />
              <el-option label="警告" value="warning" />
              <el-option label="信息" value="info" />
            </el-select>
            <el-button type="primary" size="small" @click="refreshLogs">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="logData" style="width: 100%" height="300" border stripe>
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="scope">
            <el-tag 
              :type="scope.row.level === 'error' ? 'danger' : scope.row.level === 'warning' ? 'warning' : 'info'"
              size="small"
            >
              {{ scope.row.level.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="150" />
        <el-table-column prop="message" label="消息" show-overflow-tooltip />
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

// 状态卡片数据
const statusCards = reactive([
  {
    title: '系统状态',
    value: '正常',
    description: '所有服务运行正常',
    icon: 'Check',
    bgColor: '#67C23A'
  },
  {
    title: '集群节点',
    value: '5/5',
    description: '所有节点在线',
    icon: 'Connection',
    bgColor: '#409EFF'
  },
  {
    title: '告警数量',
    value: '2',
    description: '最近24小时内',
    icon: 'Warning',
    bgColor: '#E6A23C'
  },
  {
    title: '平均响应',
    value: '42ms',
    description: '服务响应时间',
    icon: 'Timer',
    bgColor: '#909399'
  }
])

// 图表引用
const cpuChart = ref(null)
const memoryChart = ref(null)
const diskChart = ref(null)
const networkChart = ref(null)

// 图表实例
let cpuChartInstance = null
let memoryChartInstance = null
let diskChartInstance = null
let networkChartInstance = null

// 时间范围选择
const cpuTimeRange = ref('1h')
const memoryTimeRange = ref('1h')

// 视图类型选择
const diskViewType = ref('usage')
const networkViewType = ref('traffic')

// 日志相关
const logLevel = ref('all')
const logData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 刷新间隔（毫秒）
const refreshInterval = 5000
let refreshTimer = null

// 初始化CPU使用率图表
const initCpuChart = () => {
  if (!cpuChart.value) return
  
  cpuChartInstance = echarts.init(cpuChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: generateTimeData(cpuTimeRange.value)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(64, 158, 255, 0.5)'
            },
            {
              offset: 1,
              color: 'rgba(64, 158, 255, 0.1)'
            }
          ])
        },
        data: generateRandomData(cpuTimeRange.value, 10, 80)
      }
    ]
  }
  
  cpuChartInstance.setOption(option)
}

// 初始化内存使用率图表
const initMemoryChart = () => {
  if (!memoryChart.value) return
  
  memoryChartInstance = echarts.init(memoryChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: generateTimeData(memoryTimeRange.value)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(103, 194, 58, 0.5)'
            },
            {
              offset: 1,
              color: 'rgba(103, 194, 58, 0.1)'
            }
          ])
        },
        data: generateRandomData(memoryTimeRange.value, 30, 90)
      }
    ]
  }
  
  memoryChartInstance.setOption(option)
}

// 初始化磁盘使用率图表
const initDiskChart = () => {
  if (!diskChart.value) return
  
  diskChartInstance = echarts.init(diskChart.value)
  
  let option = {}
  
  if (diskViewType.value === 'usage') {
    // 磁盘使用率饼图
    option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} GB ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 10,
        data: ['系统', '数据', '日志', '备份', '可用空间']
      },
      series: [
        {
          name: '磁盘空间',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: 120, name: '系统', itemStyle: { color: '#409EFF' } },
            { value: 350, name: '数据', itemStyle: { color: '#67C23A' } },
            { value: 180, name: '日志', itemStyle: { color: '#E6A23C' } },
            { value: 250, name: '备份', itemStyle: { color: '#F56C6C' } },
            { value: 600, name: '可用空间', itemStyle: { color: '#909399' } }
          ]
        }
      ]
    }
  } else {
    // 磁盘IO折线图