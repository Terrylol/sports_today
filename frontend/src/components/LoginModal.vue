<script setup>
import { ref } from 'vue'

const username = ref('')
const emit = defineEmits(['login'])

const handleLogin = () => {
  if (username.value.trim()) {
    emit('login', username.value.trim())
  }
}
</script>

<template>
  <div class="modal-overlay">
    <div class="modal-card">
      <h2>欢迎来到球队历史</h2>
      <p>请输入您的用户名开始</p>
      <input 
        v-model="username" 
        @keyup.enter="handleLogin"
        placeholder="例如: fan123" 
        class="username-input"
        autofocus
      />
      <button @click="handleLogin" class="login-btn" :disabled="!username.trim()">
        进入
      </button>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-card {
  background: white;
  padding: 2rem;
  border-radius: 16px;
  width: 90%;
  max-width: 320px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  animation: slideUp 0.3s ease-out;
}

h2 {
  margin-top: 0;
  color: #1a1a1a;
}

.username-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #eee;
  border-radius: 8px;
  margin: 1.5rem 0;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.2s;
}

.username-input:focus {
  border-color: #DA291C;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #DA291C;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
