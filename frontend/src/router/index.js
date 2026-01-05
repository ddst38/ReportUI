import { createRouter, createWebHistory } from 'vue-router'
import ReportList from '@/components/ReportList.vue'
import ReportDetail from '@/components/ReportDetail.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: ReportList
  },
  {
    path: '/reports/:id',
    name: 'report-detail',
    component: ReportDetail,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
