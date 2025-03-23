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
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Download, FullScreen, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

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
    const date = new Date(startDate.getTime() + Math.floor(Math.random() * days) * 24 * 60 * 60 * 1000)
    const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    const category = categories[Math.floor(Math.random() * categories.length)]
    const region = regions[Math.floor(Math.random() * regions.length)]
    const sales = Math.floor(Math.random() * 10000) + 1000
    const quantity = Math.floor(Math.random() * 100) + 10
    const profit = Math.floor(sales * (Math.random() * 0.3 + 0.1))
    const growth = (Math.random() * 0.3 - 0.1) * 100
    
    result.push({
      date: formattedDate,
      category,
      region,
      sales,
      quantity,
      profit,
      growth: growth.toFixed(2) + '%'
    })
  }
  
  // 根据筛选条件过滤数据
  let filteredData = [...result]
  
  // 按维度筛选
  if (filterForm.dimension === 'category') {
    filteredData = filteredData.sort((a, b) => a.category.localeCompare(b.category))
  } else if (filterForm.dimension === 'region') {
    filteredData = filteredData.sort((a, b) => a.region.localeCompare(b.region))
  } else {
    // 默认按时间排序
    filteredData = filteredData.sort((a, b) => a.date.localeCompare(b.date))
  }
  
  // 应用排序
  if (filterForm.sortBy === 'asc') {
    // 升序排序，已经应用
  } else if (filterForm.sortBy === 'desc') {
    filteredData = filteredData.reverse()
  }
  
  // 应用数据限制
  filteredData = filteredData.slice(0, filterForm.limit)
  
  return filteredData
}

// 更新数据摘要
const updateDataSummary = (data) => {
  // 计算总销售额
  const totalSales = data.reduce((sum, item) => sum + item.sales, 0)
  // 计算总数量
  const totalQuantity = data.reduce((sum, item) => sum + item.quantity, 0)
  // 计算总利润
  const totalProfit = data.reduce((sum, item) => sum + item.profit, 0)
  // 计算平均增长率
  const avgGrowth = data.reduce((sum, item) => sum + parseFloat(item.growth), 0) / data.length
  
  // 更新数据摘要
  dataSummary.value = [
    {
      label: '总销售额',
      value: `¥${totalSales.toLocaleString()}`,
      trend: 'up',
      trendValue: '12.5%',
      color: '#409EFF'
    },
    {
      label: '总数量',
      value: totalQuantity.toLocaleString(),
      trend: 'up',
      trendValue: '8.3%',
      color: '#67C23A'
    },
    {
      label: '总利润',
      value: `¥${totalProfit.toLocaleString()}`,
      trend: 'up',
      trendValue: '15.2%',
      color: '#E6A23C'
    },
    {
      label: '平均增长率',
      value: `${avgGrowth.toFixed(2)}%`,
      trend: avgGrowth > 0 ? 'up' : 'down',
      trendValue: `${Math.abs(avgGrowth).toFixed(1)}%`,
      color: avgGrowth > 0 ? '#67C23A' : '#F56C6C'
    }
  ]
}

// 图表实例
let chartInstance = null
let fullScreenChartInstance = null

// 初始化图表
const initChart = (data) => {
  if (!mainChart.value) return
  
  // 销毁已有图表实例
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  // 创建新图表实例
  chartInstance = echarts.init(mainChart.value)
  
  // 准备图表数据
  const chartData = prepareChartData(data)
  
  // 设置图表选项
  const option = getChartOption(chartData)
  
  // 渲染图表
  chartInstance.setOption(option)
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
}

