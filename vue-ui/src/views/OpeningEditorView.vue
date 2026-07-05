<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { TheChessboard, type BoardApi } from 'vue3-chessboard'
import 'vue3-chessboard/style.css'
import { Chess, type Move } from 'chess.js'
import type { DrawShape } from 'chessground/draw'
import { useOpeningsStore } from '@/stores/openings'
import {
  parseAnnotation,
  serializeAnnotation,
  isEmptyAnnotation,
  type Annotation,
} from '@/lib/annotations'

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

// --- Board / analysis state -------------------------------------------------
let boardApi: BoardApi | undefined
const moves = ref<Move[]>([])
const currentPly = ref(0)
const initialFen = ref<string | null>(null)
const annotations = ref(new Map<number, Annotation>())
const fenInput = ref('')
const pgnInput = ref('')
const loadError = ref<string | null>(null)

// Prevents our own setShapes() calls from being treated as user edits.
let suppressShapeChange = false

const totalPlies = computed(() => moves.value.length)

const moveRows = computed(() => {
  const rows: {
    number: number
    white: { san: string; ply: number }
    black: { san: string; ply: number } | null
  }[] = []
  const list = moves.value
  for (let i = 0; i < list.length; i += 2) {
    const white = list[i]!
    const black = list[i + 1]
    rows.push({
      number: i / 2 + 1,
      white: { san: white.san, ply: i + 1 },
      black: black ? { san: black.san, ply: i + 2 } : null,
    })
  }
  return rows
})

const currentComment = computed<string>({
  get: () => annotations.value.get(currentPly.value)?.comment ?? '',
  set: (value) => {
    const existing = annotations.value.get(currentPly.value) ?? { comment: '', shapes: [] }
    annotations.value.set(currentPly.value, { ...existing, comment: value })
  },
})

function hasAnnotation(ply: number): boolean {
  return !isEmptyAnnotation(annotations.value.get(ply))
}

function refreshMoves() {
  moves.value = boardApi ? boardApi.getHistory(true) : []
  pgn.value = boardApi ? boardApi.getPgn() : ''
}

function syncCurrentPly() {
  if (boardApi) currentPly.value = boardApi.getCurrentPlyNumber()
}

function renderShapesForPly() {
  if (!boardApi) return
  const shapes = annotations.value.get(currentPly.value)?.shapes ?? []
  suppressShapeChange = true
  boardApi.setShapes(shapes as Parameters<typeof boardApi.setShapes>[0])
  suppressShapeChange = false
}

/** Rebuild the ply->annotation map from a PGN string (with Lichess-style comments). */
function rebuildAnnotations(pgnStr: string) {
  const map = new Map<number, Annotation>()
  try {
    const tmp = new Chess()
    tmp.loadPgn(pgnStr)
    const verbose = tmp.history({ verbose: true })
    const fenToPly = new Map<string, number>()
    fenToPly.set(verbose[0]?.before ?? tmp.fen(), 0)
    verbose.forEach((m, i) => fenToPly.set(m.after, i + 1))
    for (const { fen, comment } of tmp.getComments()) {
      const ply = fenToPly.get(fen)
      if (ply !== undefined) map.set(ply, parseAnnotation(comment))
    }
  } catch {
    /* no comments recovered */
  }
  annotations.value = map
}

/** Load a PGN onto the board and rehydrate moves + annotations. */
function applyLoadedPgn(pgnStr: string, startFen: string | null) {
  if (!boardApi) return
  boardApi.loadPgn(pgnStr)
  initialFen.value = startFen
  rebuildAnnotations(pgnStr)
  refreshMoves()
  boardApi.stopViewingHistory()
  syncCurrentPly()
  renderShapesForPly()
}

// --- Navigation -------------------------------------------------------------
function goToPly(ply: number) {
  if (!boardApi) return
  if (ply <= 0) boardApi.viewStart()
  else if (ply >= totalPlies.value) boardApi.stopViewingHistory()
  else boardApi.viewHistory(ply)
  syncCurrentPly()
  renderShapesForPly()
}

function goStart() {
  boardApi?.viewStart()
  syncCurrentPly()
  renderShapesForPly()
}
function goPrev() {
  boardApi?.viewPrevious()
  syncCurrentPly()
  renderShapesForPly()
}
function goNext() {
  boardApi?.viewNext()
  syncCurrentPly()
  renderShapesForPly()
}
function goEnd() {
  boardApi?.stopViewingHistory()
  syncCurrentPly()
  renderShapesForPly()
}
function flipBoard() {
  boardApi?.toggleOrientation()
}

