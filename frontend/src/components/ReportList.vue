<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-bold text-gray-800">Rapports de migration</h1>
      <div class="flex items-center gap-4">
        <!-- View toggle -->
        <div class="flex border border-gray-300 rounded">
          <button @click="viewMode = 'cards'"
                  :class="['px-2 py-1', viewMode === 'cards' ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50']">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/>
            </svg>
          </button>
          <button @click="viewMode = 'list'"
                  :class="['px-2 py-1', viewMode === 'list' ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50']">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
          </button>
        </div>
        <div class="text-sm text-gray-500">
          {{ reportsCount }} rapport(s)
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
      Erreur: {{ error }}
    </div>

    <!-- Empty state -->
    <div v-else-if="!hasReports" class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">Aucun rapport</h3>
      <p class="mt-1 text-sm text-gray-500">
        Les rapports de migration apparaîtront ici une fois soumis par ant2maven.
      </p>
    </div>

    <!-- CARDS VIEW -->
    <div v-else-if="viewMode === 'cards'" class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div v-for="report in reports" :key="report.id"
           :class="cardClass(report)"
           @click="goToReport(report.id)">
        <div class="p-4">
          <!-- Header -->
          <div class="flex items-start justify-between mb-3">
            <div>
              <div class="flex items-center gap-2">
                <h3 class="font-semibold text-gray-900 text-sm">{{ report.projectName }}</h3>
                <span class="px-1.5 py-0.5 text-xxs rounded bg-indigo-100 text-indigo-700">
                  {{ report.migrationType || 'ant2maven' }}
                </span>
              </div>
              <p class="text-xxs text-gray-500 mt-0.5">{{ formatDate(report.migrationDate) }}</p>
            </div>
            <button @click.stop="confirmDelete(report)"
                    class="text-gray-400 hover:text-red-500 transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </button>
          </div>

          <!-- Stats - Dynamic grid with colors -->
          <div class="grid gap-2 mb-3" :class="statsGridClass(report)">
            <!-- Total -->
            <div class="stat-box stat-gray">
              <div class="stat-value">{{ report.totalJars }}</div>
              <div class="stat-label">Total</div>
            </div>
            <!-- Résolus - Mode standard (sans migration-java-dette) -->
            <div v-if="!report.hasMigrationRepo" class="stat-box stat-green">
              <div class="stat-value">{{ report.resolved }}</div>
              <div class="stat-label">Résolus</div>
            </div>
            <!-- Résolus STD (mode migration-java-dette) -->
            <div v-if="report.hasMigrationRepo" class="stat-box stat-green">
              <div class="stat-value">{{ report.resolvedStd || 0 }}</div>
              <div class="stat-label">Rés. STD</div>
            </div>
            <!-- Résolus INT (migration-java-dette internes) -->
            <div v-if="report.hasMigrationRepo" class="stat-box stat-lime">
              <div class="stat-value">{{ report.resolvedInt || 0 }}</div>
              <div class="stat-label">Rés. INT</div>
            </div>
            <!-- Résolus EXT (migration-java-dette externes) -->
            <div v-if="report.hasMigrationRepo" class="stat-box stat-red-bright">
              <div class="stat-value">{{ report.resolvedExt || 0 }}</div>
              <div class="stat-label">Rés. EXT</div>
            </div>
            <!-- Non résolus - Interne -->
            <div class="stat-box stat-yellow">
              <div class="stat-value">{{ report.unresolvedInternal || 0 }}</div>
              <div class="stat-label">Non rés. Int.</div>
            </div>
            <!-- Non résolus - Externe (avec warning si > 0) -->
            <div class="stat-box stat-red relative">
              <div class="stat-value">{{ report.unresolvedExternal || 0 }}</div>
              <div class="stat-label">Non rés. Ext.</div>
              <svg v-if="report.unresolvedExternal > 0"
                   class="absolute top-1 right-1 w-3 h-3 text-red-600"
                   fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92z" clip-rule="evenodd"/>
              </svg>
            </div>
            <!-- Provided (auto-fix) -->
            <div v-if="report.providedAutoFix > 0" class="stat-box stat-purple">
              <div class="stat-value">{{ report.providedAutoFix }}</div>
              <div class="stat-label">Provided</div>
            </div>
            <!-- Missing -->
            <div v-if="report.missingCount > 0" class="stat-box stat-red-dark">
              <div class="stat-value">{{ report.missingCount }}</div>
              <div class="stat-label">Manquants</div>
            </div>
          </div>

          <!-- Progress bar -->
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: getCoverageRate(report) + '%' }"></div>
          </div>
          <div class="text-right text-xxs text-gray-500 mt-1">
            {{ getCoverageRate(report).toFixed(1) }}% couverture
          </div>

          <!-- Indicators -->
          <div class="mt-2 pt-2 border-t border-gray-200">
            <ReportIndicators
              :auto-fix-enabled="report.providedAutoFix > 0"
              :compilation-success="report.compilationSuccess"
              :cve-severity="report.cveSummary?.maxSeverity"
              :artifactory-enabled="report.artifactoryEnabled"
              :nexus-enabled="report.nexusEnabled"
              :libraries-uploaded="report.deployedLibrariesCount || 0"
              :jdeps-dependencies="report.jdepsDependencies"
              :sonar-issues="report.sonarIssues"
              :oss-analyzed="report.ossAnalyzed"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- LIST VIEW -->
    <div v-else class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Projet</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
            <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Total</th>
            <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Résolus</th>
            <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Non rés.</th>
            <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Couverture</th>
            <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">État</th>
            <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="report in reports" :key="report.id"
              :class="listRowClass(report)"
              @click="goToReport(report.id)"
              class="cursor-pointer">
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <span class="font-medium text-gray-900 text-sm">{{ report.projectName }}</span>
                <span class="px-1.5 py-0.5 text-xxs rounded bg-indigo-100 text-indigo-700">
                  {{ report.migrationType || 'ant2maven' }}
                </span>
              </div>
            </td>
            <td class="px-4 py-3 text-xs text-gray-500">
              {{ formatDate(report.migrationDate) }}
            </td>
            <td class="px-4 py-3 text-center">
              <span class="text-sm font-medium text-gray-700">{{ report.totalJars }}</span>
            </td>
            <td class="px-4 py-3 text-center">
              <span class="text-sm font-medium text-green-600">{{ report.resolved }}</span>
            </td>
            <td class="px-4 py-3 text-center">
              <span class="text-sm font-medium text-red-600">{{ report.unresolved }}</span>
            </td>
            <td class="px-4 py-3 text-center">
              <div class="flex items-center justify-center gap-2">
                <div class="w-16 h-2 bg-gray-200 rounded-full">
                  <div class="h-2 bg-primary-500 rounded-full" :style="{ width: getCoverageRate(report) + '%' }"></div>
                </div>
                <span class="text-xs text-gray-600">{{ getCoverageRate(report).toFixed(0) }}%</span>
              </div>
            </td>
            <td class="px-4 py-3 text-center">
              <ReportIndicators
                :auto-fix-enabled="report.providedAutoFix > 0"
                :compilation-success="report.compilationSuccess"
                :cve-severity="report.cveSummary?.maxSeverity"
                :artifactory-enabled="report.artifactoryEnabled"
                :nexus-enabled="report.nexusEnabled"
                :libraries-uploaded="report.deployedLibrariesCount || 0"
                :jdeps-dependencies="report.jdepsDependencies"
                :sonar-issues="report.sonarIssues"
                :oss-analyzed="report.ossAnalyzed"
              />
            </td>
            <td class="px-4 py-3 text-right">
              <button @click.stop="confirmDelete(report)"
                      class="text-gray-400 hover:text-red-500 transition-colors">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Delete confirmation modal -->
    <div v-if="showDeleteModal"
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-sm mx-4">
        <h3 class="text-lg font-semibold text-gray-900 mb-2">Supprimer le rapport ?</h3>
        <p class="text-sm text-gray-600 mb-4">
          Le rapport "{{ reportToDelete?.projectName }}" sera supprimé définitivement.
        </p>
        <div class="flex justify-end space-x-3">
          <button @click="showDeleteModal = false"
                  class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">
            Annuler
          </button>
          <button @click="executeDelete"
                  class="px-4 py-2 text-sm bg-red-500 text-white rounded hover:bg-red-600">
            Supprimer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useReportStore } from '@/stores/reportStore'
