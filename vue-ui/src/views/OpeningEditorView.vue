<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { TheChessboard, type BoardApi } from 'vue3-chessboard'
import 'vue3-chessboard/style.css'
import { useOpeningsStore } from '@/stores/openings'

const route = useRoute()
const router = useRouter()
const store = useOpeningsStore()

const isNew = computed(() => route.name === 'opening-new')
const openingId = computed<number | null>(() => {
  const raw = route.params.id
  if (!raw || Array.isArray(raw)) return null
  const parsed = Number.parseInt(raw, 10)
  return Number.isFinite(parsed) ? parsed : null
})

const name = ref('')
const notes = ref('')
const pgn = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)

let boardApi: BoardApi | undefined

function handleBoardCreated(api: BoardApi) {
  boardApi = api
  if (pgn.value) {
    try {
      boardApi.loadPgn(pgn.value)
    } catch {
      /* leave board at start position */
    }
  }
}

function handleMove() {
  if (boardApi) {
    pgn.value = boardApi.getPgn()
  }
}

function resetBoard() {
  boardApi?.resetBoard()
  pgn.value = ''
}

async function save() {
  if (!name.value.trim()) {
    error.value = 'Name is required'
    return
  }
  if (!pgn.value.trim()) {
    error.value = 'Make at least one move on the board before saving'
    return
  }
  saving.value = true
  error.value = null
  try {
    const req = {
      name: name.value.trim(),
      pgn: pgn.value,
      notes: notes.value.trim() || null,
    }
    if (isNew.value || openingId.value === null) {
      await store.create(req)
    } else {
      await store.update(openingId.value, req)
    }
    router.push({ name: 'openings' })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to save'
  } finally {
    saving.value = false
  }
}

function cancel() {
  router.push({ name: 'openings' })
}

onMounted(async () => {
  if (!isNew.value && openingId.value !== null) {
    loading.value = true
    try {
      const opening = await store.fetchOne(openingId.value)
      name.value = opening.name
      notes.value = opening.notes ?? ''
      pgn.value = opening.pgn
      if (boardApi) {
        try {
          boardApi.loadPgn(opening.pgn)
        } catch {
          /* board stays at start */
        }
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load opening'
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <main class="editor">
    <header>
      <h1>{{ isNew ? 'New opening' : 'Edit opening' }}</h1>
      <button @click="cancel">← Back</button>
    </header>

    <p v-if="loading">Loading…</p>

    <div v-else class="layout">
      <div class="board-column">
        <TheChessboard @board-created="handleBoardCreated" @move="handleMove" />
        <button class="reset" @click="resetBoard">Reset board</button>
      </div>

      <form class="form" @submit.prevent="save">
        <label>
          Name
          <input v-model="name" type="text" required maxlength="255" placeholder="e.g. Ruy Lopez" />
        </label>

        <label>
          PGN
          <textarea v-model="pgn" readonly rows="3" />
          <small>Recorded automatically as you play moves on the board.</small>
        </label>

        <label>
          Notes (optional)
          <textarea v-model="notes" rows="4" placeholder="Key ideas, traps, transpositions…" />
        </label>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <div class="actions">
          <button type="button" @click="cancel">Cancel</button>
          <button type="submit" class="primary" :disabled="saving">
            {{ saving ? 'Saving…' : 'Save' }}
          </button>
        </div>
      </form>
    </div>
  </main>
</template>

<style scoped>
.editor {
  max-width: 1000px;
  margin: 2rem auto;
  padding: 0 1rem;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
h1 {
  margin: 0;
}
.layout {
  display: grid;
  grid-template-columns: minmax(320px, 480px) 1fr;
  gap: 2rem;
  align-items: start;
}
@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
.board-column {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
}
input,
textarea {
  padding: 0.5rem;
  font: inherit;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-background);
  color: var(--color-text);
}
textarea[readonly] {
  opacity: 0.85;
  font-family: ui-monospace, monospace;
  font-size: 0.85rem;
}
small {
  opacity: 0.7;
}
button {
  padding: 0.6rem 1rem;
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
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
.error {
  color: hsl(0, 60%, 50%);
  margin: 0;
}
</style>
