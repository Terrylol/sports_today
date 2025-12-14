<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const username = ref('')
const password = ref('')
const isAuthenticated = ref(false)
const teams = ref([])
const selectedTeamId = ref(null)
const currentTaskId = ref(null)
const taskStatus = ref(null)
const pollingInterval = ref(null)
const errorMsg = ref('')

const API_BASE = 'http://localhost:8081/api'

const getAuthHeader = () => {
  return 'Basic ' + btoa(username.value + ':' + password.value)
}

const login = async () => {
  try {
    const response = await fetch(`${API_BASE}/admin/check-auth`, {
      headers: {
        'Authorization': getAuthHeader()
      }
    })
    if (response.ok) {
      isAuthenticated.value = true
      errorMsg.value = ''
      fetchTeams()
    } else {
      errorMsg.value = '认证失败，请检查用户名和密码'
    }
  } catch (e) {
    errorMsg.value = '连接服务器失败'
  }
}

const fetchTeams = async () => {
  try {
    const response = await fetch(`${API_BASE}/teams`)
    if (response.ok) {
      teams.value = await response.json()
    }
  } catch (e) {
    console.error('Failed to fetch teams', e)
  }
}

const startRefresh = async (fullRefresh) => {
  if (!selectedTeamId.value) {
    alert('请先选择球队')
    return
  }
  
  if (fullRefresh && !confirm('确定要全量刷新吗？这将删除该球队所有现有的历史数据！')) {
    return
  }

  try {
    const response = await fetch(`${API_BASE}/admin/refresh?teamId=${selectedTeamId.value}&fullRefresh=${fullRefresh}`, {
      method: 'POST',
      headers: {
        'Authorization': getAuthHeader()
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      currentTaskId.value = data.taskId
      startPolling()
    } else {
      alert('启动任务失败')
    }
  } catch (e) {
    console.error(e)
    alert('启动任务出错')
  }
}

const checkTaskStatus = async () => {
  if (!currentTaskId.value) return

  try {
    const response = await fetch(`${API_BASE}/admin/task/${currentTaskId.value}`, {
      headers: {
        'Authorization': getAuthHeader()
      }
    })
    
    if (response.ok) {
      taskStatus.value = await response.json()
      if (taskStatus.value.status === 'COMPLETED' || taskStatus.value.status === 'FAILED') {
        stopPolling()
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const startPolling = () => {
  if (pollingInterval.value) clearInterval(pollingInterval.value)
  checkTaskStatus() // check immediately
  pollingInterval.value = setInterval(checkTaskStatus, 2000)
}

const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

onUnmounted(() => {
  stopPolling()
})

</script>

<template>
  <div class="admin-page">
    <div class="admin-panel">
      <div class="header">
        <h2>后台管理</h2>
      </div>

      <div v-if="!isAuthenticated" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="admin">
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="admin123">
        </div>
        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
        <button @click="login" class="primary-btn">登录</button>
      </div>

      <div v-else class="dashboard">
        <div class="control-panel">
          <div class="form-group">
            <label>选择球队</label>
            <select v-model="selectedTeamId">
              <option v-for="team in teams" :key="team.id" :value="team.id">
                {{ team.displayName }}
              </option>
            </select>
          </div>
          
          <div class="actions">
            <button @click="startRefresh(false)" class="action-btn" :disabled="!selectedTeamId || !!pollingInterval">
              增量刷新 (填补空缺)
            </button>
            <button @click="startRefresh(true)" class="action-btn danger" :disabled="!selectedTeamId || !!pollingInterval">
              全量刷新 (覆盖所有)
            </button>
          </div>
        </div>

        <div v-if="taskStatus" class="task-monitor">
          <h3>任务状态: {{ taskStatus.status }}</h3>
          <div class="progress-bar-container">
            <div class="progress-bar" :style="{ width: taskStatus.progress + '%' }"></div>
          </div>
          <div class="progress-text">{{ taskStatus.progress }}%</div>
          
          <div class="logs">
            <div v-for="(log, index) in taskStatus.logs" :key="index" class="log-entry">
              {{ log }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.admin-panel {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.primary-btn {
  width: 100%;
  padding: 0.8rem;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
}

.error {
  color: red;
  margin-bottom: 1rem;
}

.control-panel {
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.action-btn {
  flex: 1;
  padding: 0.8rem;
  background: #2196F3;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.action-btn.danger {
  background: #f44336;
}

.action-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.task-monitor {
  background: #f9f9f9;
  padding: 1rem;
  border-radius: 8px;
}

.progress-bar-container {
  width: 100%;
  height: 10px;
  background: #eee;
  border-radius: 5px;
  overflow: hidden;
  margin: 10px 0;
}

.progress-bar {
  height: 100%;
  background: #4CAF50;
  transition: width 0.3s ease;
}

.logs {
  margin-top: 1rem;
  max-height: 200px;
  overflow-y: auto;
  background: #333;
  color: #0f0;
  padding: 0.5rem;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9rem;
}

.log-entry {
  margin-bottom: 4px;
}
</style>
