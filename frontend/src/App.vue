<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* Global styles */
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
</style>
