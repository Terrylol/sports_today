<script setup>
import { ref, onMounted, computed } from 'vue'
import LoginModal from '../components/LoginModal.vue'
import TeamSelector from '../components/TeamSelector.vue'

// State
const currentUser = ref(null)
const isChangingTeam = ref(false)
const events = ref([])
const loading = ref(false)
const aiLoading = ref(false)
const error = ref(null)
const todayDate = ref('')

// Computed
const showLogin = computed(() => !currentUser.value)
const showTeamSelector = computed(() => currentUser.value && (!currentUser.value.favoriteTeam || isChangingTeam.value))
const showContent = computed(() => currentUser.value && currentUser.value.favoriteTeam && !isChangingTeam.value)

const sortedEvents = computed(() => {
  return [...events.value].sort((a, b) => b.eventYear - a.eventYear)
})

// Methods
const getTodayString = () => {
  const date = new Date()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

const login = async (username) => {
  try {
    const res = await fetch(`/api/users/login?username=${username}`, { method: 'POST' })
    if (!res.ok) throw new Error('Login failed')
    currentUser.value = await res.json()
    localStorage.setItem('username', username)
    if (currentUser.value.favoriteTeam) {
      fetchEvents()
    }
  } catch (err) {
    alert('Login error: ' + err.message)
  }
}

const handleTeamSelect = async (team) => {
  try {
    // Update backend
    await fetch(`/api/users/${currentUser.value.id}/team?teamId=${team.id}`, { method: 'PUT' })
    // Update local state
    currentUser.value.favoriteTeam = team
    currentUser.value.favoriteTeamId = team.id
    isChangingTeam.value = false
    fetchEvents()
  } catch (err) {
    alert('Update team failed: ' + err.message)
  }
}

const fetchEvents = async () => {
  if (!currentUser.value?.favoriteTeam) return
  
  try {
    loading.value = true
    error.value = null
    const res = await fetch(`/api/events/today?teamId=${currentUser.value.favoriteTeam.id}`)
    if (!res.ok) throw new Error('Failed to fetch events')
    events.value = await res.json()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

const fetchAiEvent = async () => {
  if (!currentUser.value?.favoriteTeam) return

  try {
    aiLoading.value = true
    const res = await fetch(`/api/events/fetch-ai?teamId=${currentUser.value.favoriteTeam.id}`, {
      method: 'POST'
    })
    if (!res.ok) throw new Error('AI Fetch failed')
    const newEvents = await res.json()
    
    // Handle array response
    const eventsToAdd = Array.isArray(newEvents) ? newEvents : [newEvents]
    
    // Add only if not already in list (client-side double check)
    const existingIds = new Set(events.value.map(e => e.id))
    eventsToAdd.forEach(evt => {
        if (!existingIds.has(evt.id)) {
            events.value.push(evt)
        }
    })
    
    // Sort by year descending
    events.value.sort((a, b) => b.eventYear - a.eventYear)
    
  } catch (err) {
    alert('AI 挖掘失败: ' + err.message)
  } finally {
    aiLoading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('username')
  currentUser.value = null
  events.value = []
}

onMounted(() => {
  todayDate.value = getTodayString()
  const storedUser = localStorage.getItem('username')
  if (storedUser) {
    login(storedUser)
  }
})
</script>

<template>
  <main class="app-container">
    <!-- 1. Login Modal -->
    <LoginModal v-if="showLogin" @login="login" />

    <!-- 2. Team Selector -->
    <TeamSelector 
      v-if="showTeamSelector" 
      :userId="currentUser?.id"
      @select="handleTeamSelect" 
    />

    <!-- 3. Main Content -->
    <div v-if="showContent" class="main-content">
      <header class="header">
        <div class="header-content">
          <div class="user-info">
            <div class="avatar-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                    <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                </svg>
            </div>
            <span class="greeting">{{ currentUser.nickname }}</span>
          </div>
          <div class="title-area">
            <img :src="currentUser.favoriteTeam.logoUrl" class="team-logo-small" />
            <h1>历史上今天</h1>
          </div>
          <div class="settings">
            <button @click="isChangingTeam = true" class="icon-btn" title="切换主队">🔄</button>
            <button @click="logout" class="icon-btn" title="登出">🚪</button>
          </div>
        </div>
      </header>

      <div class="content-body">
        <div class="actions">
          <button @click="fetchAiEvent" :disabled="aiLoading" class="ai-btn">
            <span v-if="aiLoading">🔮 正在挖掘未知历史...</span>
            <span v-else>🔮 AI 挖掘 {{ currentUser.favoriteTeam.displayName }} 历史</span>
          </button>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>正在加载历史数据...</p>
        </div>

        <div v-else-if="error" class="error-state">
          ⚠️ {{ error }}
        </div>

        <div v-else class="timeline">
          <div v-if="events.length === 0" class="empty-state">
            <p>今天历史上好像很平静...</p>
            <button @click="fetchAiEvent" class="ai-btn-small">试试 AI 挖掘</button>
          </div>
          
          <div v-for="event in sortedEvents" :key="event.id" class="event-card fade-in">
            <div class="card-header">
              <span class="year-badge">{{ event.eventYear }}</span>
              <span class="event-type-badge" :class="event.type">{{ event.type }}</span>
            </div>
            
            <div class="card-body">
              <h2 class="event-title">{{ event.title }}</h2>
              <p class="event-desc">{{ event.description }}</p>
              <img v-if="event.imageUrl" :src="event.imageUrl" class="event-image" alt="Event Image"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style>
:root {
  --primary-color: #DA291C;
  --bg-color: #F5F7FA;
  --text-primary: #1A1A1A;
}

body {
  margin: 0;
  background-color: var(--bg-color);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.app-container {
  min-height: 100vh;
}

.header {
  background: white;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar-icon {
  width: 32px;
  height: 32px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #555;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.team-logo-small {
  width: 30px;
  height: 30px;
}

.header h1 {
  font-size: 1.1rem;
  margin: 0;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.2rem;
}

.content-body {
  max-width: 600px;
  margin: 0 auto;
  padding: 1rem;
}

.actions {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}

.ai-btn {
  background: linear-gradient(90deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(37, 117, 252, 0.3);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.event-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.year-badge {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
  color: #333;
}

.event-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
}

.event-desc {
  color: #666;
  line-height: 1.6;
}

.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
