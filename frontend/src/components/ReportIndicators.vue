<template>
  <div class="flex flex-wrap items-center gap-1.5">
    <!-- Auto-fix activé -->
    <span v-if="autoFixEnabled" class="indicator" title="Auto-fix activé">
      <svg class="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
      </svg>
    </span>

    <!-- Mode LOCAL -->
    <span v-if="deploymentMode === 'LOCAL'" class="indicator" title="Mode LOCAL">
      <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
      </svg>
    </span>

    <!-- Mode REMOTE -->
    <span v-if="deploymentMode === 'REMOTE'" class="indicator" title="Mode REMOTE">
      <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z"/>
      </svg>
    </span>

    <!-- Compilation réussie -->
    <span v-if="compilationSuccess === true" class="indicator" title="Compilation réussie">
      <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
      </svg>
    </span>

    <!-- Compilation échouée -->
    <span v-if="compilationSuccess === false" class="indicator" title="Compilation échouée">
      <svg class="w-4 h-4 text-red-600" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
      </svg>
    </span>

    <!-- Niveau risque CVE -->
    <span v-if="cveSeverity && cveSeverity !== 'NONE'" class="indicator" :title="'Risque CVE: ' + cveSeverityLabel">
      <svg :class="['w-4 h-4', cveSeverityColor]" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2A11.954 11.954 0 0110 1.944zM10 6a1 1 0 011 1v2a1 1 0 11-2 0V7a1 1 0 011-1zm0 6a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
      </svg>
    </span>

    <!-- Artifactory activé -->
    <span v-if="artifactoryEnabled" class="indicator indicator-letter bg-orange-100 text-orange-700" title="Artifactory">
      A
    </span>

    <!-- Nexus activé -->
    <span v-if="nexusEnabled" class="indicator indicator-letter bg-blue-100 text-blue-700" title="Nexus">
      N
    </span>

    <!-- Librairies remontées -->
    <span v-if="librariesUploaded > 0" class="indicator" :title="librariesUploaded + ' librairies remontées'">
      <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
      </svg>
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  autoFixEnabled: {
    type: Boolean,
    default: false
  },
  deploymentMode: {
    type: String,
    default: null
  },
  compilationSuccess: {
    type: Boolean,
    default: null
  },
  cveSeverity: {
    type: String,
    default: null
  },
  artifactoryEnabled: {
    type: Boolean,
    default: false
  },
  nexusEnabled: {
    type: Boolean,
    default: false
  },
  librariesUploaded: {
    type: Number,
    default: 0
  }
})

const cveSeverityColor = computed(() => {
  const colors = {
    'CRITICAL': 'text-red-800',
    'HIGH': 'text-red-600',
    'MEDIUM': 'text-orange-500',
    'LOW': 'text-yellow-500'
  }
  return colors[props.cveSeverity] || 'text-gray-400'
})

const cveSeverityLabel = computed(() => {
  const labels = {
    'CRITICAL': 'Critique',
    'HIGH': 'Haute',
    'MEDIUM': 'Moyenne',
    'LOW': 'Basse',
    'NONE': 'Aucune'
  }
  return labels[props.cveSeverity] || props.cveSeverity
})
</script>

<style scoped>
.indicator {
  @apply inline-flex items-center justify-center;
}

.indicator-letter {
  @apply w-5 h-5 rounded text-xs font-bold;
}
</style>
