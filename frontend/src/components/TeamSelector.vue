<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps(['userId'])
const emit = defineEmits(['select'])
const teams = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await fetch('/api/teams')
    teams.value = await res.json()
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
})

const selectTeam = (team) => {
  emit('select', team)
}
</script>

<template>
  <div class="team-selector-page">
    <h2>选择你支持的主队</h2>
    <div v-if="loading" class="loading">加载球队中...</div>
    <div v-else class="teams-grid">
      <div 
        v-for="team in teams" 
        :key="team.id" 
        class="team-card"
        @click="selectTeam(team)"
      >
        <img :src="team.logoUrl" :alt="team.displayName" class="team-logo"/>
        <span class="team-name">{{ team.displayName }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.team-selector-page {
  padding: 2rem;
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.team-card {
  background: white;
  padding: 1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.team-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.team-logo {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.team-name {
  font-size: 0.9rem;
  color: #333;
  font-weight: 500;
}
</style>