import ReportIndicators from '@/components/ReportIndicators.vue'

const router = useRouter()
const store = useReportStore()

const showDeleteModal = ref(false)
const reportToDelete = ref(null)
const viewMode = ref('cards')

const reports = computed(() => store.reports)
const loading = computed(() => store.loading)
const error = computed(() => store.error)
const reportsCount = computed(() => store.reportsCount)
const hasReports = computed(() => store.hasReports)

onMounted(() => {
  store.fetchReports()
})

function goToReport(id) {
  router.push({ name: 'report-detail', params: { id } })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function confirmDelete(report) {
  reportToDelete.value = report
  showDeleteModal.value = true
}

async function executeDelete() {
  if (reportToDelete.value) {
    try {
      await store.deleteReport(reportToDelete.value.id)
    } catch (e) {
      console.error('Erreur suppression:', e)
    }
  }
  showDeleteModal.value = false
  reportToDelete.value = null
}

function cardClass(report) {
  // Priorité: CVE critique > Compilation échouée > Erreurs > Normal
  const hasCriticalCve = report.cveSummary?.maxSeverity === 'CRITICAL'
  const hasHighCve = report.cveSummary?.maxSeverity === 'HIGH'
  const hasMissing = report.missingCount > 0 || report.compilationSuccess === false

  if (hasCriticalCve) {
    return 'bg-blue-50 border-2 border-red-800 rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer'
  }
  if (hasHighCve) {
    return 'bg-red-50 border-2 border-red-400 rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer'
  }
  if (hasMissing) {
    return 'bg-red-50 border border-red-200 rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer'
  }
  return 'bg-blue-50 border border-blue-200 rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer'
}

function listRowClass(report) {
  const hasCriticalCve = report.cveSummary?.maxSeverity === 'CRITICAL'
  const hasMissing = report.missingCount > 0 || report.compilationSuccess === false

  if (hasCriticalCve) return 'bg-blue-50 hover:bg-blue-100'
  if (hasMissing) return 'bg-red-50 hover:bg-red-100'
  return 'hover:bg-gray-50'
}

function statsGridClass(report) {
  let cols = 4 // Total + Non rés. Int. + Non rés. Ext. + 1 (Résolus ou STD)
  if (report.hasMigrationRepo) cols += 2 // INT + EXT
  if (report.providedAutoFix > 0) cols++
  if (report.missingCount > 0) cols++

  if (cols <= 4) return 'grid-cols-4'
  if (cols === 5) return 'grid-cols-5'
  if (cols === 6) return 'grid-cols-6'
  if (cols === 7) return 'grid-cols-7'
  return 'grid-cols-8'
}

function getCoverageRate(report) {
  return report.coverageRate ?? report.successRate ?? 0
}
</script>

<style scoped>
.stat-box {
  @apply text-center p-2 rounded;
}

.stat-value {
  @apply text-lg font-bold;
}

.stat-label {
  @apply text-xxs text-gray-500;
}

.stat-gray {
  @apply bg-gray-100;
}
.stat-gray .stat-value {
  @apply text-gray-700;
}

.stat-green {
  @apply bg-green-100;
}
.stat-green .stat-value {
  @apply text-green-600;
}

.stat-yellow {
  @apply bg-yellow-100;
}
.stat-yellow .stat-value {
  @apply text-yellow-600;
}

.stat-red {
  @apply bg-red-100;
}
.stat-red .stat-value {
  @apply text-red-600;
}

.stat-red-dark {
  @apply bg-red-200;
}
.stat-red-dark .stat-value {
  @apply text-red-700;
}

.stat-purple {
  @apply bg-purple-100;
}
.stat-purple .stat-value {
  @apply text-purple-600;
}

.stat-lime {
  @apply bg-lime-100;
}
.stat-lime .stat-value {
  @apply text-lime-700;
}

.stat-red-bright {
  @apply bg-red-200;
}
.stat-red-bright .stat-value {
  @apply text-red-700;
}
</style>
