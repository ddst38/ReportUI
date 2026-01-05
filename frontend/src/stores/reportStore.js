import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import reportApi from '@/api/reportApi'

export const useReportStore = defineStore('reports', () => {
  // State
  const reports = ref([])
  const currentReport = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Getters
  const reportsCount = computed(() => reports.value.length)
  const hasReports = computed(() => reports.value.length > 0)

  // Actions
  async function fetchReports() {
    loading.value = true
    error.value = null
    try {
      reports.value = await reportApi.listReports()
    } catch (e) {
      error.value = e.message
      console.error('Erreur chargement rapports:', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchReport(id) {
    loading.value = true
    error.value = null
    try {
      currentReport.value = await reportApi.getReport(id)
    } catch (e) {
      error.value = e.message
      console.error('Erreur chargement rapport:', e)
    } finally {
      loading.value = false
    }
  }

  async function deleteReport(id) {
    try {
      await reportApi.deleteReport(id)
      reports.value = reports.value.filter(r => r.id !== id)
      if (currentReport.value?.id === id) {
        currentReport.value = null
      }
    } catch (e) {
      error.value = e.message
      console.error('Erreur suppression rapport:', e)
      throw e
    }
  }

  function clearCurrentReport() {
    currentReport.value = null
  }

  return {
    // State
    reports,
    currentReport,
    loading,
    error,
    // Getters
    reportsCount,
    hasReports,
    // Actions
    fetchReports,
    fetchReport,
    deleteReport,
    clearCurrentReport
  }
})
