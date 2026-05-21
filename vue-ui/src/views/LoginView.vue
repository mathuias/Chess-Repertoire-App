<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login } from '@/lib/api'
import { useAuthStore } from '@/stores/auth'

const email = ref('alice@example.com')
const password = ref('password')
const error = ref<string | null>(null)
const submitting = ref(false)

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

async function submit() {
  error.value = null
  submitting.value = true
  try {
    const { token } = await login(email.value, password.value)
    auth.setToken(token)
    const redirect = (route.query.redirect as string | undefined) ?? '/hello'
    router.replace(redirect)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Login failed'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login">
    <form @submit.prevent="submit">
      <h1>Sign in</h1>

      <label>
        Email
        <input v-model="email" type="email" required autocomplete="username" />
      </label>

      <label>
        Password
        <input v-model="password" type="password" required autocomplete="current-password" />
      </label>

      <button type="submit" :disabled="submitting">
        {{ submitting ? 'Signing in…' : 'Sign in' }}
      </button>

      <p v-if="error" class="error" role="alert">{{ error }}</p>
    </form>
  </main>
</template>

<style scoped>
.login {
  display: grid;
  place-items: center;
  min-height: 60vh;
}
form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-width: 280px;
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}
label {
  display: flex;
  flex-direction: column;
  font-size: 0.875rem;
  gap: 0.25rem;
}
input {
  padding: 0.5rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-background);
  color: var(--color-text);
}
button {
  padding: 0.6rem;
  font: inherit;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid var(--color-border);
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error {
  color: hsl(0, 60%, 50%);
  margin: 0;
}
</style>
