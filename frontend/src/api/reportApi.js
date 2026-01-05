import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

export default {
  /**
   * Liste tous les rapports (résumés).
   */
  async listReports() {
    const response = await api.get('/reports')
    return response.data
  },

  /**
   * Récupère un rapport complet par son ID.
   */
  async getReport(id) {
    const response = await api.get(`/reports/${id}`)
    return response.data
  },

  /**
   * Crée un nouveau rapport.
   */
  async createReport(report) {
    const response = await api.post('/reports', report)
    return response.data
  },

  /**
   * Supprime un rapport.
   */
  async deleteReport(id) {
    await api.delete(`/reports/${id}`)
  }
}
