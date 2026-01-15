<template>
  <div class="chart-panel">
    <div class="chart-title">
      <span>Bibliothèques détectées dans le projet ({{ jars.length }})</span>
      <div class="flex items-center space-x-2">
        <!-- Search -->
        <input v-model="search" type="text" placeholder="Rechercher..."
               class="text-xs border border-gray-300 rounded px-2 py-1 w-32 focus:outline-none focus:border-primary-500">
        <!-- Source filter -->
        <select v-model="sourceFilter" class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:border-primary-500">
          <option value="">Toutes sources</option>
          <option value="cadre">Cadre</option>
          <option value="ear">Extrait EAR</option>
          <option value="lib">Répertoire lib</option>
        </select>
        <!-- Category filter -->
        <select v-model="categoryFilter" class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:border-primary-500">
          <option value="">Toutes catégories</option>
          <option value="MAIN">Principal</option>
          <option value="TEST">Test</option>
          <option value="PROVIDED">Fourni</option>
          <option value="RUNTIME">Exécution</option>
        </select>
      </div>
    </div>

    <p class="text-xs text-gray-500 mb-3">
      Liste complète des fichiers JAR trouvés dans le projet source, avant tout traitement de résolution.
    </p>

    <div class="table-container max-h-[500px] overflow-y-auto">
      <table class="data-table">
        <thead class="sticky top-0 bg-white">
          <tr>
            <th class="w-12">#</th>
            <th class="cursor-pointer" @click="sort('name')">
              Nom du fichier
              <span v-if="sortKey === 'name'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th class="cursor-pointer" @click="sort('size')">
              Taille
              <span v-if="sortKey === 'size'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Source</th>
            <th>Catégorie</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(jar, idx) in filteredJars" :key="jar.name + idx">
            <td class="text-gray-400 text-sm">{{ idx + 1 }}</td>
            <td class="font-mono">
              <span class="truncate max-w-md block" :title="jar.name">
                {{ jar.name }}
              </span>
            </td>
            <td class="text-right text-gray-500">{{ formatSize(jar.size) }}</td>
            <td class="text-sm text-gray-600">
              <span v-if="jar.cadreTag" class="flex items-center gap-1">
                <span class="badge bg-indigo-100 text-indigo-800">Cadre</span>
                <span class="font-mono text-xs text-indigo-600">{{ jar.cadreTag }}</span>
              </span>
              <span v-else-if="jar.source?.startsWith('Extrait')" class="italic">{{ jar.source }}</span>
              <span v-else>{{ jar.source }}</span>
            </td>
            <td>
              <span :class="categoryBadgeClass(jar.category)">{{ categoryLabel(jar.category) }}</span>
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
  jars: {
    type: Array,
    default: () => []
  }
})

const search = ref('')
const sourceFilter = ref('')
const categoryFilter = ref('')
const sortKey = ref('name')
const sortOrder = ref('asc')

const filteredJars = computed(() => {
  let result = [...props.jars]

  // Text search
  if (search.value) {
    const q = search.value.toLowerCase()
    result = result.filter(jar => jar.name?.toLowerCase().includes(q))
  }

  // Source filter
  if (sourceFilter.value) {
    result = result.filter(jar => {
      if (sourceFilter.value === 'cadre') return jar.cadreTag != null
      if (sourceFilter.value === 'ear') return jar.source?.startsWith('Extrait')
      if (sourceFilter.value === 'lib') return !jar.cadreTag && !jar.source?.startsWith('Extrait')
      return true
    })
  }

  // Category filter
  if (categoryFilter.value) {
    result = result.filter(jar => jar.category === categoryFilter.value)
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

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function categoryBadgeClass(category) {
  switch (category) {
    case 'MAIN': return 'badge badge-success'
    case 'TEST': return 'badge badge-warning'
    case 'PROVIDED': return 'badge bg-purple-100 text-purple-800'
    case 'RUNTIME': return 'badge bg-orange-100 text-orange-800'
    default: return 'badge bg-gray-100 text-gray-600'
  }
}

function categoryLabel(category) {
  switch (category) {
    case 'MAIN': return 'Principal'
    case 'TEST': return 'Test'
    case 'PROVIDED': return 'Fourni'
    case 'RUNTIME': return 'Exécution'
    default: return category || 'Inconnu'
  }
}
</script>
