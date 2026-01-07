<template>
  <div v-if="packages && packages.length > 0" class="chart-panel border-red-200">
    <div class="chart-title">
      <span class="text-red-600">Packages manquants ({{ packages.length }})</span>
    </div>

    <div class="bg-red-50 border border-red-200 rounded p-3 mb-3">
      <p class="text-sm text-red-700">
        Ces packages ou classes n'ont pas pu être résolus. Le projet ne compile pas.
      </p>
    </div>

    <div class="table-container max-h-[300px] overflow-y-auto">
      <table class="data-table">
        <thead class="sticky top-0 bg-white">
          <tr>
            <th>Type</th>
            <th>Nom complet</th>
            <th>Package</th>
            <th>Fichier source</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(pkg, idx) in packages" :key="idx" class="bg-red-50">
            <td>
              <span :class="typeBadgeClass(pkg.type)">{{ pkg.type }}</span>
            </td>
            <td class="font-mono text-sm">
              <span class="truncate max-w-xs block" :title="pkg.name">
                {{ truncate(pkg.name, 50) }}
              </span>
            </td>
            <td class="font-mono text-sm text-gray-600">
              {{ pkg.packageName }}
            </td>
            <td class="text-sm text-gray-500">
              {{ pkg.sourceFile || '-' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-xs text-red-600 mt-2">
      <strong>Action suggérée :</strong> Ajoutez les JARs manquants dans <code>lib-provided/</code> puis relancez avec <code>--auto-fix</code>
    </p>
  </div>
</template>

<script setup>
defineProps({
  packages: {
    type: Array,
    default: () => []
  }
})

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}

function typeBadgeClass(type) {
  return type === 'CLASS'
    ? 'badge bg-orange-100 text-orange-800'
    : 'badge bg-red-100 text-red-800'
}
</script>
