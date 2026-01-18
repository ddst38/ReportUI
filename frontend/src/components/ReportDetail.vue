<template>
  <div>
    <!-- Back button -->
    <div class="flex items-center justify-between mb-4">
      <router-link to="/" class="inline-flex items-center text-sm text-primary-600 hover:text-primary-800">
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Retour aux rapports
      </router-link>

      <!-- Export PDF button -->
      <button v-if="report" @click="exportPdf"
              class="inline-flex items-center px-3 py-1.5 text-sm bg-primary-600 text-white rounded hover:bg-primary-700 transition-colors print:hidden">
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
        </svg>
        Export PDF
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
      Erreur: {{ error }}
    </div>

    <!-- Report content -->
    <div v-else-if="report" id="report-content">
      <!-- Header with dynamic coloring -->
      <div :class="headerClass" class="rounded-lg shadow p-4 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <div class="flex items-center gap-3">
              <h1 class="text-xl font-bold text-gray-900">{{ report.projectName }}</h1>
              <span class="px-2 py-0.5 text-xs rounded bg-indigo-100 text-indigo-700">
                {{ report.migrationType || 'ant2maven' }}
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">Migration du {{ formatDate(report.migrationDate) }}</p>
            <!-- Indicateurs -->
            <div class="mt-2">
              <ReportIndicators
                :auto-fix-enabled="report.statistics?.providedAutoFix > 0"
                :compilation-success="report.compilationSuccess"
                :cve-severity="report.cveSummary?.maxSeverity"
                :artifactory-enabled="isArtifactoryEnabled"
                :nexus-enabled="isNexusEnabled"
                :libraries-uploaded="report.deploymentInfo?.deployedCount || 0"
                :jdeps-dependencies="report.jdepsAnalysis?.summary?.totalDependencies"
                :sonar-issues="report.sonarAnalysis?.summary?.totalIssues"
                :oss-analyzed="report.ossAnalysis?.summary?.totalAnalyzed"
              />
            </div>
          </div>
          <div class="text-right">
            <div class="text-3xl font-bold" :class="coverageRateColor">
              {{ getCoverageRate().toFixed(1) }}%
            </div>
            <div class="text-xs text-gray-500">Taux de couverture</div>
          </div>
        </div>
      </div>

      <!-- Stats cards - Dynamic -->
      <div class="grid gap-4 mb-6" :class="statsGridClass">
        <div class="stat-card bg-green-100">
          <div class="stat-value text-green-700">{{ report.statistics.totalJars }}</div>
          <div class="stat-label text-green-600">JARs détectés</div>
        </div>
        <!-- Mode standard -->
        <div v-if="!report.statistics.hasMigrationRepo" class="stat-card bg-green-200">
          <div class="stat-value text-green-800">{{ report.statistics.resolved }}</div>
          <div class="stat-label text-green-700">Résolus</div>
        </div>
        <!-- Mode migration-java-dette -->
        <div v-if="report.statistics.hasMigrationRepo" class="stat-card bg-green-200">
          <div class="stat-value text-green-800">{{ report.statistics.resolvedStd || 0 }}</div>
          <div class="stat-label text-green-700">Rés. STD</div>
        </div>
        <div v-if="report.statistics.hasMigrationRepo" class="stat-card bg-lime-100">
          <div class="stat-value text-lime-700">{{ report.statistics.resolvedInt || 0 }}</div>
          <div class="stat-label text-lime-600">Rés. INT</div>
        </div>
        <div v-if="report.statistics.hasMigrationRepo" class="stat-card bg-red-100">
          <div class="stat-value text-red-700">{{ report.statistics.resolvedExt || 0 }}</div>
          <div class="stat-label text-red-600">Rés. EXT</div>
        </div>
        <div class="stat-card bg-orange-100">
          <div class="stat-value text-orange-700">{{ report.statistics.unresolvedInternal || 0 }}</div>
          <div class="stat-label text-orange-600">Non rés. Interne</div>
        </div>
        <div class="stat-card bg-red-100">
          <div class="stat-value text-red-700">{{ report.statistics.unresolvedExternal || 0 }}</div>
          <div class="stat-label text-red-600">Non rés. Externe</div>
        </div>
        <div v-if="report.statistics.providedAutoFix > 0" class="stat-card bg-purple-100">
          <div class="stat-value text-purple-700">{{ report.statistics.providedAutoFix }}</div>
          <div class="stat-label text-purple-600">Provided</div>
        </div>
        <div v-if="report.statistics.missingCount > 0" class="stat-card bg-red-200">
          <div class="stat-value text-red-800">{{ report.statistics.missingCount }}</div>
          <div class="stat-label text-red-700">Manquants</div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="mb-6 border-b border-gray-200">
        <nav class="flex space-x-8">
          <button @click="activeTab = 'rapport'"
                  :class="tabClass('rapport')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            Rapport
          </button>
          <button @click="activeTab = 'detected'"
                  :class="tabClass('detected')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            Bibliothèques détectées ({{ report.detectedJars?.length || 0 }})
          </button>
          <button v-if="hasCveData"
                  @click="activeTab = 'cve'"
                  :class="tabClass('cve')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            <span class="flex items-center gap-1.5">
              <span :class="['w-2 h-2 rounded-full', cveSeverityDot]"></span>
              Vulnérabilités CVE ({{ report.cveSummary?.totalVulnerabilities || 0 }})
            </span>
          </button>
          <button v-if="hasCveData"
                  @click="activeTab = 'cve-stats'"
                  :class="tabClass('cve-stats')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            CVE Stats
          </button>
          <button v-if="hasDeploymentData"
                  @click="activeTab = 'deployed'"
                  :class="tabClass('deployed')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            <span class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full bg-green-500"></span>
              Librairies déployées ({{ report.deploymentInfo?.deployedCount || 0 }})
            </span>
          </button>
          <button v-if="hasJdepsData"
                  @click="activeTab = 'jdeps'"
                  :class="tabClass('jdeps')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            <span class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full" :class="jdepsStatusDot"></span>
              Analyse Structurelle
            </span>
          </button>
          <button v-if="hasSonarData"
                  @click="activeTab = 'sonar'"
                  :class="tabClass('sonar')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            <span class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full" :class="sonarStatusDot"></span>
              Analyse Sonar
            </span>
          </button>
          <button v-if="hasOssData"
                  @click="activeTab = 'oss'"
                  :class="tabClass('oss')"
                  class="py-2 px-1 text-sm font-medium border-b-2 transition-colors">
            <span class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full" :class="ossStatusDot"></span>
              Analyse OSS
            </span>
          </button>
        </nav>
      </div>

      <!-- Tab: Rapport -->
      <div v-if="activeTab === 'rapport'">
        <!-- Charts row -->
        <div class="grid md:grid-cols-3 gap-4 mb-6">
          <!-- Donut: Resolved vs Unresolved -->
          <DonutChart
            title="Résolution"
            :labels="['Résolus', 'Non résolus']"
            :data="[report.statistics.resolved, report.statistics.unresolved]"
            :colors="['#22c55e', '#eab308']"
          />

          <!-- Donut: By Scope -->
          <DonutChart
            title="Par portée (scope)"
            :labels="scopeLabels"
            :data="scopeData"
            :colors="['#3b82f6', '#8b5cf6', '#f97316', '#6b7280', '#ef4444']"
          />

          <!-- Radar: Key metrics -->
          <RadarChart
            title="Métriques clés"
            :labels="['Total', 'Résolus', 'Internes', 'Externes', 'Test']"
            :data="radarData"
          />
        </div>

        <!-- Bar chart: Library location -->
        <div class="mb-6">
          <BarChart
            title="Localisation des librairies"
            :labels="locationLabels"
            :data="locationData"
            :colors="locationColors"
          />
        </div>

        <!-- Missing packages section -->
        <div v-if="report.missingPackages?.length > 0" class="mb-6">
          <MissingPackages :packages="report.missingPackages" />
        </div>

        <!-- Version conflicts section -->
        <div class="mb-6">
          <VersionConflicts :libraries="report.libraries" />
        </div>

        <!-- Libraries table -->
        <LibraryTable :libraries="report.libraries" />
      </div>

      <!-- Tab: Detected Libraries -->
      <div v-if="activeTab === 'detected'">
        <DetectedLibraries :jars="report.detectedJars || []" />
      </div>

      <!-- Tab: CVE Vulnerabilities -->
      <div v-if="activeTab === 'cve'">
        <CveTable :vulnerabilities="report.cveVulnerabilities || []" />
      </div>

      <!-- Tab: CVE Stats -->
      <div v-if="activeTab === 'cve-stats'">
        <CveStatsTab
          :summary="report.cveSummary || {}"
          :vulnerabilities="report.cveVulnerabilities || []"
        />
      </div>

      <!-- Tab: Deployed Libraries (Remote mode) -->
      <div v-if="activeTab === 'deployed'">
        <DeployedLibrariesTab :deploymentInfo="report.deploymentInfo" />
      </div>

      <!-- Tab: Jdeps Analysis -->
      <div v-if="activeTab === 'jdeps'">
        <JdepsAnalysis :analysis="report.jdepsAnalysis" />
      </div>

      <!-- Tab: Sonar Analysis -->
      <div v-if="activeTab === 'sonar'">
        <SonarAnalysis :analysis="report.sonarAnalysis" />
      </div>

      <!-- Tab: OSS Index Analysis -->
      <div v-if="activeTab === 'oss'">
        <OssAnalysis :analysis="report.ossAnalysis" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useReportStore } from '@/stores/reportStore'
