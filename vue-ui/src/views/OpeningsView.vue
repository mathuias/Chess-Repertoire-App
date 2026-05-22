<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useOpeningsStore } from '@/stores/openings'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const openingsStore = useOpeningsStore()
const auth = useAuthStore()
const { items, loading, error } = storeToRefs(openingsStore)

onMounted(() => {
  openingsStore.fetchAll()
})

function openEditor(id: number) {
  router.push({ name: 'opening-edit', params: { id: String(id) } })
}

function newOpening() {
  router.push({ name: 'opening-new' })
}

async function remove(id: number, name: string) {
  if (!confirm(`Delete "${name}"?`)) return
  try {
    await openingsStore.remove(id)
  } catch (e) {
    alert(e instanceof Error ? e.message : 'Failed to delete')
  }
}

function logout() {
  auth.clearToken()
  router.replace({ name: 'login' })
}

function pgnPreview(pgn: string): string {
  const trimmed = pgn.trim().replace(/\s+/g, ' ')
  return trimmed.length > 80 ? trimmed.slice(0, 80) + '…' : trimmed
}
</script>

<template>
  <main class="openings">
    <header>
      <h1>My Openings</h1>
      <div class="actions">
        <button class="primary" @click="newOpening">+ New opening</button>
        <button @click="logout">Sign out</button>
      </div>
    </header>

    <p v-if="loading">Loading…</p>
    <p v-else-if="error" class="error" role="alert">{{ error }}</p>
    <p v-else-if="items.length === 0" class="empty">
      No openings yet. Click <strong>+ New opening</strong> to record your first line.
    </p>

    <ul v-else class="list">
      <li v-for="opening in items" :key="opening.id" @click="openEditor(opening.id)">
        <div class="row">
          <div class="info">
            <h3>{{ opening.name }}</h3>
            <code>{{ pgnPreview(opening.pgn) }}</code>
            <p v-if="opening.notes" class="notes">{{ opening.notes }}</p>
          </div>
          <button class="delete" @click.stop="remove(opening.id, opening.name)">Delete</button>
        </div>
      </li>
    </ul>
  </main>
</template>

<style scoped>
.openings {
  max-width: 720px;
  margin: 2rem auto;
  padding: 0 1rem;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.actions {
  display: flex;
  gap: 0.5rem;
}
button {
  padding: 0.5rem 1rem;
  font: inherit;
  cursor: pointer;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-background);
  color: var(--color-text);
}
button.primary {
  background: hsl(120, 40%, 35%);
  color: white;
  border-color: hsl(120, 40%, 25%);
}
.empty {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--color-text);
  opacity: 0.7;
}
.error {
  color: hsl(0, 60%, 50%);
}
.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.list li {
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 1rem;
  cursor: pointer;
  transition: background 0.1s;
}
.list li:hover {
  background: var(--color-background-mute);
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}
.info {
  flex: 1;
  min-width: 0;
}
h3 {
  margin: 0 0 0.25rem;
}
code {
  display: block;
  font-size: 0.8rem;
  opacity: 0.85;
  word-break: break-all;
}
.notes {
  margin: 0.5rem 0 0;
  font-size: 0.875rem;
  opacity: 0.75;
}
.delete {
  color: hsl(0, 60%, 45%);
  border-color: hsl(0, 60%, 60%);
}
</style>
