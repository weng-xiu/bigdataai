<template>
  <div class="dashboard-container">
    <!-- 系统概览卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statCards" :key="index">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <div class="icon-container" :style="{ backgroundColor: item.bgColor }">
              <el-icon :size="24" :color="'#fff'"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-change" :class="item.trend === 'up' ? 'up' : 'down'">
                <el-icon v-if="item.trend === 'up'"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
                {{ item.change }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 数据处理趋势图 -->
      <el-col :xs="24" :md="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>数据处理趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="day">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" ref="processingTrendChart"></div>
        </el-card>
      </el-col>

      <!-- 存储空间分布图 -->
      <el-col :xs="24" :md="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>存储空间分布</span>
            </div>
          </template>
          <div class="chart-container" ref="storageDistributionChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统状态和最近任务 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 系统状态 -->
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
              <el-tag type="success" size="small" effect="dark">运行中</el-tag>
            </div>
          </template>
          <div class="system-status">
            <el-row :gutter="20" v-for="(item, index) in systemStatus" :key="index" class="status-item">
              <el-col :span="8">{{ item.name }}</el-col>
              <el-col :span="16">
                <el-progress 
                  :percentage="item.value" 
                  :color="item.color" 
                  :status="item.status"
                  :stroke-width="10"
                  :format="(percentage) => percentage + '%'"
                />
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>

      <!-- 最近任务 -->
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>最近任务</span>
              <el-button type="primary" size="small" plain>查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTasks" style="width: 100%" :max-height="300">
            <el-table-column prop="name" label="任务名称" width="180" />
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'success' ? 'success' : scope.row.status === 'processing' ? 'warning' : 'danger'">
                  {{ scope.row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { useStore } from 'vuex'
import * as echarts from 'echarts'

const store = useStore()

// 图表引用
const processingTrendChart = ref(null)
const storageDistributionChart = ref(null)

// 时间范围选择
const timeRange = ref('day')

// 统计卡片数据
const statCards = reactive([
  {
    title: '数据总量',
    value: '12.5 TB',
    change: '12.8%',
    trend: 'up',
    icon: 'DataLine',
    bgColor: '#409EFF'
  },
  {
    title: '今日处理',
    value: '1.8 GB',
    change: '5.2%',
    trend: 'up',
    icon: 'Histogram',
    bgColor: '#67C23A'
  },
  {
    title: '活跃用户',
    value: '128',
    change: '3.1%',
    trend: 'down',
    icon: 'User',
    bgColor: '#E6A23C'
  },
  {
    title: '系统负载',
    value: '42%',
    change: '2.5%',
    trend: 'down',
    icon: 'CPU',
    bgColor: '#F56C6C'
  }
])