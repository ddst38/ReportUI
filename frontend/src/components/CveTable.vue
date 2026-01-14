<template>
  <div class="chart-panel">
    <h3 class="text-sm font-semibold text-gray-700 mb-3">Vulnérabilités CVE</h3>

    <!-- Filters -->
    <div class="flex flex-wrap gap-3 mb-4">
      <!-- Search -->
      <div class="flex-1 min-w-[200px]">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher (CVE, librairie, description...)"
          class="w-full px-3 py-1.5 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>

      <!-- Severity filter -->
      <select v-model="severityFilter" class="px-3 py-1.5 text-sm border border-gray-300 rounded">
        <option value="">Toutes les sévérités</option>
        <option value="CRITICAL">Critique</option>
        <option value="HIGH">Haute</option>
        <option value="MEDIUM">Moyenne</option>
        <option value="LOW">Basse</option>
      </select>

      <!-- Library filter -->
      <select v-model="libraryFilter" class="px-3 py-1.5 text-sm border border-gray-300 rounded max-w-xs">
        <option value="">Toutes les librairies</option>
        <option v-for="lib in uniqueLibraries" :key="lib" :value="lib">{{ truncateLib(lib) }}</option>
      </select>
    </div>

    <!-- Summary badges -->
    <div class="flex flex-wrap gap-2 mb-4">
      <span v-if="criticalCount > 0" class="cve-badge cve-critical">
        {{ criticalCount }} Critiques
      </span>
      <span v-if="highCount > 0" class="cve-badge cve-high">
        {{ highCount }} Hautes
      </span>
      <span v-if="mediumCount > 0" class="cve-badge cve-medium">
        {{ mediumCount }} Moyennes
      </span>
      <span v-if="lowCount > 0" class="cve-badge cve-low">
        {{ lowCount }} Basses
      </span>
    </div>

    <!-- Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="cursor-pointer" @click="sort('cveId')">
              CVE {{ sortIcon('cveId') }}
            </th>
            <th class="cursor-pointer" @click="sort('cvssScore')">
              Score {{ sortIcon('cvssScore') }}
            </th>
            <th class="cursor-pointer" @click="sort('severity')">
              Sévérité {{ sortIcon('severity') }}
            </th>
            <th class="cursor-pointer" @click="sort('libraryName')">
              Librairie {{ sortIcon('libraryName') }}
            </th>
            <th>Description</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cve in filteredCves" :key="cve.cveId" class="hover:bg-gray-50">
            <td>
              <a :href="nvdUrl(cve.cveId)" target="_blank"
                 class="text-primary-600 hover:text-primary-800 hover:underline">
                {{ cve.cveId }}
              </a>
            </td>
            <td>
              <span :class="['cve-badge', severityClass(cve.severity)]">
                {{ cve.cvssScore.toFixed(1) }}
              </span>
            </td>
            <td>
              <div class="flex items-center gap-1.5">
                <span :class="['severity-icon', severityClass(cve.severity)]" v-html="severityIcon(cve.severity)">
                </span>
                <span :class="severityTextClass(cve.severity)">
                  {{ severityLabel(cve.severity) }}
                </span>
              </div>
            </td>
            <td>
              <code class="text-xs bg-gray-100 px-1 py-0.5 rounded" :title="cve.gav">
                {{ truncateLib(cve.libraryName) }}
              </code>
            </td>
            <td class="text-xs text-gray-600 max-w-xs">
              <span :title="cve.description">{{ truncateDesc(cve.description) }}</span>
            </td>
          </tr>
          <tr v-if="filteredCves.length === 0">
            <td colspan="5" class="text-center text-gray-500 py-4">
              Aucune vulnérabilité trouvée
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="text-xs text-gray-500 mt-2">
      {{ filteredCves.length }} vulnérabilités affichées sur {{ vulnerabilities.length }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  vulnerabilities: {
    type: Array,
    default: () => []
  }
})

const searchQuery = ref('')
const severityFilter = ref('')
const libraryFilter = ref('')
const sortField = ref('cvssScore')
const sortOrder = ref('desc')

