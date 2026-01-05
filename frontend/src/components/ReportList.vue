<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-bold text-gray-800">Rapports de migration</h1>
      <div class="text-sm text-gray-500">
        {{ reportsCount }} rapport(s)
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

    <!-- Reports grid -->
    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div v-for="report in reports" :key="report.id"
           class="bg-white rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer"
           @click="goToReport(report.id)">
        <div class="p-4">
          <!-- Header -->
          <div class="flex items-start justify-between mb-3">
            <div>
              <h3 class="font-semibold text-gray-900 text-sm">{{ report.projectName }}</h3>
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

          <!-- Stats -->
          <div class="grid grid-cols-3 gap-2 mb-3">
            <div class="text-center p-2 bg-gray-50 rounded">
              <div class="text-lg font-bold text-gray-700">{{ report.totalJars }}</div>
              <div class="text-xxs text-gray-500">Total</div>
            </div>
            <div class="text-center p-2 bg-green-50 rounded">
              <div class="text-lg font-bold text-green-600">{{ report.resolved }}</div>
              <div class="text-xxs text-gray-500">Résolus</div>
            </div>
            <div class="text-center p-2 bg-yellow-50 rounded">
              <div class="text-lg font-bold text-yellow-600">{{ report.unresolved }}</div>
              <div class="text-xxs text-gray-500">Non résolus</div>
            </div>
          </div>

          <!-- Progress bar -->
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: report.successRate + '%' }"></div>
          </div>
          <div class="text-right text-xxs text-gray-500 mt-1">
            {{ report.successRate.toFixed(1) }}% de succès
          </div>
        </div>
      </div>
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

const router = useRouter()
const store = useReportStore()

const showDeleteModal = ref(false)
const reportToDelete = ref(null)

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
</script>
