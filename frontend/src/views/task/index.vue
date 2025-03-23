<template>
  <div class="task-container">
    <!-- 任务概览卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in taskStats" :key="index">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <div class="icon-container" :style="{ backgroundColor: item.bgColor }">
              <el-icon :size="24" :color="'#fff'"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-desc">{{ item.description }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleCreateTask">
        <el-icon><Plus /></el-icon>创建任务
      </el-button>
      <el-button type="success" @click="handleBatchStart" :disabled="!hasSelected">
        <el-icon><VideoPlay /></el-icon>批量启动
      </el-button>
      <el-button type="warning" @click="handleBatchStop" :disabled="!hasSelected">
        <el-icon><VideoPause /></el-icon>批量停止
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="!hasSelected">
        <el-icon><Delete /></el-icon>批量删除
      </el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索任务名称"
        class="search-input"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #suffix>
          <el-icon class="el-input__icon" @click="handleSearch"><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 任务列表 -->
    <el-card class="task-list-card">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-radio-group v-model="taskFilter" size="small" @change="handleFilterChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="running">运行中</el-radio-button>
            <el-radio-button label="waiting">等待中</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="failed">失败</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="任务类型" width="120">
          <template #default="scope">
            <el-tag :type="getTaskTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSource" label="数据源" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="progress" label="进度" width="180">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.progress" 
              :status="getProgressStatus(scope.row.status)"
              :stroke-width="15"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'waiting' || scope.row.status === 'paused'"
              size="small" 
              type="success" 
              @click="handleStart(scope.row)"
            >
              <el-icon><VideoPlay /></el-icon>启动
            </el-button>
            <el-button 
              v-if="scope.row.status === 'running'"
              size="small" 
              type="warning" 
              @click="handlePause(scope.row)"
            >
              <el-icon><VideoPause /></el-icon>暂停
            </el-button>
            <el-button size="small" type="primary" @click="handleDetail(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-dropdown>
              <el-button size="small" type="primary">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogs(scope.row)">
                    <el-icon><Document /></el-icon>查看日志
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleEdit(scope.row)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleClone(scope.row)">
                    <el-icon><CopyDocument /></el-icon>克隆
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(scope.row)">
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 创建任务对话框 -->
    <el-dialog v-model="createTaskDialogVisible" title="创建任务" width="650px">
      <el-form :model="taskForm" label-width="120px" :rules="taskFormRules" ref="taskFormRef">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="taskForm.type" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="数据导入" value="import" />
            <el-option label="数据清洗" value="clean" />
            <el-option label="数据转换" value="transform" />
            <el-option label="数据分析" value="analysis" />
            <el-option label="数据导出" value="export" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源" prop="dataSource">
          <el-select v-model="taskForm.dataSource" placeholder="请选择数据源" style="width: 100%">
            <el-option
              v-for="item in dataSources"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="执行计划" prop="schedule">
          <el-radio-group v-model="taskForm.scheduleType">
            <el-radio label="once">单次执行</el-radio>
            <el-radio label="periodic">周期执行</el-radio>
          </el-radio-group>
          <div v-if="taskForm.scheduleType === 'periodic'" class="schedule-config">
            <el-select v-model="taskForm.scheduleInterval" placeholder="执行周期" style="width: 150px; margin-right: 10px">
              <el-option label="每小时" value="hourly" />
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
            <el-time-picker
              v-if="taskForm.scheduleInterval !== 'hourly'"
              v-model="taskForm.scheduleTime"
              placeholder="执行时间"
              format="HH:mm"
              style="width: 150px"
            />
          </div>
        </el-form-item>
        <el-form-item label="任务参数">
          <el-tabs v-model="taskParamTab">
            <el-tab-pane label="基本参数" name="basic">
              <el-form-item label="并行度" prop="parallelism">
                <el-input-number v-model="taskForm.params.parallelism" :min="1" :max="32" style="width: 150px" />
              </el-form-item>
              <el-form-item label="超时时间(分钟)" prop="timeout">
                <el-input-number v-model="taskForm.params.timeout" :min="1" :max="1440" style="width: 150px" />
              </el-form-item>
              <el-form-item label="失败重试次数" prop="retries">
                <el-input-number v-model="taskForm.params.retries" :min="0" :max="10" style="width: 150px" />
              </el-form-item>
            </el-tab-pane>
            <el-tab-pane label="高级参数" name="advanced">
              <el-form-item label="内存配置(GB)" prop="memory">
                <el-input-number v-model="taskForm.params.memory" :min="1" :max="64" style="width: 150px" />
              </el-form-item>
              <el-form-item label="CPU配置(核)" prop="cpu">
                <el-input-number v-model="taskForm.params.cpu" :min="1" :max="16" style="width: 150px" />
              </el-form-item>
              <el-form-item label="优先级" prop="priority">
                <el-select v-model="taskForm.params.priority" placeholder="请选择优先级" style="width: 150px">
                  <el-option label="低" value="low" />
                  <el-option label="中" value="medium" />
                  <el-option label="高" value="high" />
                </el-select>
              </el-form-item>
            </el-tab-pane>
            <el-tab-pane :label="getTaskTypeSpecificLabel()" name="specific">
              <component :is="getTaskTypeSpecificForm()" v-model="taskForm.params.specific" />
            </el-tab-pane>
          </el-tabs>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="taskForm.description" type="textarea" rows="3" placeholder="请输入任务描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createTaskDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTaskForm" :loading="submitting">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="taskDetailVisible" title="任务详情" width="80%">
      <div v-if="currentTask" class="task-detail-container">
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskTypeTag(currentTask.type)">{{ currentTask.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentTask.status)">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数据源">{{ currentTask.dataSource }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="3">{{ currentTask.description }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">执行进度</el-divider>
        <el-progress 
          :percentage="currentTask.progress" 
          :status="getProgressStatus(currentTask.status)"
          :stroke-width="20"
          :text-inside="true"
        />

        <el-divider content-position="left">任务参数</el-divider>
        <el-descriptions :column="3" border>