import DonutChart from '@/components/charts/DonutChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import LibraryTable from '@/components/LibraryTable.vue'
import VersionConflicts from '@/components/VersionConflicts.vue'
import MissingPackages from '@/components/MissingPackages.vue'
import DetectedLibraries from '@/components/DetectedLibraries.vue'
import CveTable from '@/components/CveTable.vue'
import CveStatsTab from '@/components/CveStatsTab.vue'
import DeployedLibrariesTab from '@/components/DeployedLibrariesTab.vue'
import ReportIndicators from '@/components/ReportIndicators.vue'
import JdepsAnalysis from '@/components/JdepsAnalysis.vue'
import SonarAnalysis from '@/components/SonarAnalysis.vue'
import OssAnalysis from '@/components/OssAnalysis.vue'

const props = defineProps({
  id: {
    type: String,
    required: true
  }
})

const store = useReportStore()
const activeTab = ref('rapport')

const report = computed(() => store.currentReport)
const loading = computed(() => store.loading)
const error = computed(() => store.error)

onMounted(() => {
  store.fetchReport(props.id)
})

watch(() => props.id, (newId) => {
  store.fetchReport(newId)
  activeTab.value = 'rapport'
})

// Header class based on compilation success
const headerClass = computed(() => {
  const compiles = report.value?.compilationSuccess !== false
  return compiles ? 'bg-blue-50 border border-blue-200' : 'bg-red-50 border border-red-200'
})