// --- Board event handlers ---------------------------------------------------
function handleBoardCreated(api: BoardApi) {
  boardApi = api
  if (pgn.value) {
    try {
      applyLoadedPgn(pgn.value, initialFen.value)
    } catch {
      /* leave board at start position */
    }
  }
}

function handleMove() {
  refreshMoves()
  syncCurrentPly()
  renderShapesForPly()
}

function handleShapesChanged(shapes: DrawShape[]) {
  if (suppressShapeChange) return
  const existing = annotations.value.get(currentPly.value) ?? { comment: '', shapes: [] }
  annotations.value.set(currentPly.value, {
    ...existing,
    shapes: shapes.map((s) => ({ ...s })),
  })
}

const boardConfig = {
  coordinates: true,
  drawable: {
    enabled: true,
    visible: true,
    onChange: handleShapesChanged,
  },
}

// --- Load from FEN / PGN ----------------------------------------------------
function loadFromFen() {
  loadError.value = null
  const fen = fenInput.value.trim()
  if (!fen) return
  try {
    new Chess(fen) // validate; throws on invalid FEN
  } catch {
    loadError.value = 'Invalid FEN'
    return
  }
  if (!boardApi) return
  boardApi.setPosition(fen)
  initialFen.value = fen
  annotations.value = new Map()
  refreshMoves()
  syncCurrentPly()
  renderShapesForPly()
}

function loadFromPgn() {
  loadError.value = null
  const text = pgnInput.value.trim()
  if (!text) return
  let startFen: string | null = null
  try {
    const tmp = new Chess()
    tmp.loadPgn(text) // validate; throws on invalid PGN
    startFen = tmp.getHeaders().FEN ?? null
  } catch {
    loadError.value = 'Invalid PGN'
    return
  }
  applyLoadedPgn(text, startFen)
}

// --- Reset / save -----------------------------------------------------------
function resetBoard() {
  boardApi?.resetBoard()
  annotations.value = new Map()
  initialFen.value = null
  fenInput.value = ''
  pgnInput.value = ''
  loadError.value = null
  refreshMoves()
  syncCurrentPly()
  renderShapesForPly()
}

/** Rebuild the PGN with embedded comments/arrows for persistence. */
function buildAnnotatedPgn(): string {
  const replay = initialFen.value ? new Chess(initialFen.value) : new Chess()
  const start = serializeAnnotation(annotations.value.get(0))
  if (start) replay.setComment(start)
  moves.value.forEach((m, i) => {
    replay.move(m.san)
    const body = serializeAnnotation(annotations.value.get(i + 1))
    if (body) replay.setComment(body)
  })
  return replay.pgn()
}

