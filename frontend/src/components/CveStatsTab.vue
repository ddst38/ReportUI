<template>
  <div>
    <!-- Summary cards -->
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
      <div class="cve-stat-card cve-critical" v-if="summary.criticalCount > 0">
        <div class="cve-stat-value">{{ summary.criticalCount }}</div>
        <div class="cve-stat-label">Critiques</div>
      </div>
      <div class="cve-stat-card cve-high" v-if="summary.highCount > 0">
        <div class="cve-stat-value">{{ summary.highCount }}</div>
        <div class="cve-stat-label">Hautes</div>
      </div>
      <div class="cve-stat-card cve-medium" v-if="summary.mediumCount > 0">
        <div class="cve-stat-value">{{ summary.mediumCount }}</div>
        <div class="cve-stat-label">Moyennes</div>
      </div>
      <div class="cve-stat-card cve-low" v-if="summary.lowCount > 0">
        <div class="cve-stat-value">{{ summary.lowCount }}</div>
        <div class="cve-stat-label">Basses</div>
      </div>
      <div class="stat-card">
        <div class="stat-value text-primary-600">{{ summary.riskScore || 0 }}</div>
        <div class="stat-label">Score de risque</div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="grid md:grid-cols-2 gap-6 mb-6">
      <!-- Donut: Distribution by severity -->
      <DonutChart
        title="Distribution par sévérité"
        :labels="severityLabels"
        :data="severityData"
        :colors="severityColors"
      />

      <!-- Bar: Top vulnerable libraries -->
      <div class="chart-panel">
        <h3 class="text-sm font-semibold text-gray-700 mb-3">Top 10 librairies vulnérables</h3>
        <div class="space-y-2">
          <div v-for="(item, index) in topLibraries" :key="item.gav"
               class="flex items-center gap-2">
            <span class="text-xs text-gray-500 w-4">{{ index + 1 }}.</span>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <code class="text-xs bg-gray-100 px-1 py-0.5 rounded truncate" :title="item.gav">
                  {{ truncateGav(item.gav) }}
                </code>
                <span :class="['cve-badge-sm', severityClass(item.maxSeverity)]">
                  {{ item.count }}
                </span>
              </div>
              <div class="h-1.5 bg-gray-200 rounded-full mt-1">
                <div class="h-1.5 rounded-full" :class="severityBgClass(item.maxSeverity)"
                     :style="{ width: (item.count / maxCveCount * 100) + '%' }"></div>
              </div>
            </div>
          </div>
          <div v-if="topLibraries.length === 0" class="text-center text-gray-500 py-4 text-sm">
            Aucune librairie vulnérable
          </div>
        </div>
      </div>
    </div>

    <!-- Risk assessment -->
    <div class="chart-panel">
      <h3 class="text-sm font-semibold text-gray-700 mb-3">Évaluation du risque</h3>
      <div class="grid md:grid-cols-3 gap-4">
        <div class="bg-gray-50 rounded-lg p-4 text-center">
          <div class="text-2xl font-bold" :class="riskLevelColor">
            {{ riskLevel }}
          </div>
          <div class="text-sm text-gray-600">Niveau de risque</div>
        </div>
        <div class="bg-gray-50 rounded-lg p-4 text-center">
          <div class="text-2xl font-bold text-gray-700">
            {{ summary.affectedLibraries || 0 }}
          </div>
          <div class="text-sm text-gray-600">Librairies affectées</div>
        </div>
        <div class="bg-gray-50 rounded-lg p-4 text-center">
          <div class="text-2xl font-bold text-gray-700">
            {{ summary.totalVulnerabilities || 0 }}
          </div>
          <div class="text-sm text-gray-600">Vulnérabilités totales</div>
        </div>
      </div>

      <!-- Recommendations -->
      <div v-if="summary.criticalCount > 0" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
        <div class="flex items-start gap-2">
          <svg class="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2A11.954 11.954 0 0110 1.944z" clip-rule="evenodd"/>
          </svg>
          <div>
            <div class="font-semibold text-red-800">Vulnérabilités critiques détectées</div>
            <div class="text-sm text-red-700 mt-1">
              {{ summary.criticalCount }} vulnérabilité(s) critique(s) nécessitent une action immédiate.
              Mettez à jour les librairies concernées avant la mise en production.
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="summary.highCount > 0" class="mt-4 p-3 bg-orange-50 border border-orange-200 rounded-lg">
        <div class="flex items-start gap-2">
          <svg class="w-5 h-5 text-orange-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92z" clip-rule="evenodd"/>
          </svg>
          <div>
            <div class="font-semibold text-orange-800">Vulnérabilités hautes détectées</div>
            <div class="text-sm text-orange-700 mt-1">
              {{ summary.highCount }} vulnérabilité(s) haute(s) devraient être corrigées rapidement.
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="summary.totalVulnerabilities === 0" class="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg">
        <div class="flex items-start gap-2">
          <svg class="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
          <div>
            <div class="font-semibold text-green-800">Aucune vulnérabilité détectée</div>
            <div class="text-sm text-green-700 mt-1">
              Le projet n'utilise pas de librairies avec des failles de sécurité connues.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DonutChart from '@/components/charts/DonutChart.vue'

