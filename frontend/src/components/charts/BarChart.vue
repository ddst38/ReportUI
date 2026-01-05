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
    <div class="relative" style="height: 220px;">
      <Bar :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

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
  },
  color: {
    type: String,
    default: '#3b82f6'
  },
  horizontal: {
    type: Boolean,
    default: false
  }
})

defineEmits(['menu'])

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [{
    data: props.data,
    backgroundColor: props.color,
    borderColor: props.color,
    borderWidth: 1,
    borderRadius: 4,
    barThickness: 20
  }]
}))

const chartOptions = computed(() => ({
  indexAxis: props.horizontal ? 'y' : 'x',
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
    x: {
      grid: {
        display: !props.horizontal,
        color: '#e5e7eb'
      },
      ticks: {
        font: { size: 9 },
        color: '#6b7280'
      }
    },
    y: {
      grid: {
        display: props.horizontal,
        color: '#e5e7eb'
      },
      ticks: {
        font: { size: 9 },
        color: '#6b7280'
      },
      beginAtZero: true
    }
  }
}))
</script>
