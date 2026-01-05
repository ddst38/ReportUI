<template>
  <div class="chart-panel">
    <div class="chart-title">
      <span>{{ title }}</span>
      <button @click="$emit('menu')" class="text-gray-400 hover:text-gray-600">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
        </svg>
      </button>
    </div>
    <div class="relative" style="height: 200px;">
      <Radar :data="chartData" :options="chartOptions" />
    </div>
    <div class="flex justify-center gap-4 mt-2 text-xxs">
      <div class="flex items-center">
        <span class="w-2 h-2 rounded-full mr-1 bg-primary-300"></span>
        <span class="text-gray-600">Métriques</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Radar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend)

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  labels: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    required: true
  }
})

defineEmits(['menu'])

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [{
    label: 'Métriques',
    data: props.data,
    backgroundColor: 'rgba(59, 130, 246, 0.2)',
    borderColor: '#3b82f6',
    borderWidth: 2,
    pointBackgroundColor: '#1e40af',
    pointBorderColor: '#ffffff',
    pointBorderWidth: 2,
    pointRadius: 4
  }]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: '#1e40af',
      titleFont: { size: 11 },
      bodyFont: { size: 10 },
      padding: 8,
      cornerRadius: 4
    }
  },
  scales: {
    r: {
      angleLines: {
        color: '#e5e7eb'
      },
      grid: {
        color: '#e5e7eb'
      },
      pointLabels: {
        font: { size: 9 },
        color: '#6b7280'
      },
      ticks: {
        font: { size: 8 },
        color: '#9ca3af',
        backdropColor: 'transparent'
      },
      beginAtZero: true
    }
  }
}
</script>