const props = defineProps({
  summary: {
    type: Object,
    default: () => ({})
  },
  vulnerabilities: {
    type: Array,
    default: () => []
  }
})

// Severity chart data
const severityLabels = computed(() => {
  const labels = []
  if (props.summary.criticalCount > 0) labels.push('Critiques')
  if (props.summary.highCount > 0) labels.push('Hautes')
  if (props.summary.mediumCount > 0) labels.push('Moyennes')
  if (props.summary.lowCount > 0) labels.push('Basses')
  return labels
})

const severityData = computed(() => {
  const data = []
  if (props.summary.criticalCount > 0) data.push(props.summary.criticalCount)
  if (props.summary.highCount > 0) data.push(props.summary.highCount)
  if (props.summary.mediumCount > 0) data.push(props.summary.mediumCount)
  if (props.summary.lowCount > 0) data.push(props.summary.lowCount)
  return data
})

const severityColors = computed(() => {
  const colors = []
  if (props.summary.criticalCount > 0) colors.push('#1a1a1a')
  if (props.summary.highCount > 0) colors.push('#dc2626')
  if (props.summary.mediumCount > 0) colors.push('#f97316')
  if (props.summary.lowCount > 0) colors.push('#eab308')
  return colors
})

// Top libraries
const topLibraries = computed(() => {
  const byLib = {}
  for (const cve of props.vulnerabilities) {
    const gav = cve.gav || cve.libraryName
    if (!byLib[gav]) {
      byLib[gav] = { gav, count: 0, maxSeverity: 'LOW', maxScore: 0 }
    }
    byLib[gav].count++
    if (cve.cvssScore > byLib[gav].maxScore) {
      byLib[gav].maxScore = cve.cvssScore
      byLib[gav].maxSeverity = cve.severity
    }
  }

  return Object.values(byLib)
    .sort((a, b) => {
      // Sort by max severity first, then by count
      const severityOrder = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }
      const sevDiff = (severityOrder[a.maxSeverity] || 4) - (severityOrder[b.maxSeverity] || 4)
      if (sevDiff !== 0) return sevDiff
      return b.count - a.count
    })
    .slice(0, 10)
})

const maxCveCount = computed(() => {
  return Math.max(...topLibraries.value.map(l => l.count), 1)
})

// Risk level
const riskLevel = computed(() => {
  const score = props.summary.riskScore || 0
  if (props.summary.criticalCount > 0) return 'CRITIQUE'
  if (score >= 50) return 'ÉLEVÉ'
  if (score >= 20) return 'MOYEN'
  if (score > 0) return 'FAIBLE'
  return 'AUCUN'
})

const riskLevelColor = computed(() => {
  const level = riskLevel.value
  const colors = {
    'CRITIQUE': 'text-gray-900',
    'ÉLEVÉ': 'text-red-600',
    'MOYEN': 'text-orange-500',
    'FAIBLE': 'text-yellow-500',
    'AUCUN': 'text-green-500'
  }
  return colors[level] || 'text-gray-600'
})

// Helpers
function truncateGav(gav) {
  if (!gav) return ''
  if (gav.length <= 50) return gav
  return gav.substring(0, 47) + '...'
}

function severityClass(severity) {
  const classes = {
    CRITICAL: 'cve-critical',
    HIGH: 'cve-high',
    MEDIUM: 'cve-medium',
    LOW: 'cve-low'
  }
  return classes[severity] || ''
}

function severityBgClass(severity) {
  const classes = {
    CRITICAL: 'bg-gray-900',
    HIGH: 'bg-red-600',
    MEDIUM: 'bg-orange-500',
    LOW: 'bg-yellow-400'
  }
  return classes[severity] || 'bg-gray-400'
}
</script>

<style scoped>
.cve-stat-card {
  @apply rounded-lg p-4 text-center;
}

.cve-stat-value {
  @apply text-2xl font-bold;
}

.cve-stat-label {
  @apply text-sm opacity-80;
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

.cve-badge-sm {
  @apply inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium;
}
</style>
