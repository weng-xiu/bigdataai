<template>
  <div class="data-analysis-container">
    <!-- 分析条件区域 -->
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>分析条件</span>
          <div>
            <el-button type="primary" @click="handleAnalyze">分析</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </div>
      </template>
      <el-form :model="filterForm" label-width="100px" size="default">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="数据集">
              <el-select v-model="filterForm.datasetId" placeholder="请选择数据集" style="width: 100%">
                <el-option
                  v-for="item in datasets"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="分析维度">
              <el-select v-model="filterForm.dimension" placeholder="请选择分析维度" style="width: 100%">
                <el-option label="时间" value="time" />
                <el-option label="类别" value="category" />
                <el-option label="区域" value="region" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="分析指标">
              <el-select v-model="filterForm.metrics" multiple placeholder="请选择分析指标" style="width: 100%">
                <el-option label="销售额" value="sales" />
                <el-option label="数量" value="quantity" />
                <el-option label="利润" value="profit" />
                <el-option label="增长率" value="growth" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="filterForm.timeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="图表类型">
              <el-radio-group v-model="filterForm.chartType">
                <el-radio label="bar">柱状图</el-radio>
                <el-radio label="line">折线图</el-radio>
                <el-radio label="pie">饼图</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="高级选项">
              <el-switch
                v-model="showAdvancedOptions"
                active-text="显示"
                inactive-text="隐藏"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 高级选项 -->
        <el-collapse-transition>
          <div v-show="showAdvancedOptions">
            <el-divider content-position="left">高级选项</el-divider>
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="聚合方式">
                  <el-select v-model="filterForm.aggregation" placeholder="请选择聚合方式" style="width: 100%">
                    <el-option label="求和" value="sum" />
                    <el-option label="平均值" value="avg" />
                    <el-option label="最大值" value="max" />
                    <el-option label="最小值" value="min" />
                    <el-option label="计数" value="count" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="排序方式">
                  <el-select v-model="filterForm.sortBy" placeholder="请选择排序方式" style="width: 100%">
                    <el-option label="默认" value="default" />
                    <el-option label="升序" value="asc" />
                    <el-option label="降序" value="desc" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="数据限制">
                  <el-input-number v-model="filterForm.limit" :min="1" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-collapse-transition>
      </el-form>
    </el-card>

    <!-- 分析结果区域 -->
    <div class="analysis-result" v-loading="loading">
      <!-- 图表展示 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>{{ chartTitle }}</span>
                <div class="chart-actions">
                  <el-tooltip content="下载图表" placement="top">
                    <el-button circle @click="downloadChart">
                      <el-icon><Download /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="全屏查看" placement="top">
                    <el-button circle @click="viewFullScreen">
                      <el-icon><FullScreen /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </template>
            <div class="chart-container" ref="mainChart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>数据摘要</span>
              </div>
            </template>
            <div class="summary-container">
              <div v-for="(item, index) in dataSummary" :key="index" class="summary-item">
                <div class="summary-label">{{ item.label }}</div>
                <div class="summary-value" :style="{color: item.color}">{{ item.value }}</div>
                <div class="summary-trend">
                  <el-icon v-if="item.trend === 'up'" class="trend-up"><ArrowUp /></el-icon>
                  <el-icon v-else-if="item.trend === 'down'" class="trend-down"><ArrowDown /></el-icon>
                  <span>{{ item.trendValue }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-card class="data-table-card">
        <template #header>
          <div class="card-header">
            <span>数据明细</span>
            <el-button type="primary" size="small" @click="exportData">
              <el-icon><Download /></el-icon>导出数据
            </el-button>
          </div>
        </template>
        <el-table :data="tableData" style="width: 100%" border stripe height="400">
          <el-table-column v-for="column in tableColumns" :key="column.prop" :prop="column.prop" :label="column.label" :width="column.width" :sortable="column.sortable" />
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

    <!-- 全屏图表对话框 -->
    <el-dialog v-model="fullScreenVisible" title="图表详情" width="90%" fullscreen>
      <div class="fullscreen-chart-container" ref="fullScreenChart"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 图表引用
const mainChart = ref(null)
const fullScreenChart = ref(null)

// 加载状态
const loading = ref(false)

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 全屏图表对话框
const fullScreenVisible = ref(false)

// 高级选项显示状态
const showAdvancedOptions = ref(false)

// 数据集列表
const datasets = ref([
  { id: 1, name: '销售数据集' },
  { id: 2, name: '用户行为数据集' },
  { id: 3, name: '系统性能数据集' },
  { id: 4, name: '网络流量数据集' }
])

// 筛选表单
const filterForm = reactive({
  datasetId: 1,
  dimension: 'time',
  metrics: ['sales'],
  timeRange: [new Date(2023, 0, 1), new Date(2023, 11, 31)],
  chartType: 'bar',
  aggregation: 'sum',
  sortBy: 'default',
  limit: 10
})

// 图表标题
const chartTitle = computed(() => {
  const dataset = datasets.value.find(d => d.id === filterForm.datasetId)
  const datasetName = dataset ? dataset.name : '数据集'
  const dimensionText = {
    time: '时间',
    category: '类别',
    region: '区域'
  }[filterForm.dimension] || '维度'
  
  const metricsText = filterForm.metrics.map(m => {
    return {
      sales: '销售额',
      quantity: '数量',
      profit: '利润',
      growth: '增长率'
    }[m] || m
  }).join('、')
  
  return `${datasetName}按${dimensionText}的${metricsText}分析`
})

// 数据摘要
const dataSummary = ref([
  {
    label: '总销售额',
    value: '¥1,234,567',
    trend: 'up',
    trendValue: '12.5%',
    color: '#409EFF'
  },
  {
    label: '总数量',
    value: '8,765',
    trend: 'up',
    trendValue: '8.3%',
    color: '#67C23A'
  },
  {
    label: '总利润',
    value: '¥456,789',
    trend: 'up',
    trendValue: '15.2%',
    color: '#E6A23C'
  },
  {
    label: '平均增长率',
    value: '10.7%',
    trend: 'down',
    trendValue: '2.1%',
    color: '#F56C6C'
  }
])

// 表格列定义
const tableColumns = ref([
  { prop: 'date', label: '日期', width: '180', sortable: true },
  { prop: 'category', label: '类别', width: '120', sortable: true },
  { prop: 'region', label: '区域', width: '120', sortable: true },
  { prop: 'sales', label: '销售额', width: '150', sortable: true },
  { prop: 'quantity', label: '数量', width: '120', sortable: true },
  { prop: 'profit', label: '利润', width: '150', sortable: true },
  { prop: 'growth', label: '增长率', width: '120', sortable: true }
])

// 表格数据
const tableData = ref([])

// 生成模拟数据
const generateMockData = () => {
  const result = []
  const categories = ['电子产品', '服装', '食品', '家居', '其他']
  const regions = ['华东', '华南', '华北', '华中', '西南', '西北', '东北']
  
  // 生成日期范围内的数据
  const startDate = filterForm.timeRange[0] || new Date(2023, 0, 1)
  const endDate = filterForm.timeRange[1] || new Date(2023, 11, 31)
  const days = Math.floor((endDate - startDate) / (24 * 60 * 60 * 1000))
  
  // 限制生成的数据量
  const dataCount = Math.min(days, 100)
  
  for (let i = 0; i < dataCount; i++) {
    const date = new Date(startDate.getTime() + Math.floor(Math.random() * days)