// Coverage rate color
const coverageRateColor = computed(() => {
  const rate = getCoverageRate()
  if (rate >= 90) return 'text-green-600'
  if (rate >= 70) return 'text-yellow-600'
  return 'text-red-600'
})

function getCoverageRate() {
  return report.value?.statistics?.coverageRate ?? report.value?.statistics?.successRate ?? 0
}

// CVE data
const hasCveData = computed(() => {
  return report.value?.cveSummary != null || (report.value?.cveVulnerabilities?.length > 0)
})

// Deployment data (mode REMOTE)
const hasDeploymentData = computed(() => {
  return report.value?.deploymentInfo != null &&
         report.value?.deploymentInfo?.deployedLibraries?.length > 0
})

// Jdeps data
const hasJdepsData = computed(() => {
  return report.value?.jdepsAnalysis != null &&
         report.value?.jdepsAnalysis?.summary != null
})

// Sonar data
const hasSonarData = computed(() => {
  return report.value?.sonarAnalysis != null &&
         report.value?.sonarAnalysis?.metrics != null
})

// OSS Index data
const hasOssData = computed(() => {
  return report.value?.ossAnalysis != null &&
         report.value?.ossAnalysis?.totalDependencies > 0
})

// Artifactory/Nexus detection via byMethod
const isArtifactoryEnabled = computed(() => {
  const byMethod = report.value?.statistics?.byMethod
  if (!byMethod) return false
  return byMethod['ARTIFACTORY'] > 0 || byMethod['ARTIFACTORY_CHECKSUM'] > 0
})

const isNexusEnabled = computed(() => {
  const byMethod = report.value?.statistics?.byMethod
  if (!byMethod) return false
  return byMethod['NEXUS'] > 0 || byMethod['NEXUS_CHECKSUM'] > 0
})

const cveSeverityDot = computed(() => {
  const severity = report.value?.cveSummary?.maxSeverity
  const classes = {
    'CRITICAL': 'bg-red-800',
    'HIGH': 'bg-red-600',
    'MEDIUM': 'bg-orange-500',
    'LOW': 'bg-yellow-400',
    'NONE': 'bg-green-500'
  }
  return classes[severity] || 'bg-green-500'
})

