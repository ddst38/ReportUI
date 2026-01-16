<template>
  <div class="space-y-6">
    <!-- Score global de viabilité -->
    <div v-if="analysis" class="bg-white rounded-lg shadow p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">Radar de Viabilité OSS</h3>
        <span v-if="analysis.analysisDate" class="text-xs text-gray-400">
          {{ formatDate(analysis.analysisDate) }}
        </span>
      </div>

      <!-- Score principal avec jauge -->
      <div class="flex items-center gap-8 mb-6">
        <div class="relative w-32 h-32">
          <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
            <!-- Background circle -->
            <circle cx="50" cy="50" r="45" fill="none" stroke="#e5e7eb" stroke-width="10"/>
            <!-- Progress circle -->
            <circle cx="50" cy="50" r="45" fill="none"
                    :stroke="scoreColor"
                    stroke-width="10"
                    stroke-linecap="round"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="scoreOffset"/>
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <span class="text-3xl font-bold" :class="scoreTextClass">
              {{ analysis.overallHealthScore?.toFixed(1) }}
            </span>
            <span class="text-xs text-gray-500">/10</span>
          </div>
        </div>

        <div class="flex-1">
          <div class="text-2xl font-bold mb-1" :class="statusClass">
            {{ statusLabel }}
          </div>
          <p class="text-gray-600 text-sm">
            {{ analysis.totalDependencies }} dépendances analysées
          </p>
          <p class="text-gray-500 text-sm mt-2">
            {{ analysis.healthyPercentage?.toFixed(1) }}% de dépendances saines
          </p>
        </div>

        <!-- Indicateurs clés -->
        <div class="grid grid-cols-3 gap-4">
          <div class="text-center p-3 bg-orange-50 rounded-lg">
            <div class="text-2xl font-bold text-orange-600">{{ analysis.outdatedCount }}</div>
            <div class="text-xs text-gray-500">Obsolètes</div>
          </div>
          <div class="text-center p-3 bg-red-50 rounded-lg">
            <div class="text-2xl font-bold text-red-600">{{ analysis.vulnerableCount }}</div>
            <div class="text-xs text-gray-500">Vulnérables</div>
          </div>
          <div class="text-center p-3 bg-purple-50 rounded-lg">
            <div class="text-2xl font-bold text-purple-600">{{ analysis.staleCount }}</div>
            <div class="text-xs text-gray-500">Inactives</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Distribution santé -->
    <div class="grid md:grid-cols-2 gap-4">
      <!-- Répartition par statut -->
      <div class="bg-white rounded-lg shadow p-4">
        <h3 class="text-lg font-semibold mb-4">Distribution de santé</h3>
        <div class="space-y-3">
          <div v-for="status in healthStatuses" :key="status.key" class="flex items-center gap-3">
            <div class="w-24 text-sm">{{ status.label }}</div>
            <div class="flex-1 bg-gray-200 rounded-full h-6 overflow-hidden">
              <div :class="status.bgClass"
                   :style="{ width: getStatusWidth(status.key) + '%' }"
                   class="h-full transition-all duration-500 flex items-center justify-end pr-2">
                <span v-if="getStatusCount(status.key) > 0" class="text-xs font-medium text-white">
                  {{ getStatusCount(status.key) }}
                </span>
              </div>
            </div>
            <div class="w-12 text-right text-sm font-mono">
              {{ getStatusPercentage(status.key) }}%
            </div>
          </div>
        </div>
      </div>

      <!-- Légende scoring -->
      <div class="bg-white rounded-lg shadow p-4">
        <h3 class="text-lg font-semibold mb-4">Grille de scoring</h3>
        <div class="text-sm space-y-2">
          <div class="flex justify-between py-1 border-b">
            <span class="text-gray-600">Score de base</span>
            <span class="font-mono">10.0</span>
          </div>
          <div class="text-gray-500 text-xs mb-2">Pénalités appliquées :</div>
          <div class="flex justify-between text-red-600">
            <span>Version majeure en retard</span>
            <span class="font-mono">-1.5 à -3.0</span>
          </div>
          <div class="flex justify-between text-orange-600">
            <span>Release > 1-3 ans</span>
            <span class="font-mono">-1.0 à -3.0</span>
          </div>
          <div class="flex justify-between text-red-700">
            <span>Vulnérabilités (par sévérité)</span>
            <span class="font-mono">-0.2 à -1.0</span>
          </div>
          <div class="text-gray-500 text-xs mt-2">Bonus :</div>
          <div class="flex justify-between text-green-600">
            <span>Popularité Maven (top libs)</span>
            <span class="font-mono">+0.5 à +1.5</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Alertes critiques -->
    <div v-if="analysis?.criticalAlerts?.length > 0" class="bg-white rounded-lg shadow p-4">
      <h3 class="text-lg font-semibold mb-4 text-red-600">
        Alertes prioritaires ({{ analysis.criticalAlerts.length }})
      </h3>
      <div class="space-y-2">
        <div v-for="alert in analysis.criticalAlerts.slice(0, 10)" :key="alert.groupId + ':' + alert.artifactId"
             class="flex items-center justify-between p-3 bg-red-50 rounded-lg border border-red-200">
          <div>
            <div class="font-medium text-red-800">
              {{ alert.groupId }}:{{ alert.artifactId }}
            </div>
            <div class="text-sm text-red-600">
              {{ alert.currentVersion }}
              <span v-if="alert.latestVersion" class="text-gray-500">
                → {{ alert.latestVersion }}
              </span>
            </div>
          </div>
          <div class="text-right">
            <div class="text-lg font-bold" :class="getScoreClass(alert.healthScore)">
              {{ alert.healthScore?.toFixed(1) }}
            </div>
            <div class="text-xs text-gray-500">
              <span v-if="alert.majorVersionsBehind > 0" class="text-orange-600">
                {{ alert.majorVersionsBehind }} maj.
              </span>
              <span v-if="alert.totalVulnerabilities > 0" class="text-red-600 ml-1">
                {{ alert.totalVulnerabilities }} vuln.
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tableau complet des dépendances -->
    <div v-if="analysis?.dependencies?.length > 0" class="bg-white rounded-lg shadow p-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">
          Toutes les dépendances ({{ filteredDependencies.length }})
        </h3>
        <div class="flex gap-2">
          <select v-model="statusFilter" class="px-2 py-1 border border-gray-300 rounded text-sm">
            <option value="">Tous statuts</option>
            <option v-for="status in healthStatuses" :key="status.key" :value="status.key">
              {{ status.label }}
            </option>
          </select>
          <select v-model="sortBy" class="px-2 py-1 border border-gray-300 rounded text-sm">
            <option value="score">Tri par score</option>
            <option value="versions">Tri par retard</option>
            <option value="vulns">Tri par vulnérabilités</option>
            <option value="name">Tri par nom</option>
          </select>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Score</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Dépendance</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Version actuelle</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Dernière version</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Retard</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Vulns</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Statut</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="dep in paginatedDependencies" :key="dep.groupId + ':' + dep.artifactId"
                class="hover:bg-gray-50">
              <td class="px-3 py-2">
                <span class="text-lg font-bold" :class="getScoreClass(dep.healthScore)">
                  {{ dep.healthScore?.toFixed(1) }}
                </span>
              </td>
              <td class="px-3 py-2">
                <div class="font-medium text-gray-900">{{ dep.artifactId }}</div>
                <div class="text-xs text-gray-500">{{ dep.groupId }}</div>
              </td>
              <td class="px-3 py-2 font-mono text-sm">
                {{ dep.currentVersion }}
              </td>
              <td class="px-3 py-2 font-mono text-sm">
                <span v-if="dep.hasUpdate" class="text-green-600">{{ dep.latestVersion }}</span>
                <span v-else class="text-gray-400">-</span>
              </td>
              <td class="px-3 py-2">
                <div v-if="dep.majorVersionsBehind > 0" class="text-red-600 font-medium">
                  {{ dep.majorVersionsBehind }} majeure(s)
                </div>
                <div v-else-if="dep.minorVersionsBehind > 0" class="text-orange-600">
                  {{ dep.minorVersionsBehind }} mineure(s)
                </div>
                <div v-else-if="dep.patchVersionsBehind > 0" class="text-yellow-600 text-sm">
                  {{ dep.patchVersionsBehind }} patch(s)
                </div>
                <div v-else class="text-green-600 text-sm">À jour</div>
              </td>
              <td class="px-3 py-2">
                <div v-if="dep.totalVulnerabilities > 0" class="flex gap-1">
                  <span v-if="dep.criticalVulns > 0" class="px-1.5 py-0.5 bg-red-800 text-white text-xs rounded">
                    {{ dep.criticalVulns }}C
                  </span>
                  <span v-if="dep.highVulns > 0" class="px-1.5 py-0.5 bg-red-600 text-white text-xs rounded">
                    {{ dep.highVulns }}H
                  </span>
                  <span v-if="dep.mediumVulns > 0" class="px-1.5 py-0.5 bg-orange-500 text-white text-xs rounded">
                    {{ dep.mediumVulns }}M
                  </span>
                  <span v-if="dep.lowVulns > 0" class="px-1.5 py-0.5 bg-yellow-400 text-gray-800 text-xs rounded">
                    {{ dep.lowVulns }}L
                  </span>
                </div>
                <span v-else class="text-green-600 text-sm">Aucune</span>
              </td>
              <td class="px-3 py-2">
                <span class="px-2 py-1 rounded text-xs font-medium"
                      :class="getStatusBadgeClass(dep.healthStatus)">
                  {{ getStatusLabel(dep.healthStatus) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-between mt-4 pt-4 border-t">
        <div class="text-sm text-gray-500">
          Page {{ currentPage }} / {{ totalPages }}
        </div>
        <div class="flex gap-2">
          <button @click="currentPage--" :disabled="currentPage === 1"
                  class="px-3 py-1 border rounded text-sm disabled:opacity-50">
            Précédent
          </button>
          <button @click="currentPage++" :disabled="currentPage === totalPages"
                  class="px-3 py-1 border rounded text-sm disabled:opacity-50">
            Suivant
          </button>
        </div>
      </div>
    </div>

    <!-- Message si pas de données -->
    <div v-if="!analysis" class="text-center py-8 text-gray-500">
      Aucune analyse OSS Index disponible pour ce projet.
      <br>
      <span class="text-sm">Utilisez l'option --oss-analysis lors de la migration.</span>
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

const statusFilter = ref('')
const sortBy = ref('score')
const currentPage = ref(1)
const pageSize = 20

const circumference = 2 * Math.PI * 45

const healthStatuses = [
  { key: 'HEALTHY', label: 'Sain', bgClass: 'bg-green-500' },
  { key: 'MONITOR', label: 'À surveiller', bgClass: 'bg-yellow-500' },
  { key: 'RISKY', label: 'Risqué', bgClass: 'bg-orange-500' },
  { key: 'CRITICAL', label: 'Critique', bgClass: 'bg-red-600' }
]

const scoreColor = computed(() => {
  const score = props.analysis?.overallHealthScore || 0
  if (score >= 8) return '#22c55e' // green
  if (score >= 6) return '#eab308' // yellow
  if (score >= 4) return '#f97316' // orange
  return '#dc2626' // red
})

const scoreOffset = computed(() => {
  const score = props.analysis?.overallHealthScore || 0
  const progress = score / 10
  return circumference * (1 - progress)
})

const scoreTextClass = computed(() => {
  const score = props.analysis?.overallHealthScore || 0
  if (score >= 8) return 'text-green-600'
  if (score >= 6) return 'text-yellow-600'
  if (score >= 4) return 'text-orange-600'
  return 'text-red-600'
})

const statusClass = computed(() => {
  const status = props.analysis?.overallStatus
  const classes = {
    'HEALTHY': 'text-green-600',
    'MONITOR': 'text-yellow-600',
    'RISKY': 'text-orange-600',
    'CRITICAL': 'text-red-600'
  }
  return classes[status] || 'text-gray-600'
})

const statusLabel = computed(() => {
  const labels = {
    'HEALTHY': 'Écosystème Sain',
    'MONITOR': 'À Surveiller',
    'RISKY': 'Risques Identifiés',
    'CRITICAL': 'Situation Critique'
  }
  return labels[props.analysis?.overallStatus] || 'Inconnu'
})

const filteredDependencies = computed(() => {
  let deps = props.analysis?.dependencies || []

  if (statusFilter.value) {
    deps = deps.filter(d => d.healthStatus === statusFilter.value)
  }

  // Tri
  switch (sortBy.value) {
    case 'score':
      deps = [...deps].sort((a, b) => a.healthScore - b.healthScore)
      break
    case 'versions':
      deps = [...deps].sort((a, b) => {
        const aTotal = (a.majorVersionsBehind * 100) + (a.minorVersionsBehind * 10) + a.patchVersionsBehind
        const bTotal = (b.majorVersionsBehind * 100) + (b.minorVersionsBehind * 10) + b.patchVersionsBehind
        return bTotal - aTotal
      })
      break
    case 'vulns':
      deps = [...deps].sort((a, b) => b.totalVulnerabilities - a.totalVulnerabilities)
      break
    case 'name':
      deps = [...deps].sort((a, b) => a.artifactId.localeCompare(b.artifactId))
      break
  }

  return deps
})

const totalPages = computed(() => Math.ceil(filteredDependencies.value.length / pageSize))

const paginatedDependencies = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredDependencies.value.slice(start, start + pageSize)
})

function getStatusCount(status) {
  return props.analysis?.healthDistribution?.[status] || 0
}

function getStatusWidth(status) {
  const count = getStatusCount(status)
  const total = props.analysis?.totalDependencies || 1
  return (count / total) * 100
}

function getStatusPercentage(status) {
  const count = getStatusCount(status)
  const total = props.analysis?.totalDependencies || 1
  return Math.round((count / total) * 100)
}

function getScoreClass(score) {
  if (score >= 8) return 'text-green-600'
  if (score >= 6) return 'text-yellow-600'
  if (score >= 4) return 'text-orange-600'
  return 'text-red-600'
}

function getStatusBadgeClass(status) {
  const classes = {
    'HEALTHY': 'bg-green-100 text-green-800',
    'MONITOR': 'bg-yellow-100 text-yellow-800',
    'RISKY': 'bg-orange-100 text-orange-800',
    'CRITICAL': 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function getStatusLabel(status) {
  const labels = {
    'HEALTHY': 'Sain',
    'MONITOR': 'Surveiller',
    'RISKY': 'Risqué',
    'CRITICAL': 'Critique'
  }
  return labels[status] || status
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
