<template>
  <div class="space-y-6">
    <!-- Resume -->
    <div v-if="analysis?.summary" class="bg-white rounded-lg shadow p-4">
      <h3 class="text-lg font-semibold mb-4">Resume de l'analyse structurelle</h3>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div class="text-center p-3 bg-blue-50 rounded-lg">
          <div class="text-2xl font-bold text-blue-600">{{ analysis.summary.totalPackages }}</div>
          <div class="text-xs text-gray-500">Packages</div>
        </div>
        <div class="text-center p-3 bg-gray-50 rounded-lg">
          <div class="text-2xl font-bold text-gray-600">{{ analysis.summary.totalDependencies }}</div>
          <div class="text-xs text-gray-500">Dependances</div>
        </div>
        <div class="text-center p-3 rounded-lg" :class="analysis.summary.cycleCount > 0 ? 'bg-red-50' : 'bg-green-50'">
          <div class="text-2xl font-bold" :class="analysis.summary.cycleCount > 0 ? 'text-red-600' : 'text-green-600'">
            {{ analysis.summary.cycleCount }}
          </div>
          <div class="text-xs text-gray-500">Cycles</div>
        </div>
        <div class="text-center p-3 rounded-lg" :class="analysis.summary.jdkInternalCount > 0 ? 'bg-orange-50' : 'bg-green-50'">
          <div class="text-2xl font-bold" :class="analysis.summary.jdkInternalCount > 0 ? 'text-orange-600' : 'text-green-600'">
            {{ analysis.summary.jdkInternalCount }}
          </div>
          <div class="text-xs text-gray-500">APIs internes JDK</div>
        </div>
        <div class="text-center p-3 bg-purple-50 rounded-lg">
          <div class="text-2xl font-bold text-purple-600">{{ analysis.summary.avgInstability?.toFixed(2) }}</div>
          <div class="text-xs text-gray-500">Instabilite moy.</div>
        </div>
      </div>
    </div>

    <!-- Cycles de dependances -->
    <div v-if="analysis?.cycles?.length > 0" class="bg-white rounded-lg shadow p-4">
      <h3 class="text-lg font-semibold mb-4 text-red-700">
        <svg class="inline w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
        </svg>
        Cycles de dependances detectes ({{ analysis.cycles.length }})
      </h3>
      <div class="space-y-2">
        <div v-for="(cycle, index) in analysis.cycles" :key="index"
             class="p-3 bg-red-50 rounded border border-red-200">
          <div class="font-mono text-sm text-red-800">{{ cycle.cycleString }}</div>
          <div class="text-xs text-red-600 mt-1">Longueur: {{ cycle.length }} packages</div>
        </div>
      </div>
    </div>

    <!-- APIs internes JDK -->
    <div v-if="analysis?.jdkInternalUsages?.length > 0" class="bg-white rounded-lg shadow p-4">
      <h3 class="text-lg font-semibold mb-4 text-orange-700">
        <svg class="inline w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
        </svg>
        Usages d'APIs internes JDK ({{ analysis.jdkInternalUsages.length }})
      </h3>
      <div class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-orange-50">
            <tr>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Classe source</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">API interne</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Module</th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Suggestion</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="(usage, index) in analysis.jdkInternalUsages" :key="index" class="hover:bg-orange-25">
              <td class="px-3 py-2 text-sm font-mono">{{ usage.sourceClass }}</td>
              <td class="px-3 py-2 text-sm font-mono text-orange-600">{{ usage.internalApi }}</td>
              <td class="px-3 py-2 text-sm">{{ usage.module }}</td>
              <td class="px-3 py-2 text-sm text-green-700">{{ usage.suggestion || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Metriques par package -->
    <div v-if="analysis?.packageMetrics?.length > 0" class="bg-white rounded-lg shadow p-4">
      <h3 class="text-lg font-semibold mb-4">
        Metriques de couplage par package
        <span class="text-sm font-normal text-gray-500">(tries par instabilite)</span>
      </h3>

      <!-- Filtres -->
      <div class="flex flex-wrap gap-4 mb-4">
        <input v-model="packageFilter" type="text" placeholder="Filtrer les packages..."
               class="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:ring-primary-500 focus:border-primary-500">
        <label class="flex items-center gap-2 text-sm">
          <input type="checkbox" v-model="showUnstableOnly" class="rounded text-primary-600">
          Instables uniquement (I > 0.7)
        </label>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 cursor-pointer hover:bg-gray-100"
                  @click="sortBy('packageName')">
                Package {{ sortIcon('packageName') }}
              </th>
              <th class="px-3 py-2 text-center text-xs font-medium text-gray-500 cursor-pointer hover:bg-gray-100"
                  @click="sortBy('afferentCoupling')"
                  title="Afferent Coupling - Packages qui dependent de celui-ci">
                Ca {{ sortIcon('afferentCoupling') }}
              </th>
              <th class="px-3 py-2 text-center text-xs font-medium text-gray-500 cursor-pointer hover:bg-gray-100"
                  @click="sortBy('efferentCoupling')"
                  title="Efferent Coupling - Packages dont celui-ci depend">
                Ce {{ sortIcon('efferentCoupling') }}
              </th>
              <th class="px-3 py-2 text-center text-xs font-medium text-gray-500 cursor-pointer hover:bg-gray-100"
                  @click="sortBy('instability')"
                  title="Instabilite I = Ce/(Ca+Ce). 0=stable, 1=instable">
                Instabilite {{ sortIcon('instability') }}
              </th>
              <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Details</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="(pkg, index) in filteredMetrics" :key="index"
                class="hover:bg-gray-50"
                :class="{ 'bg-red-50': pkg.instability > 0.8 }">
              <td class="px-3 py-2 text-sm font-mono">{{ pkg.packageName }}</td>
              <td class="px-3 py-2 text-sm text-center">
                <span class="inline-block w-8 text-center px-2 py-0.5 rounded bg-blue-100 text-blue-800">
                  {{ pkg.afferentCoupling }}
                </span>
              </td>
              <td class="px-3 py-2 text-sm text-center">
                <span class="inline-block w-8 text-center px-2 py-0.5 rounded bg-purple-100 text-purple-800">
                  {{ pkg.efferentCoupling }}
                </span>
              </td>
              <td class="px-3 py-2 text-sm text-center">
                <span class="inline-block w-12 text-center px-2 py-0.5 rounded"
                      :class="instabilityClass(pkg.instability)">
                  {{ pkg.instability?.toFixed(2) }}
                </span>
              </td>
              <td class="px-3 py-2 text-sm">
                <button @click="toggleDetails(index)"
                        class="text-primary-600 hover:text-primary-800 text-xs">
                  {{ expandedRows.includes(index) ? 'Masquer' : 'Voir details' }}
                </button>
              </td>
            </tr>
            <!-- Details row -->
            <tr v-for="(pkg, index) in filteredMetrics" :key="'details-' + index"
                v-show="expandedRows.includes(index)">
              <td colspan="5" class="px-3 py-2 bg-gray-50">
                <div class="grid md:grid-cols-2 gap-4 text-xs">
                  <div>
                    <div class="font-semibold text-gray-700 mb-1">Depend de ({{ pkg.dependsOn?.length || 0 }}):</div>
                    <div class="font-mono text-gray-600">
                      {{ pkg.dependsOn?.join(', ') || 'Aucun' }}
                    </div>
                  </div>
                  <div>
                    <div class="font-semibold text-gray-700 mb-1">Utilise par ({{ pkg.usedBy?.length || 0 }}):</div>
                    <div class="font-mono text-gray-600">
                      {{ pkg.usedBy?.join(', ') || 'Aucun' }}
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Legende -->
      <div class="mt-4 text-xs text-gray-500">
        <strong>Legende:</strong>
        Ca = Couplage afferent (packages qui dependent de celui-ci),
        Ce = Couplage efferent (packages dont celui-ci depend),
        I = Instabilite (0 = stable, 1 = instable)
      </div>
    </div>

    <!-- Message si pas de donnees -->
    <div v-if="!analysis" class="text-center py-8 text-gray-500">
      Aucune analyse jdeps disponible pour ce projet.
      <br>
      <span class="text-sm">Utilisez l'option --jdeps-analysis lors de la migration.</span>
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

const packageFilter = ref('')
const showUnstableOnly = ref(false)
const sortColumn = ref('instability')
const sortDirection = ref('desc')
const expandedRows = ref([])

const filteredMetrics = computed(() => {
  let metrics = props.analysis?.packageMetrics || []

  // Filtre texte
  if (packageFilter.value) {
    const filter = packageFilter.value.toLowerCase()
    metrics = metrics.filter(m => m.packageName?.toLowerCase().includes(filter))
  }

  // Filtre instable
  if (showUnstableOnly.value) {
    metrics = metrics.filter(m => m.instability > 0.7)
  }

  // Tri
  metrics = [...metrics].sort((a, b) => {
    const aVal = a[sortColumn.value] ?? 0
    const bVal = b[sortColumn.value] ?? 0
    if (typeof aVal === 'string') {
      return sortDirection.value === 'asc'
        ? aVal.localeCompare(bVal)
        : bVal.localeCompare(aVal)
    }
    return sortDirection.value === 'asc' ? aVal - bVal : bVal - aVal
  })

  return metrics
})

function sortBy(column) {
  if (sortColumn.value === column) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortColumn.value = column
    sortDirection.value = column === 'packageName' ? 'asc' : 'desc'
  }
}

function sortIcon(column) {
  if (sortColumn.value !== column) return ''
  return sortDirection.value === 'asc' ? '▲' : '▼'
}

function instabilityClass(value) {
  if (value > 0.8) return 'bg-red-100 text-red-800'
  if (value > 0.5) return 'bg-orange-100 text-orange-800'
  if (value > 0.3) return 'bg-yellow-100 text-yellow-800'
  return 'bg-green-100 text-green-800'
}

function toggleDetails(index) {
  const pos = expandedRows.value.indexOf(index)
  if (pos >= 0) {
    expandedRows.value.splice(pos, 1)
  } else {
    expandedRows.value.push(index)
  }
}
</script>