// 准备图表数据
const prepareChartData = (data) => {
  const result = {
    xAxis: [],
    series: []
  }
  
  // 根据维度分组数据
  if (filterForm.dimension === 'category') {
    // 按类别分组
    const categories = [...new Set(data.map(item => item.category))]
    result.xAxis = categories
    
    // 为每个指标创建系列数据
    filterForm.metrics.forEach(metric => {
      const seriesData = categories.map(category => {
        const items = data.filter(item => item.category === category)
        // 根据聚合方式计算值
        return aggregateValues(items, metric)
      })
      
      result.series.push({
        name: getMetricName(metric),
        data: seriesData
      })
    })
  } else if (filterForm.dimension === 'region') {
    // 按区域分组
    const regions = [...new Set(data.map(item => item.region))]
    result.xAxis = regions
    
    // 为每个指标创建系列数据
    filterForm.metrics.forEach(metric => {
      const seriesData = regions.map(region => {
        const items = data.filter(item => item.region === region)
        // 根据聚合方式计算值
        return aggregateValues(items, metric)
      })
      
      result.series.push({
        name: getMetricName(metric),
        data: seriesData
      })
    })
  } else {
    // 按时间分组
    const dates = [...new Set(data.map(item => item.date))].sort()
    result.xAxis = dates
    
    // 为每个指标创建系列数据
    filterForm.metrics.forEach(metric => {
      const seriesData = dates.map(date => {
        const items = data.filter(item => item.date === date)
        // 根据聚合方式计算值
        return aggregateValues(items, metric)
      })
      
      result.series.push({
        name: getMetricName(metric),
        data: seriesData
      })
    })
  }
  
  return result
}

// 根据聚合方式计算值
const aggregateValues = (items, metric) => {
  if (!items.length) return 0
  
  // 处理增长率特殊情况
  if (metric === 'growth') {
    const values = items.map(item => parseFloat(item.growth))
    
    switch (filterForm.aggregation) {
      case 'avg':
        return values.reduce((sum, val) => sum + val, 0) / values.length
      case 'max':
        return Math.max(...values)
      case 'min':
        return Math.min(...values)
      case 'count':
        return items.length
      case 'sum':
      default:
        return values.reduce((sum, val) => sum + val, 0)
    }
  }
  
  // 处理其他指标
  const values = items.map(item => item[metric])
  
  switch (filterForm.aggregation) {
    case 'avg':
      return values.reduce((sum, val) => sum + val, 0) / values.length
    case 'max':
      return Math.max(...values)
    case 'min':
      return Math.min(...values)
    case 'count':
      return items.length
    case 'sum':
    default:
      return values.reduce((sum, val) => sum + val, 0)
  }
}

// 获取指标名称
const getMetricName = (metric) => {
  const metricNames = {
    sales: '销售额',
    quantity: '数量',
    profit: '利润',
    growth: '增长率'
  }
  
  return metricNames[metric] || metric
}

// 获取图表配置
const getChartOption = (chartData) => {
  // 设置主题色
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9C27B0', '#3F51B5', '#00BCD4']
  const option = {
    color: colors,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: chartData.series.map(item => item.name)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: chartData.xAxis
    },
    yAxis: {
      type: 'value'
    },
    series: []
  }
  
  // 根据图表类型设置系列
  if (filterForm.chartType === 'bar') {
    // 柱状图
    chartData.series.forEach(series => {
      option.series.push({
        name: series.name,
        type: 'bar',
        data: series.data
      })
    })
  } else if (filterForm.chartType === 'line') {
    // 折线图
    chartData.series.forEach(series => {
      option.series.push({
        name: series.name,
        type: 'line',
        data: series.data,
        smooth: true,
        symbolSize: 6,
        lineStyle: {
          width: 2
        }
      })
    })
  } else if (filterForm.chartType === 'pie') {
    // 饼图
    option.xAxis = undefined
    option.yAxis = undefined
    
    // 合并所有系列的数据
    const seriesData = []
    
    if (chartData.series.length > 0) {
      chartData.xAxis.forEach((category, index) => {
        seriesData.push({
          name: category,
          value: chartData.series[0].data[index]
        })
      })
    }
    
    option.series.push({
      type: 'pie',
      radius: '60%',
      center: ['50%', '50%'],
      data: seriesData,
      label: {
        formatter: '{b}: {c} ({d}%)',
        fontSize: 14
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    })
  }
  
  return option
}

