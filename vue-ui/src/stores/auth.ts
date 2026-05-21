import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'auth.token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))

  const isAuthenticated = computed(() => token.value !== null)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
  }

  function clearToken() {
    token.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, isAuthenticated, setToken, clearToken }
})
