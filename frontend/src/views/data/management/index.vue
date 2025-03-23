<template>
  <div class="data-management-container">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleUpload">
        <el-icon><Upload /></el-icon>上传数据
      </el-button>
      <el-button type="success" @click="handleImport">
        <el-icon><Download /></el-icon>导入数据
      </el-button>
      <el-button type="danger" :disabled="!hasSelected" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>批量删除
      </el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索数据名称"
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

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="数据名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="type" label="数据类型" width="120">
        <template #default="scope">
          <el-tag :type="getTypeTagType(scope.row.type)">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" width="120" />
      <el-table-column prop="source" label="数据源" width="150" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'available' ? 'success' : scope.row.status === 'processing' ? 'warning' : 'info'">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleAnalyze(scope.row)">分析</el-button>
          <el-dropdown>
            <el-button size="small" type="primary">
              更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDownload(scope.row)">
                  <el-icon><Download /></el-icon>下载
                </el-dropdown-item>
                <el-dropdown-item @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-dropdown-item>
                <el-dropdown-item @click="handleHistory(scope.row)">
                  <el-icon><Timer /></el-icon>历史版本
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

    <!-- 上传数据对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传数据" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="数据名称" required>
          <el-input v-model="uploadForm.name" placeholder="请输入数据名称" />
        </el-form-item>
        <el-form-item label="数据类型" required>
          <el-select v-model="uploadForm.type" placeholder="请选择数据类型" style="width: 100%">
            <el-option label="CSV" value="CSV" />
            <el-option label="JSON" value="JSON" />
            <el-option label="Excel" value="Excel" />
            <el-option label="Text" value="Text" />
            <el-option label="其他" value="Other" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源">
          <el-input v-model="uploadForm.source" placeholder="请输入数据源" />
        </el-form-item>
        <el-form-item label="文件上传" required>
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传CSV、JSON、Excel等格式文件，单个文件不超过500MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" rows="3" placeholder="请输入数据描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading">
            上传
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入数据对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入数据" width="500px">
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="数据源类型" required>
          <el-select v-model="importForm.sourceType" placeholder="请选择数据源类型" style="width: 100%">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="MongoDB" value="MongoDB" />
            <el-option label="Kafka" value="Kafka" />
            <el-option label="HDFS" value="HDFS" />
            <el-option label="API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接信息" required>
          <el-input v-model="importForm.connection" placeholder="请输入连接信息" />
        </el-form-item>
        <el-form-item label="数据名称" required>
          <el-input v-model="importForm.name" placeholder="请输入数据名称" />
        </el-form-item>
        <el-form-item label="导入方式">
          <el-radio-group v-model="importForm.importType">
            <el-radio label="full">全量导入</el-radio>
            <el-radio label="incremental">增量导入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="importForm.description" type="textarea" rows="3" placeholder="请输入数据描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitImport" :loading="importing">
            导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const selectedRows = ref([])
const hasSelected = computed(() => selectedRows.value.length > 0)

// 上传对话框
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadForm = reactive({
  name: '',
  type: '',
  source: '',
  description: '',
  file: null
})

// 导入对话框
const importDialogVisible = ref(false)
const importing = ref(false)
const importForm = reactive({
  sourceType: '',
  connection: '',
  name: '',
  importType: 'full',
  description: ''
})

// 获取数据列表
const fetchData = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    const mockData = []
    for (let i = 1; i <= 50; i++) {
      mockData.push({
        id: i,
        name: `数据集${i}`,
        type: ['CSV', 'JSON', 'Excel', 'Text', 'Other'][Math.floor(Math.random() * 5)],
        size: `${(Math.random() * 500).toFixed(2)} MB`,
        source: ['MySQL', 'MongoDB', 'Kafka', 'HDFS', 'API', '文件上传'][Math.floor(Math.random() * 6)],
        updateTime: new Date(Date.now() - Math.floor(Math.random() * 30) * 24 * 60 * 60 * 1000).toLocaleString(),
        status: ['available', 'processing', 'archived'][Math.floor(Math.random() * 3)]
      })
    }
    
    // 过滤和分页
    let filteredData = mockData
    if (searchQuery.value) {
      filteredData = mockData.filter(item => item.name.includes(searchQuery.value))
    }
    
    total.value = filteredData.length
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = filteredData.slice(start, end)
    
    loading.value = false
  }, 500)
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'available': return '可用'
    case 'processing': return '处理中'
    case 'archived': return '已归档'
    default: return '未知'
  }
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  switch (type) {
    case 'CSV': return ''
    case 'JSON': return 'success'
    case 'Excel': return 'warning'
    case 'Text': return 'info'
    default: return 'info'
  }
}

// 处理表格选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchData()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchData()
}

// 处理上传按钮点击
const handleUpload = () => {
  // 重置表单
  Object.keys(uploadForm).forEach(key => {
    uploadForm[key] = ''
  })
  uploadForm.file = null
  uploadDialogVisible.value = true
}

// 处理文件变化
const handleFileChange = (file) => {
  uploadForm.file = file.raw
}

// 提交上传
const submitUpload = () => {
  // 表单验证
  if (!uploadForm.name || !uploadForm.type || !uploadForm.file) {
    ElMessage.warning('请填写必填项并上传文件')
    return
  }
  
  uploading.value = true
  
  // 模拟上传过程
  setTimeout(() => {
    uploading.value = false
    uploadDialogVisible.value = false
    ElMessage.success('数据上传成功')
    fetchData() // 刷新数据列表
  }, 1500)
}

// 处理导入按钮点击
const handleImport = () => {
  // 重置表单
  Object.keys(importForm).forEach(key => {
    if (key !== 'importType') {
      importForm[key] = ''
    }
  })
  importForm.importType = 'full'
  importDialogVisible.value = true
}

// 提交导入
con