// 处理分析按钮点击
const handleAnalyze = () => {
  // 显示加载状态
  loading.value = true
  
  // 模拟API请求延迟
  setTimeout(() => {
    try {
      // 生成模拟数据
      const data = generateMockData()
      
      // 更新表格数据
      tableData.value = data
      total.value = data.length
      
      // 更新数据摘要
      updateDataSummary(data)
      
      // 初始化图表
      nextTick(() => {
        initChart(data)
      })
      
      // 显示成功消息
      ElMessage.success('数据分析完成')
    } catch (error) {
      console.error('分析数据时出错:', error)
      ElMessage.error('分析数据时出错')
    } finally {
      // 隐藏加载状态
      loading.value = false
    }
  }, 800)
}

// 重置筛选条件
const resetFilters = () => {
  // 重置筛选表单
  Object.assign(filterForm, {
    datasetId: 1,
    dimension: 'time',
    metrics: ['sales'],
    timeRange: [new Date(2023, 0, 1), new Date(2023, 11, 31)],
    chartType: 'bar',
    aggregation: 'sum',
    sortBy: 'default',
    limit: 10
  })
  
  // 重置高级选项显示状态
  showAdvancedOptions.value = false
  
  // 清空表格数据
  tableData.value = []
  total.value = 0
  
  // 销毁图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  ElMessage.info('已重置所有筛选条件')
}

// 导出数据
const exportData = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  try {
    // 准备CSV数据
    const headers = tableColumns.value.map(col => col.label).join(',')
    const rows = tableData.value.map(row => {
      return tableColumns.value.map(col => row[col.prop]).join(',')
    }).join('\n')
    const csvContent = `${headers}\n${rows}`
    
    // 创建Blob对象
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    
    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    // 设置下载属性
    link.setAttribute('href', url)
    link.setAttribute('download', `数据分析_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    
    // 添加到文档并触发点击
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出数据时出错:', error)
    ElMessage.error('导出数据时出错')
  }
}

// 下载图表
const downloadChart = () => {
  if (!chartInstance) {
    ElMessage.warning('没有可下载的图表')
    return
  }
  
  try {
    // 获取图表的base64数据URL
    const dataURL = chartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = dataURL
    link.download = `${chartTitle.value}_${new Date().toISOString().split('T')[0]}.png`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('图表下载成功')
  } catch (error) {
    console.error('下载图表时出错:', error)
    ElMessage.error('下载图表时出错')
  }
}

// 全屏查看图表
const viewFullScreen = () => {
  if (!chartInstance) {
    ElMessage.warning('没有可查看的图表')
    return
  }
  
  // 显示全屏对话框
  fullScreenVisible.value = true
  
  // 等待DOM更新后初始化全屏图表
  nextTick(() => {
    // 销毁已有全屏图表实例
    if (fullScreenChartInstance) {
      fullScreenChartInstance.dispose()
    }
    
    // 创建新全屏图表实例
    fullScreenChartInstance = echarts.init(fullScreenChart.value)
    
    // 使用与主图表相同的配置
    fullScreenChartInstance.setOption(chartInstance.getOption())
    
    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', () => {
      fullScreenChartInstance && fullScreenChartInstance.resize()
    })
  })
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  // 这里可以添加重新加载数据的逻辑
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  // 这里可以添加重新加载数据的逻辑
}

// 组件挂载时的处理
onMounted(() => {
  // 初始执行一次分析
  handleAnalyze()
})

// 组件卸载前的清理
onBeforeUnmount(() => {
  // 销毁图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  // 销毁全屏图表实例
  if (fullScreenChartInstance) {
    fullScreenChartInstance.dispose()
    fullScreenChartInstance = null
  }
  
  // 移除窗口大小变化监听器
  window.removeEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
  
  window.removeEventListener('resize', () => {
    fullScreenChartInstance && fullScreenChartInstance.resize()
  })
})
</script>

<style scoped>
.data-analysis-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart-actions {
  display: flex;
  gap: 10px;
}

.summary-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.summary-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.summary-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.trend-up {
  color: #67C23A;
}

.trend-down {
  color: #F56C6C;
}

.data-table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.fullscreen-chart-container {
  height: 80vh;
  width: 100%;
}
</style>