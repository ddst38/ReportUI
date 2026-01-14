<template>
  <div class="space-y-4">
    <!-- En-tête avec infos repository -->
    <div class="chart-panel">
      <div class="flex items-center gap-3 mb-4">
        <div class="p-2 rounded-lg" :class="repoTypeClass">
          <span v-html="repoIcon"></span>
        </div>
        <div>
          <h3 class="text-lg font-semibold text-gray-900">
            Librairies déployées sur {{ deploymentInfo.repositoryType }}
          </h3>
          <p class="text-sm text-gray-600">
            Repository : <code class="bg-gray-100 px-1.5 py-0.5 rounded">{{ deploymentInfo.repositoryName }}</code>
          </p>
        </div>
      </div>

      <!-- Statistiques -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
        <div class="bg-gray-50 rounded-lg p-3 text-center">
          <div class="text-2xl font-bold text-gray-900">{{ deploymentInfo.deployedCount }}</div>
          <div class="text-xs text-gray-500">Total déployées</div>
        </div>
        <div class="bg-blue-50 rounded-lg p-3 text-center">
          <div class="text-2xl font-bold text-blue-600">{{ internalCount }}</div>
          <div class="text-xs text-blue-600">Internes</div>
        </div>
        <div class="bg-purple-50 rounded-lg p-3 text-center">
          <div class="text-2xl font-bold text-purple-600">{{ externalCount }}</div>
          <div class="text-xs text-purple-600">Externes</div>
        </div>
        <div class="bg-green-50 rounded-lg p-3 text-center">
          <div class="text-2xl font-bold text-green-600">{{ providedCount }}</div>
          <div class="text-xs text-green-600">Provided</div>
        </div>
      </div>

      <!-- Lien vers le repository -->
      <div class="text-sm">
        <a :href="deploymentInfo.repositoryUrl" target="_blank"
           class="text-primary-600 hover:text-primary-800 hover:underline inline-flex items-center gap-1">
          <span v-html="externalLinkIcon"></span>
          {{ deploymentInfo.repositoryUrl }}
        </a>
      </div>
    </div>

    <!-- Filtres -->
    <div class="chart-panel">
      <div class="flex flex-wrap gap-3 mb-4">
        <div class="flex-1 min-w-[200px]">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher (groupId, artifactId, JAR...)"
            class="w-full px-3 py-1.5 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-primary-500 focus:border-primary-500"
          />
        </div>
        <select v-model="typeFilter" class="px-3 py-1.5 text-sm border border-gray-300 rounded">
          <option value="">Tous les types</option>
          <option value="INTERNAL">Internes</option>
          <option value="EXTERNAL">Externes</option>
          <option value="PROVIDED">Provided</option>
        </select>
      </div>

      <!-- Table des librairies -->
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th class="cursor-pointer" @click="sort('groupId')">
                GroupId {{ sortIcon('groupId') }}
              </th>
              <th class="cursor-pointer" @click="sort('artifactId')">
                ArtifactId {{ sortIcon('artifactId') }}
              </th>
              <th class="cursor-pointer" @click="sort('version')">
                Version {{ sortIcon('version') }}
              </th>
              <th class="cursor-pointer" @click="sort('type')">
                Type {{ sortIcon('type') }}
              </th>
              <th>JAR Original</th>
              <th class="cursor-pointer text-right" @click="sort('size')">
                Taille {{ sortIcon('size') }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="lib in filteredLibraries" :key="lib.gav" class="hover:bg-gray-50">
              <td>
                <code class="text-xs">{{ lib.groupId }}</code>
              </td>
              <td>
                <code class="text-xs font-medium text-gray-900">{{ lib.artifactId }}</code>
              </td>
              <td>
                <span class="text-xs" :class="versionClass(lib.version)">
                  {{ truncateVersion(lib.version) }}
                </span>
              </td>
              <td>
                <span :class="typeClass(lib.type)" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium">
                  {{ typeLabel(lib.type) }}
                </span>
              </td>
              <td>
                <span class="text-xs text-gray-600" :title="lib.originalJar">
                  {{ truncate(lib.originalJar, 30) }}
                </span>
              </td>
              <td class="text-right text-xs text-gray-600">
                {{ formatSize(lib.size) }}
              </td>
            </tr>
            <tr v-if="filteredLibraries.length === 0">
              <td colspan="6" class="text-center text-gray-500 py-4">
                Aucune librairie trouvée
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="text-xs text-gray-500 mt-2">
        {{ filteredLibraries.length }} librairies affichées sur {{ deploymentInfo.deployedLibraries?.length || 0 }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  deploymentInfo: {
    type: Object,
    required: true
  }
})

