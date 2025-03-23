<template>
  <div class="monitor-cluster-container">
    <!-- 集群状态概览 -->
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

    <!-- 节点列表 -->
    <el-card class="node-list-card">
      <template #header>
        <div class="card-header">
          <span>集群节点列表</span>
          <div>
            <el-button type="primary" size="small" @click="refreshNodes">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
            <el-button type="success" size="small" @click="addNodeDialogVisible = true">
              <el-icon><Plus /></el-icon>添加节点
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="nodeList" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column type="expand">
          <template #default="props">
            <el-form label-position="left" inline class="node-detail">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="节点ID">
                    <span>{{ props.row.id }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="IP地址">
                    <span>{{ props.row.ip }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="端口">
                    <span>{{ props.row.port }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="CPU核心数">
                    <span>{{ props.row.cpuCores }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="内存大小">
                    <span>{{ props.row.memory }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="磁盘空间">
                    <span>{{ props.row.disk }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="启动时间">
                    <span>{{ props.row.startTime }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="最后心跳">
                    <span>{{ props.row.lastHeartbeat }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="运行任务数">
                    <span>{{ props.row.runningTasks }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-divider content-position="left">节点标签</el-divider>
              <div class="node-tags">
                <el-tag
                  v-for="tag in props.row.tags"
                  :key="tag"
                  class="node-tag"
                  :type="getTagType(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="节点名称" min-width="150" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'master' ? 'danger' : scope.row.role === 'worker' ? 'success' : 'info'">
              {{ scope.row.role === 'master' ? '主节点' : scope.row.role === 'worker' ? '工作节点' : '数据节点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="资源使用" width="250">
          <template #default="scope">
            <div class="resource-usage">
              <div class="resource-item">
                <span class="resource-label">CPU:</span>
                <el-progress :percentage="scope.row.cpuUsage" :color="getResourceColor(scope.row.cpuUsage)" :stroke-width="15" />
              </div>
              <div class="resource-item">
                <span class="resource-label">内存:</span>
                <el-progress :percentage="scope.row.memoryUsage" :color="getResourceColor(scope.row.memoryUsage)" :stroke-width="15" />
              </div>
              <div class="resource-item">
                <span class="resource-label">磁盘:</span>
                <el-progress :percentage="scope.row.diskUsage" :color="getResourceColor(scope.row.diskUsage)" :stroke-width="15" />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleDetail(scope.row)">
              <el-icon><Monitor /></el-icon>监控
            </el-button>
            <el-button size="small" type="success" @click="handleConfig(scope.row)">
              <el-icon><Setting /></el-icon>配置
            </el-button>
            <el-dropdown>
              <el-button size="small" type="primary">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleRestart(scope.row)">
                    <el-icon><RefreshRight /></el-icon>重启
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleMaintenance(scope.row)">
                    <el-icon><Tools /></el-icon>维护模式
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleRemove(scope.row)">
                    <el-icon><Delete /></el-icon>移除节点
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 节点拓扑图 -->
    <el-card class="topology-card">
      <template #header>
        <div class="card-header">
          <span>集群拓扑图</span>
          <el-radio-group v-model="topologyViewType" size="small">
            <el-radio-button label="logical">逻辑视图</el-radio-button>
            <el-radio-button label="physical">物理视图</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="topology-container" ref="topologyChart"></div>
    </el-card>

    <!-- 添加节点对话框 -->
    <el-dialog v-model="addNodeDialogVisible" title="添加节点" width="500px">
      <el-form :model="nodeForm" label-width="100px" :rules="nodeFormRules" ref="nodeFormRef">
        <el-form-item label="节点名称" prop="name">
          <el-input v-model="nodeForm.name" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="nodeForm.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="nodeForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="节点角色" prop="role">
          <el-select v-model="nodeForm.role" placeholder="请选择节点角色" style="width: 100%">
            <el-option label="主节点" value="master" />
            <el-option label="工作节点" value="worker" />
            <el-option label="数据节点" value="data" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点标签">
          <el-tag
            v-for="tag in nodeForm.tags"
            :key="tag"
            closable
            @close="handleTagClose(tag)"
            class="node-tag"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleTagConfirm"
            @blur="handleTagConfirm"
          />
          <el-button v-else class="button-new-tag" size="small" @click="showTagInput">
            + 添加标签
          </el-button>
        </el-form-item>
        <el-form-item label="CPU核心数" prop="cpuCores">
          <el-input-number v-model="nodeForm.cpuCores" :min="1" :max="128" style="width: 100%" />
        </el-form-item>
        <el-form-item label="内存大小(GB)" prop="memoryGB">
          <el-input-number v-model="nodeForm.memoryGB" :min="1" :max="1024" style="width: 100%" />
        </el-form-item>
        <el-form-item label="磁盘空间(GB)" prop="diskGB">
          <el-input-number v-model="nodeForm.diskGB" :min="1" :max="10000" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addNodeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitNodeForm" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 节点详情对话框 -->
    <el-dialog v-model="nodeDetailVisible" title="节点监控" width="80%" fullscreen>
      <div v-if="currentNode" class="node-detail-container">
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="节点名称">{{ currentNode.name }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentNode.ip }}</el-descriptions-item>
          <el-descriptions-item label="端口">{{ currentNode.port }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="currentNode.role === 'master' ? 'danger' : currentNode.role === 'worker' ? 'success' : 'info'">
              {{ currentNode.role === 'master' ? '主节点' : currentNode.role === 'worker' ? '工作节点' : '数据节点' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentNode.status)">
              {{ getStatusText(currentNode.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="启动时间">{{ currentNode.startTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">资源监控</el-divider>
        
        <el-row :gutter="20" class="chart-row">
          <!-- CPU使用率 -->
          <el-col :xs="24" :lg="8">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>CPU使用率</span>
                  <el-select v-model="monitorTimeRange" size="small" style="width: 120px">
                    <el-option label="最近1小时" value="1h" />
                    <el-option label="最近6小时" value="6h" />
                    <el-option label="最近24小时" value="24h" />
                  </el-select>
                </div>
              </template>
              <div class="chart-container" ref="nodeCpuChart"></div>
            </el-card>
          </el-col>

          <!-- 内存使用率 -->
          <el-col :xs="24" :lg="8">