<template>
  <div class="chart-panel">
    <div class="chart-title">
      <span>Bibliothèques ({{ filteredLibraries.length }})</span>
      <div class="flex items-center space-x-2">
        <!-- Search -->
        <div class="relative">
          <input v-model="search" type="text" placeholder="Rechercher..."
                 class="text-xs border border-gray-300 rounded px-2 py-1 w-32 focus:outline-none focus:border-primary-500">
        </div>
        <!-- Status filter -->
        <select v-model="statusFilter" class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:border-primary-500">
          <option value="">Tous</option>
          <option value="RESOLVED">Résolus</option>
          <option value="LOCAL">Local</option>
          <option value="UNRESOLVED">Non résolus</option>
        </select>
        <!-- Scope filter -->
        <select v-model="scopeFilter" class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:border-primary-500">
          <option value="">Tous scopes</option>
          <option value="COMPILE">Compile</option>
          <option value="PROVIDED">Provided</option>
          <option value="RUNTIME">Runtime</option>
          <option value="TEST">Test</option>
        </select>
      </div>
    </div>

    <div class="table-container max-h-96 overflow-y-auto">
      <table class="data-table">
        <thead class="sticky top-0">
          <tr>
            <th class="cursor-pointer" @click="sort('originalName')">
              Fichier
              <span v-if="sortKey === 'originalName'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th class="cursor-pointer" @click="sort('groupId')">
              GroupId
              <span v-if="sortKey === 'groupId'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>ArtifactId:Version</th>
            <th class="cursor-pointer" @click="sort('scope')">
              Scope
              <span v-if="sortKey === 'scope'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Méthode</th>
            <th class="cursor-pointer" @click="sort('size')">
              Taille
              <span v-if="sortKey === 'size'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Statut</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="lib in paginatedLibraries" :key="lib.originalName + lib.sha1">
            <td class="font-mono">
              <span class="truncate max-w-xs block" :title="lib.originalName">
                {{ truncate(lib.cleanedName || lib.originalName, 30) }}
              </span>
            </td>
            <td class="font-mono text-gray-600">
              <span class="truncate max-w-xs block" :title="lib.groupId">
                {{ truncate(lib.groupId, 25) }}
              </span>
            </td>
            <td class="font-mono">
              <span :title="lib.artifactId + ':' + lib.version">
                {{ truncate(lib.artifactId, 15) }}:{{ truncate(lib.version, 10) }}
              </span>
            </td>
            <td>
              <span :class="scopeBadgeClass(lib.scope)">{{ lib.scope }}</span>
            </td>
            <td class="text-gray-600">{{ methodLabel(lib.resolutionMethod) }}</td>
            <td class="text-right text-gray-500">{{ formatSize(lib.size) }}</td>
            <td>
              <span :class="statusBadgeClass(lib.status)">{{ statusLabel(lib.status) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-between mt-3 text-xs text-gray-600">
      <div>
        Page {{ currentPage }} / {{ totalPages }}
      </div>
      <div class="flex space-x-1">
        <button @click="currentPage = 1" :disabled="currentPage === 1"
                class="px-2 py-1 border rounded disabled:opacity-50 hover:bg-gray-100">
          «
        </button>
        <button @click="currentPage--" :disabled="currentPage === 1"
                class="px-2 py-1 border rounded disabled:opacity-50 hover:bg-gray-100">
          ‹
        </button>
        <button @click="currentPage++" :disabled="currentPage === totalPages"
                class="px-2 py-1 border rounded disabled:opacity-50 hover:bg-gray-100">
          ›
        </button>
        <button @click="currentPage = totalPages" :disabled="currentPage === totalPages"
                class="px-2 py-1 border rounded disabled:opacity-50 hover:bg-gray-100">
          »
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  libraries: {
    type: Array,
    required: true
  }
})

const search = ref('')
const statusFilter = ref('')
const scopeFilter = ref('')
const sortKey = ref('originalName')
const sortOrder = ref('asc')
const currentPage = ref(1)
const pageSize = 50

// Reset page when filters change
watch([search, statusFilter, scopeFilter], () => {
  currentPage.value = 1
})

const filteredLibraries = computed(() => {
  let result = [...props.libraries]

  // Text search
  if (search.value) {
    const q = search.value.toLowerCase()
    result = result.filter(lib =>
      lib.originalName?.toLowerCase().includes(q) ||
      lib.cleanedName?.toLowerCase().includes(q) ||
      lib.groupId?.toLowerCase().includes(q) ||
      lib.artifactId?.toLowerCase().includes(q)
    )
  }

  // Status filter
  if (statusFilter.value) {
    result = result.filter(lib => lib.status === statusFilter.value)
  }

  // Scope filter
  if (scopeFilter.value) {
    result = result.filter(lib => lib.scope === scopeFilter.value)
  }

  // Sort
  result.sort((a, b) => {
    const aVal = a[sortKey.value] ?? ''
    const bVal = b[sortKey.value] ?? ''
    const cmp = typeof aVal === 'number' ? aVal - bVal : String(aVal).localeCompare(String(bVal))
    return sortOrder.value === 'asc' ? cmp : -cmp
  })

  return result
})

const totalPages = computed(() => Math.ceil(filteredLibraries.value.length / pageSize))

const paginatedLibraries = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredLibraries.value.slice(start, start + pageSize)
})

function sort(key) {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
}

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function statusBadgeClass(status) {
  switch (status) {
    case 'RESOLVED': return 'badge badge-success'
    case 'LOCAL': return 'badge badge-warning'
    case 'UNRESOLVED': return 'badge badge-error'
    default: return 'badge badge-info'
  }
}

function statusLabel(status) {
  switch (status) {
    case 'RESOLVED': return 'Résolu'
    case 'LOCAL': return 'Local'
    case 'UNRESOLVED': return 'Non résolu'
    default: return status
  }
}

function scopeBadgeClass(scope) {
  switch (scope) {
    case 'COMPILE': return 'badge badge-info'
    case 'PROVIDED': return 'badge bg-purple-100 text-purple-800'
    case 'TEST': return 'badge bg-gray-100 text-gray-800'
    case 'RUNTIME': return 'badge bg-orange-100 text-orange-800'
    default: return 'badge'
  }
}

function methodLabel(method) {
  const labels = {
    'INTERNAL_PATTERN': 'Interne',
    'KNOWN_CONFIG': 'Connu',
    'ARTIFACTORY_CHECKSUM': 'Artif. SHA',
    'ARTIFACTORY': 'Artifactory',
    'CHECKSUM': 'Central SHA',
    'MANIFEST': 'Manifest',
    'PATTERN': 'Pattern',
    'PACKAGE_ANALYSIS': 'Packages',
    'UNRESOLVED': 'Non résolu',
    'AUTO_FIX': 'Auto-fix'
  }
  return labels[method] || method
}
</script>
