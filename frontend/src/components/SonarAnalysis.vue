<template>
  <div class="space-y-6">
    <!-- Quality Gate Status -->
    <div v-if="analysis" class="bg-white rounded-lg shadow p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">Analyse SonarQube</h3>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-500">{{ analysis.projectKey }}</span>
          <span v-if="analysis.analysisDate" class="text-xs text-gray-400">
            {{ formatDate(analysis.analysisDate) }}
          </span>
        </div>
      </div>

      <!-- Quality Gate Badge -->
      <div class="flex items-center gap-4 mb-6">
        <div class="px-4 py-2 rounded-lg text-lg font-bold"
             :class="qualityGateClass">
          Quality Gate: {{ analysis.qualityGateStatus || 'N/A' }}
        </div>
        <span v-if="analysis.criticalIssuesCount > 0" class="text-red-600 font-semibold">
          {{ analysis.criticalIssuesCount }} issues bloquantes/critiques
        </span>
      </div>

      <!-- Ratings -->
      <div class="grid grid-cols-3 md:grid-cols-5 gap-4 mb-6">
        <div class="text-center">
          <div class="text-3xl font-bold" :class="ratingClass(analysis.metrics?.maintainabilityRating)">
            {{ analysis.metrics?.maintainabilityRating || '-' }}
          </div>
          <div class="text-xs text-gray-500">Maintenabilite</div>
        </div>
        <div class="text-center">
          <div class="text-3xl font-bold" :class="ratingClass(analysis.metrics?.reliabilityRating)">
            {{ analysis.metrics?.reliabilityRating || '-' }}
          </div>
          <div class="text-xs text-gray-500">Fiabilite</div>
        </div>
        <div class="text-center">
          <div class="text-3xl font-bold" :class="ratingClass(analysis.metrics?.securityRating)">
            {{ analysis.metrics?.securityRating || '-' }}
          </div>
          <div class="text-xs text-gray-500">Securite</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-gray-700">
            {{ analysis.metrics?.coverage?.toFixed(1) || 0 }}%
          </div>
          <div class="text-xs text-gray-500">Couverture</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-gray-700">
            {{ analysis.metrics?.duplications?.toFixed(1) || 0 }}%
          </div>
          <div class="text-xs text-gray-500">Duplications</div>
        </div>
      </div>
    </div>

    <!-- Technical Debt -->
    <div v-if="analysis?.metrics" class="bg-white rounded-lg shadow p-4">
      <h3 class="text-lg font-semibold mb-4">Dette technique</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="text-center p-3 bg-orange-50 rounded-lg">
          <div class="text-2xl font-bold text-orange-600">
            {{ analysis.metrics.technicalDebtFormatted || formatDebt(analysis.metrics.technicalDebt) }}
          </div>
          <div class="text-xs text-gray-500">Dette totale</div>
        </div>
        <div class="text-center p-3 bg-blue-50 rounded-lg">
          <div class="text-2xl font-bold text-blue-600">
            {{ analysis.metrics.debtRatio?.toFixed(1) || 0 }}%
          </div>
          <div class="text-xs text-gray-500">Ratio de dette</div>
        </div>
        <div class="text-center p-3 bg-purple-50 rounded-lg">
          <div class="text-2xl font-bold text-purple-600">
            {{ formatDebt(analysis.metrics.newTechnicalDebt) }}
          </div>
          <div class="text-xs text-gray-500">Nouveau code</div>
        </div>
        <div class="text-center p-3 bg-gray-50 rounded-lg">
          <div class="text-2xl font-bold text-gray-600">
            {{ analysis.metrics.linesOfCode?.toLocaleString() || 0 }}
          </div>
          <div class="text-xs text-gray-500">Lignes de code</div>
        </div>
      </div>
    </div>

    <!-- Issues Summary -->
    <div class="grid md:grid-cols-2 gap-4">
      <!-- By Type -->
      <div class="bg-white rounded-lg shadow p-4">
        <h3 class="text-lg font-semibold mb-4">Issues par type</h3>
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <span class="flex items-center gap-2">
              <span class="w-3 h-3 bg-red-500 rounded-full"></span>
              Bugs
            </span>
            <span class="font-bold" :class="analysis?.metrics?.bugs > 0 ? 'text-red-600' : 'text-green-600'">
              {{ analysis?.metrics?.bugs || 0 }}
            </span>
          </div>
          <div class="flex items-center justify-between">
            <span class="flex items-center gap-2">
              <span class="w-3 h-3 bg-orange-500 rounded-full"></span>
              Vulnerabilites
            </span>
            <span class="font-bold" :class="analysis?.metrics?.vulnerabilities > 0 ? 'text-orange-600' : 'text-green-600'">
              {{ analysis?.metrics?.vulnerabilities || 0 }}
            </span>
          </div>
          <div class="flex items-center justify-between">
            <span class="flex items-center gap-2">
              <span class="w-3 h-3 bg-yellow-500 rounded-full"></span>
              Code Smells
            </span>
            <span class="font-bold text-yellow-600">
              {{ analysis?.metrics?.codeSmells || 0 }}
            </span>
          </div>
          <div class="flex items-center justify-between">
            <span class="flex items-center gap-2">
              <span class="w-3 h-3 bg-purple-500 rounded-full"></span>
              Security Hotspots
            </span>
            <span class="font-bold text-purple-600">
              {{ analysis?.metrics?.securityHotspots || 0 }}
            </span>
          </div>
        </div>
      </div>

      <!-- By Severity -->
      <div class="bg-white rounded-lg shadow p-4">
        <h3 class="text-lg font-semibold mb-4">Issues par severite</h3>
        <div class="space-y-2">
          <div v-for="severity in severities" :key="severity.key"
               class="flex items-center gap-2">
            <span class="w-20 text-sm">{{ severity.label }}</span>
            <div class="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
              <div :class="severity.bgClass"
                   :style="{ width: getSeverityWidth(severity.key) + '%' }"
                   class="h-full transition-all duration-300">
              </div>
            </div>
            <span class="w-10 text-right text-sm font-mono">
              {{ analysis?.issuesBySeverity?.[severity.key] || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Issues Table -->
    <div v-if="analysis?.issues?.length > 0" class="bg-white rounded-lg shadow p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">
          Top Issues ({{ analysis.issues.length }} / {{ analysis.totalIssues }})
        </h3>
        <div class="flex gap-2">
          <select v-model="severityFilter"
                  class="px-2 py-1 border border-gray-300 rounded text-sm">
            <option value="">Toutes severites</option>
            <option v-for="sev in severities" :key="sev.key" :value="sev.key">
              {{ sev.label }}
            </option>
          </select>
          <select v-model="typeFilter"
                  class="px-2 py-1 border border-gray-300 rounded text-sm">
            <option value="">Tous types</option>
            <option value="BUG">Bug</option>
            <option value="VULNERABILITY">Vulnerabilite</option>
            <option value="CODE_SMELL">Code Smell</option>
          </select>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Severite</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Type</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Message</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Fichier</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Effort</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="issue in filteredIssues" :key="issue.key" class="hover:bg-gray-50">
              <td class="px-3 py-2">
                <span class="px-2 py-0.5 rounded text-xs font-medium"
                      :class="severityClass(issue.severity)">
                  {{ issue.severity }}
                </span>
              </td>
              <td class="px-3 py-2 text-sm">
                <span class="flex items-center gap-1">
                  <span :class="typeIcon(issue.type)"></span>
                  {{ issue.type }}
                </span>
              </td>
              <td class="px-3 py-2 text-sm max-w-md truncate" :title="issue.message">
                {{ issue.message }}
              </td>
              <td class="px-3 py-2 text-sm font-mono text-gray-600">
                {{ issue.component }}{{ issue.line > 0 ? ':' + issue.line : '' }}
              </td>
              <td class="px-3 py-2 text-sm text-gray-500">
                {{ issue.effort || '-' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- No data message -->
    <div v-if="!analysis" class="text-center py-8 text-gray-500">
      Aucune analyse SonarQube disponible pour ce projet.
      <br>
      <span class="text-sm">Utilisez l'option --sonar-analysis lors de la migration.</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  analysis: {
    type: Object,
    default: null
  }
})

const severityFilter = ref('')
const typeFilter = ref('')

const severities = [
  { key: 'BLOCKER', label: 'Blocker', bgClass: 'bg-red-800' },
  { key: 'CRITICAL', label: 'Critical', bgClass: 'bg-red-600' },
  { key: 'MAJOR', label: 'Major', bgClass: 'bg-orange-500' },
  { key: 'MINOR', label: 'Minor', bgClass: 'bg-yellow-400' },
  { key: 'INFO', label: 'Info', bgClass: 'bg-blue-400' }
]

const qualityGateClass = computed(() => {
  const status = props.analysis?.qualityGateStatus
  if (status === 'OK') return 'bg-green-100 text-green-800'
  if (status === 'WARN') return 'bg-yellow-100 text-yellow-800'
  return 'bg-red-100 text-red-800'
})

const filteredIssues = computed(() => {
  let issues = props.analysis?.issues || []
  if (severityFilter.value) {
    issues = issues.filter(i => i.severity === severityFilter.value)
  }
  if (typeFilter.value) {
    issues = issues.filter(i => i.type === typeFilter.value)
  }
  return issues
})

const maxSeverityCount = computed(() => {
  const counts = props.analysis?.issuesBySeverity || {}
  return Math.max(...Object.values(counts), 1)
})

function getSeverityWidth(severity) {
  const count = props.analysis?.issuesBySeverity?.[severity] || 0
  return (count / maxSeverityCount.value) * 100
}

function ratingClass(rating) {
  const classes = {
    'A': 'text-green-600',
    'B': 'text-lime-600',
    'C': 'text-yellow-600',
    'D': 'text-orange-600',
    'E': 'text-red-600'
  }
  return classes[rating] || 'text-gray-600'
}

function severityClass(severity) {
  const classes = {
    'BLOCKER': 'bg-red-800 text-white',
    'CRITICAL': 'bg-red-600 text-white',
    'MAJOR': 'bg-orange-500 text-white',
    'MINOR': 'bg-yellow-400 text-gray-800',
    'INFO': 'bg-blue-400 text-white'
  }
  return classes[severity] || 'bg-gray-400 text-white'
}

function typeIcon(type) {
  const icons = {
    'BUG': 'text-red-500',
    'VULNERABILITY': 'text-orange-500',
    'CODE_SMELL': 'text-yellow-600',
    'SECURITY_HOTSPOT': 'text-purple-500'
  }
  return icons[type] || ''
}

function formatDebt(minutes) {
  if (!minutes || minutes <= 0) return '0min'
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 8)
  if (days > 0) {
    const remainingHours = hours % 8
    return `${days}j ${remainingHours}h`
  }
  if (hours > 0) {
    const remainingMins = minutes % 60
    return `${hours}h ${remainingMins}min`
  }
  return `${minutes}min`
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