// Counts
const criticalCount = computed(() => props.vulnerabilities.filter(v => v.severity === 'CRITICAL').length)
const highCount = computed(() => props.vulnerabilities.filter(v => v.severity === 'HIGH').length)
const mediumCount = computed(() => props.vulnerabilities.filter(v => v.severity === 'MEDIUM').length)
const lowCount = computed(() => props.vulnerabilities.filter(v => v.severity === 'LOW').length)

// Unique libraries for filter
const uniqueLibraries = computed(() => {
  const libs = new Set(props.vulnerabilities.map(v => v.gav || v.libraryName))
  return Array.from(libs).sort()
})

// Filtered and sorted CVEs
const filteredCves = computed(() => {
  let result = [...props.vulnerabilities]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(cve =>
      cve.cveId?.toLowerCase().includes(query) ||
      cve.libraryName?.toLowerCase().includes(query) ||
      cve.gav?.toLowerCase().includes(query) ||
      cve.description?.toLowerCase().includes(query)
    )
  }

  // Severity filter
  if (severityFilter.value) {
    result = result.filter(cve => cve.severity === severityFilter.value)
  }

  // Library filter
  if (libraryFilter.value) {
    result = result.filter(cve => cve.gav === libraryFilter.value || cve.libraryName === libraryFilter.value)
  }

  // Sort
  result.sort((a, b) => {
    let aVal = a[sortField.value]
    let bVal = b[sortField.value]

    if (typeof aVal === 'string') {
      aVal = aVal.toLowerCase()
      bVal = bVal?.toLowerCase() || ''
    }

    if (aVal < bVal) return sortOrder.value === 'asc' ? -1 : 1
    if (aVal > bVal) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })

  return result
})

// Sorting
function sort(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = field === 'cvssScore' ? 'desc' : 'asc'
  }
}

function sortIcon(field) {
  if (sortField.value !== field) return ''
  return sortOrder.value === 'asc' ? '↑' : '↓'
}

// Helpers
function nvdUrl(cveId) {
  return `https://nvd.nist.gov/vuln/detail/${cveId}`
}

function severityLabel(severity) {
  const labels = {
    CRITICAL: 'Critique',
    HIGH: 'Haute',
    MEDIUM: 'Moyenne',
    LOW: 'Basse',
    NONE: 'Aucune'
  }
  return labels[severity] || severity
}

function severityClass(severity) {
  const classes = {
    CRITICAL: 'cve-critical',
    HIGH: 'cve-high',
    MEDIUM: 'cve-medium',
    LOW: 'cve-low',
    NONE: 'cve-none'
  }
  return classes[severity] || ''
}

function severityTextClass(severity) {
  const classes = {
    CRITICAL: 'text-gray-900 font-semibold',
    HIGH: 'text-red-700 font-semibold',
    MEDIUM: 'text-orange-600',
    LOW: 'text-yellow-700',
    NONE: 'text-green-600'
  }
  return classes[severity] || 'text-gray-600'
}

function severityIcon(severity) {
  const icons = {
    CRITICAL: `<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2A11.954 11.954 0 0110 1.944zM10 6a1 1 0 011 1v2a1 1 0 11-2 0V7a1 1 0 011-1zm0 6a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/></svg>`,
    HIGH: `<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`,
    MEDIUM: `<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`,
    LOW: `<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>`
  }
  return icons[severity] || icons.LOW
}

function truncateLib(text) {
  if (!text) return ''
  if (text.length <= 40) return text
  return text.substring(0, 37) + '...'
}

function truncateDesc(text) {
  if (!text) return ''
  if (text.length <= 100) return text
  return text.substring(0, 97) + '...'
}
</script>

<style scoped>
.cve-badge {
  @apply inline-flex items-center px-2 py-0.5 rounded text-xs font-medium;
}

.cve-critical {
  @apply bg-gray-900 text-white;
}

.cve-high {
  @apply bg-red-600 text-white;
}

.cve-medium {
  @apply bg-orange-500 text-white;
}

.cve-low {
  @apply bg-yellow-400 text-gray-900;
}

.cve-none {
  @apply bg-green-500 text-white;
}

.severity-icon {
  @apply flex-shrink-0;
}

.severity-icon.cve-critical {
  @apply text-gray-900;
}

.severity-icon.cve-high {
  @apply text-red-600;
}

.severity-icon.cve-medium {
  @apply text-orange-500;
}

.severity-icon.cve-low {
  @apply text-yellow-500;
}
</style>
