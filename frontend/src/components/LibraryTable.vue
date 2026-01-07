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
        <!-- Type filter -->
        <select v-model="typeFilter" class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:border-primary-500">
          <option value="">Tous types</option>
          <option value="internal">Interne</option>
          <option value="external">Externe</option>
        </select>
      </div>
    </div>

    <div class="table-container max-h-[500px] overflow-y-auto">
      <table class="data-table">
        <thead class="sticky top-0 bg-white">
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
            <th>Identifiant</th>
            <th>Type</th>
            <th class="cursor-pointer" @click="sort('size')">
              Taille
              <span v-if="sortKey === 'size'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Statut</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="lib in filteredLibraries" :key="lib.originalName + lib.sha1">
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
              <span v-if="lib.isAutoFixProvided" class="badge bg-violet-100 text-violet-800 ml-1">auto-fix</span>
            </td>
            <td>
              <span v-if="lib.identificationSource" :class="identifierBadgeClass(lib.identificationSource)">
                {{ identifierLabel(lib.identificationSource) }}
              </span>
              <span v-else class="text-gray-400">-</span>
            </td>
            <td>
              <span :class="typeBadgeClass(lib.libraryType, lib.status)">
                {{ lib.libraryType === 'internal' ? 'Interne' : 'Externe' }}
              </span>
            </td>
            <td class="text-right text-gray-500">{{ formatSize(lib.size) }}</td>
            <td>
              <span :class="statusBadgeClass(lib.status)">{{ statusLabel(lib.status) }}</span>
              <span v-if="lib.status === 'UNRESOLVED' && lib.isLocalInstall" class="badge bg-gray-100 text-gray-600 ml-1">local</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  libraries: {
    type: Array,
    required: true
  }
})

const search = ref('')
const statusFilter = ref('')
const scopeFilter = ref('')
const typeFilter = ref('')
const sortKey = ref('originalName')
const sortOrder = ref('asc')

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

  // Type filter
  if (typeFilter.value) {
    result = result.filter(lib => lib.libraryType === typeFilter.value)
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
    case 'UNRESOLVED': return 'badge badge-error'
    default: return 'badge badge-info'
  }
}

function statusLabel(status) {
  switch (status) {
    case 'RESOLVED': return 'Résolu'
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

function identifierBadgeClass(source) {
  switch (source) {
    case 'ARTIFACTORY': return 'badge badge-info'
    case 'MAVEN_CENTRAL': return 'badge badge-success'
    case 'CACHE': return 'badge bg-gray-100 text-gray-600'
    default: return 'badge'
  }
}

function identifierLabel(source) {
  switch (source) {
    case 'ARTIFACTORY': return 'Artifactory'
    case 'MAVEN_CENTRAL': return 'Maven Central'
    case 'CACHE': return 'Cache'
    default: return source
  }
}

function typeBadgeClass(type, status) {
  if (type === 'internal') {
    return 'badge badge-info'
  }
  // External non résolu = rouge clair
  if (status === 'UNRESOLVED') {
    return 'badge bg-red-100 text-red-800'
  }
  return 'badge bg-gray-100 text-gray-600'
}
</script>
