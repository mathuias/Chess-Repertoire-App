<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHello } from '@/lib/api'
import { useAuthStore } from '@/stores/auth'

const message = ref<string | null>(null)
const error = ref<string | null>(null)

const auth = useAuthStore()
const router = useRouter()

onMounted(async () => {
  try {
    message.value = await getHello()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load'
  }
})

function logout() {
  auth.clearToken()
  router.replace({ name: 'login' })
}
</script>

<template>
  <main class="hello">
    <h1 v-if="message">{{ message }}</h1>
    <p v-else-if="error" class="error">{{ error }}</p>
    <p v-else>Loading…</p>
    <button @click="logout">Sign out</button>
  </main>
</template>

<style scoped>
.hello {
  display: grid;
  place-items: center;
  min-height: 60vh;
  gap: 1rem;
}
h1 {
  font-size: 2.5rem;
}
button {
  padding: 0.5rem 1rem;
  font: inherit;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid var(--color-border);
}
.error {
  color: hsl(0, 60%, 50%);
}
</style>