async function save() {
  if (!name.value.trim()) {
    error.value = 'Name is required'
    return
  }
  if (moves.value.length === 0) {
    error.value = 'Make at least one move on the board before saving'
    return
  }
  saving.value = true
  error.value = null
  try {
    const req = {
      name: name.value.trim(),
      pgn: buildAnnotatedPgn(),
      initialFen: initialFen.value,
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

// --- Keyboard navigation ----------------------------------------------------
function isEditableTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false
  const tag = target.tagName
  return tag === 'INPUT' || tag === 'TEXTAREA' || target.isContentEditable
}

function handleKeydown(event: KeyboardEvent) {
  if (isEditableTarget(event.target)) return
  switch (event.key) {
    case 'ArrowLeft':
      goPrev()
      break
    case 'ArrowRight':
      goNext()
      break
    case 'ArrowUp':
    case 'Home':
      goStart()
      break
    case 'ArrowDown':
    case 'End':
      goEnd()
      break
    case 'f':
    case 'F':
      flipBoard()
      break
    default:
      return
  }
  event.preventDefault()
}

onMounted(async () => {
  window.addEventListener('keydown', handleKeydown)
  if (!isNew.value && openingId.value !== null) {
    loading.value = true
    try {
      const opening = await store.fetchOne(openingId.value)
      name.value = opening.name
      notes.value = opening.notes ?? ''
      pgn.value = opening.pgn
      initialFen.value = opening.initialFen
      if (boardApi) {
        try {
          applyLoadedPgn(opening.pgn, opening.initialFen)
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

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
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
        <TheChessboard
          :board-config="boardConfig"
          @board-created="handleBoardCreated"
          @move="handleMove"
        />

        <div class="nav-controls" role="group" aria-label="Move navigation">
          <button type="button" title="Start (↑ / Home)" @click="goStart">⏮</button>
          <button type="button" title="Previous (←)" @click="goPrev">◀</button>
          <button type="button" title="Next (→)" @click="goNext">▶</button>
          <button type="button" title="End (↓ / End)" @click="goEnd">⏭</button>
          <button type="button" title="Flip board (f)" @click="flipBoard">⟲</button>
          <button type="button" class="reset" @click="resetBoard">Reset</button>
        </div>
        <p class="hint">
          Use ← / → to step through moves, ↑ / ↓ for start / end, <kbd>f</kbd> to flip.
          Right-click-drag on the board to draw arrows and mark squares.
        </p>
      </div>

      <div class="side-column">
        <section class="panel">
          <h2>Moves</h2>
          <ol v-if="moveRows.length" class="move-list">
            <li v-for="row in moveRows" :key="row.number">
              <span class="move-number">{{ row.number }}.</span>
              <button
                type="button"
                class="ply"
                :class="{ active: currentPly === row.white.ply }"
                @click="goToPly(row.white.ply)"
              >
                {{ row.white.san }}
                <span v-if="hasAnnotation(row.white.ply)" class="mark" title="Has notes">📝</span>
              </button>
              <button
                v-if="row.black"
                type="button"
                class="ply"
                :class="{ active: currentPly === row.black.ply }"
                @click="goToPly(row.black.ply)"
              >
                {{ row.black.san }}
                <span v-if="hasAnnotation(row.black.ply)" class="mark" title="Has notes">📝</span>
              </button>
            </li>
          </ol>
          <p v-else class="empty">No moves yet — play on the board or load a game below.</p>
        </section>

        <section class="panel">
          <h2>
            Comment
            <small>({{ currentPly === 0 ? 'starting position' : `after move ${currentPly}` }})</small>
          </h2>
          <textarea
            v-model="currentComment"
            rows="3"
            placeholder="Notes for this move — key ideas, plans, traps…"
          />
        </section>

        <details class="panel">
          <summary>Load a game</summary>
          <label>
            From FEN
            <div class="load-row">
              <input v-model="fenInput" type="text" placeholder="rnbqkbnr/pppppppp/…" />
              <button type="button" @click="loadFromFen">Load</button>
            </div>
          </label>
          <label>
            From PGN
            <textarea v-model="pgnInput" rows="3" placeholder="1. e4 e5 2. Nf3 …" />
            <button type="button" @click="loadFromPgn">Load PGN</button>
          </label>
          <p v-if="loadError" class="error" role="alert">{{ loadError }}</p>
        </details>

        <form class="form" @submit.prevent="save">
          <label>
            Name
            <input v-model="name" type="text" required maxlength="255" placeholder="e.g. Ruy Lopez" />
          </label>

          <label>
            PGN
            <textarea v-model="pgn" readonly rows="3" />
            <small>Recorded automatically; comments and arrows are saved with the game.</small>
          </label>

          <label>
            Notes (optional)
            <textarea v-model="notes" rows="4" placeholder="Overall summary, transpositions…" />
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
    </div>
  </main>
</template>

<style scoped>
.editor {
  max-width: 1100px;
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
h2 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
}
h2 small {
  font-weight: normal;
  opacity: 0.7;
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
.side-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.nav-controls {
  display: flex;
  gap: 0.35rem;
}
.nav-controls button {
  flex: 1;
}
.nav-controls .reset {
  flex: 1.4;
}
.hint {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.7;
}
.hint kbd {
  padding: 0 0.3rem;
  border: 1px solid var(--color-border);
  border-radius: 3px;
  font-family: ui-monospace, monospace;
}
.panel {
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 0.75rem;
}
.move-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.15rem 0.35rem;
  max-height: 220px;
  overflow-y: auto;
  font-family: ui-monospace, monospace;
  font-size: 0.9rem;
}
.move-list li {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
.move-number {
  opacity: 0.6;
}
.ply {
  padding: 0.1rem 0.35rem;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  color: var(--color-text);
  cursor: pointer;
  font: inherit;
}
.ply:hover {
  border-color: var(--color-border);
}
.ply.active {
  background: hsl(120, 40%, 35%);
  color: white;
}
.mark {
  font-size: 0.75rem;
}
.empty {
  margin: 0;
  opacity: 0.7;
  font-size: 0.875rem;
}
.load-row {
  display: flex;
  gap: 0.5rem;
}
.load-row input {
  flex: 1;
}
details.panel summary {
  cursor: pointer;
  font-weight: 600;
}
details.panel[open] summary {
  margin-bottom: 0.5rem;
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
