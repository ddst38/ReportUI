<template>
  <div class="chart-panel">
    <h3 class="text-sm font-semibold text-gray-700 mb-3">Localisation des librairies</h3>
    <div class="space-y-3">
      <div v-for="item in chartItems" :key="item.key" class="flex items-center gap-3">
        <div class="w-28 text-xs text-gray-600 truncate" :title="item.label">{{ item.label }}</div>
        <div class="flex-1 bg-gray-200 rounded-full h-4 overflow-hidden">
          <div
            class="h-full rounded-full transition-all duration-500"
            :style="{ width: item.percentage + '%', backgroundColor: item.color }"
          ></div>
        </div>
        <div class="w-12 text-right text-xs font-medium" :style="{ color: item.color }">
          {{ item.count }}
        </div>
      </div>
    </div>
    <div class="flex flex-wrap justify-center gap-4 mt-4 pt-3 border-t border-gray-200">
      <div v-for="item in legendItems" :key="item.key" class="flex items-center gap-1.5">
        <span class="w-3 h-3 rounded-sm" :style="{ backgroundColor: item.color }"></span>
        <span class="text-xs text-gray-600">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  byLocation: {
    type: Object,
    default: () => ({})
  }
})

// Couleurs par catégorie
const locationColors = {
  MAVEN_CENTRAL: '#3b82f6',  // Bleu
  INTERNAL_REPO: '#22c55e',  // Vert
  DEBT_REPO: '#f97316',      // Orange
  LOCAL: '#6b7280'           // Gris
}

const locationLabels = {
  MAVEN_CENTRAL: 'Maven Central',
  INTERNAL_REPO: 'Repos internes',
  DEBT_REPO: 'Dette technique',
  LOCAL: 'Installation locale'
}

const legendItems = [
  { key: 'MAVEN_CENTRAL', label: 'Maven Central', color: locationColors.MAVEN_CENTRAL },
  { key: 'INTERNAL_REPO', label: 'Repos internes', color: locationColors.INTERNAL_REPO },
  { key: 'DEBT_REPO', label: 'Dette technique', color: locationColors.DEBT_REPO },
  { key: 'LOCAL', label: 'Installation locale', color: locationColors.LOCAL }
]

const chartItems = computed(() => {
  const data = props.byLocation || {}
  const total = Object.values(data).reduce((sum, val) => sum + (val || 0), 0)

  // Ordre d'affichage
  const order = ['MAVEN_CENTRAL', 'INTERNAL_REPO', 'DEBT_REPO', 'LOCAL']

  return order
    .filter(key => data[key] > 0)
    .map(key => ({
      key,
      label: locationLabels[key] || key,
      count: data[key] || 0,
      percentage: total > 0 ? Math.round((data[key] || 0) / total * 100) : 0,
      color: locationColors[key] || '#6b7280'
    }))
})
</script>