const searchQuery = ref('')
const typeFilter = ref('')
const sortField = ref('groupId')
const sortOrder = ref('asc')

// Compteurs par type
const internalCount = computed(() =>
  props.deploymentInfo.deployedLibraries?.filter(l => l.type === 'INTERNAL').length || 0
)
const externalCount = computed(() =>
  props.deploymentInfo.deployedLibraries?.filter(l => l.type === 'EXTERNAL').length || 0
)
const providedCount = computed(() =>
  props.deploymentInfo.deployedLibraries?.filter(l => l.type === 'PROVIDED').length || 0
)

// Classe selon le type de repository
const repoTypeClass = computed(() => {
  return props.deploymentInfo.repositoryType === 'NEXUS'
    ? 'bg-green-100 text-green-700'
    : 'bg-orange-100 text-orange-700'
})

// Filtrage et tri
const filteredLibraries = computed(() => {
  let result = [...(props.deploymentInfo.deployedLibraries || [])]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(lib =>
      lib.groupId?.toLowerCase().includes(query) ||
      lib.artifactId?.toLowerCase().includes(query) ||
      lib.originalJar?.toLowerCase().includes(query) ||
      lib.gav?.toLowerCase().includes(query)
    )
  }

  if (typeFilter.value) {
    result = result.filter(lib => lib.type === typeFilter.value)
  }

  result.sort((a, b) => {
    let aVal = a[sortField.value]
    let bVal = b[sortField.value]

    if (typeof aVal === 'string') {
      aVal = aVal?.toLowerCase() || ''
      bVal = bVal?.toLowerCase() || ''
    }

    if (aVal < bVal) return sortOrder.value === 'asc' ? -1 : 1
    if (aVal > bVal) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })

  return result
})

function sort(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'asc'
  }
}

function sortIcon(field) {
  if (sortField.value !== field) return ''
  return sortOrder.value === 'asc' ? '↑' : '↓'
}

function typeClass(type) {
  const classes = {
    INTERNAL: 'bg-blue-100 text-blue-800',
    EXTERNAL: 'bg-purple-100 text-purple-800',
    PROVIDED: 'bg-green-100 text-green-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

function typeLabel(type) {
  const labels = {
    INTERNAL: 'Interne',
    EXTERNAL: 'Externe',
    PROVIDED: 'Provided'
  }
  return labels[type] || type
}

function versionClass(version) {
  if (version?.startsWith('SHA-')) {
    return 'text-orange-600 font-mono'
  }
  return 'text-gray-700'
}

function truncateVersion(version) {
  if (!version) return ''
  if (version.startsWith('SHA-') && version.length > 15) {
    return version.substring(0, 15) + '...'
  }
  return version
}

function truncate(text, maxLen) {
  if (!text) return ''
  if (text.length <= maxLen) return text
  return text.substring(0, maxLen - 3) + '...'
}

function formatSize(bytes) {
  if (!bytes || bytes === 0) return '-'
  const kb = bytes / 1024
  if (kb < 1024) return kb.toFixed(1) + ' KB'
  const mb = kb / 1024
  return mb.toFixed(2) + ' MB'
}

// Icônes SVG
const repoIcon = `<svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M5 4v3H4a2 2 0 00-2 2v3a2 2 0 002 2h1v3a2 2 0 002 2h6a2 2 0 002-2v-3h1a2 2 0 002-2V9a2 2 0 00-2-2h-1V4a2 2 0 00-2-2H7a2 2 0 00-2 2zm2 0v3h6V4H7zm6 9v4H7v-4h6z" clip-rule="evenodd"/></svg>`

const externalLinkIcon = `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/></svg>`
</script>
