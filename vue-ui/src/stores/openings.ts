import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { OpeningRequest, OpeningResponse } from '@/lib/api'
import {
  createOpening,
  deleteOpening,
  getOpening,
  listOpenings,
  updateOpening,
} from '@/lib/api'

export const useOpeningsStore = defineStore('openings', () => {
  const items = ref<OpeningResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      items.value = await listOpenings()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load openings'
    } finally {
      loading.value = false
    }
  }

  async function fetchOne(id: number): Promise<OpeningResponse> {
    return await getOpening(id)
  }

  async function create(req: OpeningRequest): Promise<OpeningResponse> {
    const created = await createOpening(req)
    items.value = [created, ...items.value]
    return created
  }

  async function update(id: number, req: OpeningRequest): Promise<OpeningResponse> {
    const updated = await updateOpening(id, req)
    items.value = items.value.map((o) => (o.id === id ? updated : o))
    return updated
  }

  async function remove(id: number) {
    await deleteOpening(id)
    items.value = items.value.filter((o) => o.id !== id)
  }

  return { items, loading, error, fetchAll, fetchOne, create, update, remove }
})