const jdepsStatusDot = computed(() => {
  const jdeps = report.value?.jdepsAnalysis?.summary
  if (!jdeps) return 'bg-gray-400'
  if (jdeps.cycleCount > 0 || jdeps.jdkInternalCount > 0) return 'bg-orange-500'
  return 'bg-green-500'
})

const sonarStatusDot = computed(() => {
  const sonar = report.value?.sonarAnalysis
  if (!sonar) return 'bg-gray-400'
  if (sonar.qualityGateStatus === 'ERROR') return 'bg-red-600'
  if (sonar.qualityGateStatus === 'WARN') return 'bg-orange-500'
  if (sonar.criticalIssuesCount > 0) return 'bg-orange-500'
  return 'bg-green-500'
})

const ossStatusDot = computed(() => {
  const oss = report.value?.ossAnalysis
  if (!oss) return 'bg-gray-400'
  if (oss.overallStatus === 'CRITICAL') return 'bg-red-600'
  if (oss.overallStatus === 'RISKY') return 'bg-orange-500'
  if (oss.overallStatus === 'MONITOR') return 'bg-yellow-500'
  return 'bg-green-500'
})

// Stats grid class
const statsGridClass = computed(() => {
  let cols = 4 // Base: Total, Résolus/STD, Non rés. Int., Non rés. Ext.
  if (report.value?.statistics?.hasMigrationRepo) cols += 2 // INT + EXT
  if (report.value?.statistics?.providedAutoFix > 0) cols++
  if (report.value?.statistics?.missingCount > 0) cols++

  if (cols <= 4) return 'grid-cols-2 md:grid-cols-4'
  if (cols === 5) return 'grid-cols-2 md:grid-cols-5'
  if (cols === 6) return 'grid-cols-3 md:grid-cols-6'
  if (cols === 7) return 'grid-cols-3 md:grid-cols-7'
  return 'grid-cols-4 md:grid-cols-8'
})

// Tab styling
function tabClass(tab) {
  return activeTab.value === tab
    ? 'border-primary-600 text-primary-600'
    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
}

// Scope chart data
const scopeLabels = computed(() => {
  if (!report.value?.statistics?.byScope) return []
  return Object.keys(report.value.statistics.byScope)
})

const scopeData = computed(() => {
  if (!report.value?.statistics?.byScope) return []
  return Object.values(report.value.statistics.byScope)
})

// Method chart data
const methodLabels = computed(() => {
  if (!report.value?.statistics?.byMethod) return []
  const labels = {
    'INTERNAL_PATTERN': 'Interne',
    'KNOWN_CONFIG': 'Connu',
    'ARTIFACTORY_CHECKSUM': 'Artif. SHA',
    'ARTIFACTORY': 'Artifactory',
    'CHECKSUM': 'Central',
    'MANIFEST': 'Manifest',
    'PATTERN': 'Pattern',
    'PACKAGE_ANALYSIS': 'Packages',
    'UNRESOLVED': 'Non résolu',
    'AUTO_FIX': 'Auto-fix'
  }
  return Object.keys(report.value.statistics.byMethod).map(k => labels[k] || k)
})

const methodData = computed(() => {
  if (!report.value?.statistics?.byMethod) return []
  return Object.values(report.value.statistics.byMethod)
})

