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
      <Doughnut :data="chartData" :options="chartOptions" />
    </div>
    <div class="flex flex-wrap justify-center gap-3 mt-3 text-xxs">
      <div v-for="(item, index) in legendItems" :key="index" class="flex items-center">
        <span class="w-2 h-2 rounded-full mr-1" :style="{ backgroundColor: item.color }"></span>
        <span class="text-gray-600">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

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
  colors: {
    type: Array,
    default: () => ['#1e40af', '#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe']
  }
})

defineEmits(['menu'])

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [{
    data: props.data,
    backgroundColor: props.colors.slice(0, props.data.length),
    borderColor: '#ffffff',
    borderWidth: 2,
    hoverOffset: 4
  }]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '60%',
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
  }
}

const legendItems = computed(() =>
  props.labels.map((label, index) => ({
    label,
    color: props.colors[index] || props.colors[props.colors.length - 1]
  }))
)
</script>
