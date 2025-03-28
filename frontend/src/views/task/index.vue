<template>
  <div class="task-container">
    <!-- 任务列表区域 -->
    <div class="task-list">
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="name" label="任务名称" width="180" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="progress" label="进度" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页控制 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTaskList, deleteTask } from '@/api/task'

// 任务列表数据
const taskList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取任务列表
const fetchTaskList = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await getTaskList(params)
    taskList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTaskList()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTaskList()
}

// 查看任务详情
const handleView = (row) => {
  console.log('查看任务:', row)
}

// 删除任务
const handleDelete = async (row) => {
  try {
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    fetchTaskList()
  } catch (error) {
    console.error('删除任务失败:', error)
    ElMessage.error('删除失败')
  }
}

// 初始化加载数据
onMounted(() => {
  fetchTaskList()
})
</script>

<style scoped>
.task-container {
  padding: 20px;
}

.task-list {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}
</style>