// Localisation des librairies - calcul à partir des libraries
const libraryLocationStats = computed(() => {
  const libs = report.value?.libraries || []
  const useArtifactory = isArtifactoryEnabled.value
  const useNexus = isNexusEnabled.value

  const stats = {
    MAVEN_CENTRAL_EXT: 0,
    ARTIFACTORY_INT: 0,
    ARTIFACTORY_EXT: 0,
    ARTIFACTORY_DEBT_INT: 0,
    ARTIFACTORY_DEBT_EXT: 0,
    NEXUS_INT: 0,
    NEXUS_EXT: 0,
    NEXUS_DEBT_INT: 0,
    NEXUS_DEBT_EXT: 0
  }

  libs.forEach(lib => {
    const method = lib.resolutionMethod?.toUpperCase()
    const isInternal = lib.libraryType === 'internal' || lib.isInternal
    const isDebt = lib.isFromDebtRepo === true

    // Maven Central : CHECKSUM, MANIFEST ou KNOWN_CONFIG pour libs externes sans dette
    if (method === 'CHECKSUM' || method === 'MANIFEST') {
      stats.MAVEN_CENTRAL_EXT++
    }
    // KNOWN_CONFIG : classifier selon libraryType et isFromDebtRepo
    else if (method === 'KNOWN_CONFIG') {
      if (isInternal) {
        // Interne connu → repo interne (Artifactory ou Nexus selon contexte)
        if (useNexus) {
          if (isDebt) stats.NEXUS_DEBT_INT++
          else stats.NEXUS_INT++
        } else if (useArtifactory) {
          if (isDebt) stats.ARTIFACTORY_DEBT_INT++
          else stats.ARTIFACTORY_INT++
        }
      } else {
        // Externe connu → Maven Central
        stats.MAVEN_CENTRAL_EXT++
      }
    }
    // Artifactory
    else if (method === 'ARTIFACTORY' || method === 'ARTIFACTORY_CHECKSUM') {
      if (isDebt) {
        if (isInternal) stats.ARTIFACTORY_DEBT_INT++
        else stats.ARTIFACTORY_DEBT_EXT++
      } else {
        if (isInternal) stats.ARTIFACTORY_INT++
        else stats.ARTIFACTORY_EXT++
      }
    }
    // Nexus
    else if (method === 'NEXUS' || method === 'NEXUS_CHECKSUM') {
      if (isDebt) {
        if (isInternal) stats.NEXUS_DEBT_INT++
        else stats.NEXUS_DEBT_EXT++
      } else {
        if (isInternal) stats.NEXUS_INT++
        else stats.NEXUS_EXT++
      }
    }
  })

  return stats
})

// Configuration des catégories de localisation
const locationConfig = computed(() => {
  const useArtifactory = isArtifactoryEnabled.value
  const useNexus = isNexusEnabled.value

  // Catégories avec labels sur 2 lignes, couleurs
  const categories = [
    { key: 'MAVEN_CENTRAL_EXT', label: ['Maven Central', 'Externe'], color: '#3b82f6' }
  ]

  if (useArtifactory) {
    categories.push(
      { key: 'ARTIFACTORY_INT', label: ['Artifactory', 'Interne'], color: '#22c55e' },
      { key: 'ARTIFACTORY_EXT', label: ['Artifactory', 'Externe'], color: '#3b82f6' },
      { key: 'ARTIFACTORY_DEBT_INT', label: ['Artifactory Dettes', 'Interne'], color: '#f97316' },
      { key: 'ARTIFACTORY_DEBT_EXT', label: ['Artifactory Dettes', 'Externe'], color: '#ef4444' }
    )
  } else if (useNexus) {
    categories.push(
      { key: 'NEXUS_INT', label: ['Nexus', 'Interne'], color: '#22c55e' },
      { key: 'NEXUS_EXT', label: ['Nexus', 'Externe'], color: '#3b82f6' },
      { key: 'NEXUS_DEBT_INT', label: ['Nexus Dettes', 'Interne'], color: '#f97316' },
      { key: 'NEXUS_DEBT_EXT', label: ['Nexus Dettes', 'Externe'], color: '#ef4444' }
    )
  }

  return categories
})

const locationLabels = computed(() => {
  return locationConfig.value
    .filter(cat => libraryLocationStats.value[cat.key] > 0)
    .map(cat => cat.label)
})

const locationData = computed(() => {
  return locationConfig.value
    .filter(cat => libraryLocationStats.value[cat.key] > 0)
    .map(cat => libraryLocationStats.value[cat.key])
})

const locationColors = computed(() => {
  return locationConfig.value
    .filter(cat => libraryLocationStats.value[cat.key] > 0)
    .map(cat => cat.color)
})

// Radar chart data
const radarData = computed(() => {
  if (!report.value) return [0, 0, 0, 0, 0]
  const stats = report.value.statistics
  const libs = report.value.libraries || []
  const maxValue = Math.max(
    stats.totalJars,
    stats.resolved,
    libs.filter(l => l.isInternal).length,
    libs.filter(l => !l.isInternal).length,
    stats.byScope?.TEST || 0
  ) || 1

  // Normalize to 0-100 scale
  return [
    (stats.totalJars / maxValue) * 100,
    (stats.resolved / maxValue) * 100,
    (libs.filter(l => l.isInternal).length / maxValue) * 100,
    (libs.filter(l => !l.isInternal).length / maxValue) * 100,
    ((stats.byScope?.TEST || 0) / maxValue) * 100
  ]
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function exportPdf() {
  window.print()
}
</script>

<style scoped>
@media print {
  :deep(.chart-panel) {
    break-inside: avoid;
    page-break-inside: avoid;
  }
}
</style>
