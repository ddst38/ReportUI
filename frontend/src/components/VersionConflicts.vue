<template>
  <div v-if="conflicts.length > 0" class="chart-panel">
    <div class="chart-title">
      <span class="text-amber-600">Conflits de versions ({{ conflicts.length }})</span>
    </div>

    <div class="table-container max-h-[300px] overflow-y-auto">
      <table class="data-table">
        <thead class="sticky top-0 bg-white">
          <tr>
            <th>GroupId</th>
            <th>ArtifactId</th>
            <th>Versions détectées</th>
            <th>Fichiers sources</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="conflict in conflicts" :key="conflict.key" class="bg-amber-50">
            <td class="font-mono text-gray-600">{{ conflict.groupId }}</td>
            <td class="font-mono">{{ conflict.artifactId }}</td>
            <td>
              <div class="flex flex-wrap gap-1">
                <span v-for="ver in conflict.versions" :key="ver"
                      class="badge bg-amber-100 text-amber-800">
                  {{ ver }}
                </span>
              </div>
            </td>
            <td class="text-xs text-gray-500">
              <div class="max-w-xs">
                <span v-for="(file, idx) in conflict.files" :key="file">
                  {{ file }}<span v-if="idx < conflict.files.length - 1">, </span>
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-xs text-amber-600 mt-2">
      Ces dépendances sont présentes en plusieurs versions. Vérifiez le pom.xml généré pour résoudre les conflits.
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  libraries: {
    type: Array,
    required: true
  }
})

const conflicts = computed(() => {
  const grouped = {}

  for (const lib of props.libraries) {
    if (!lib.groupId || !lib.artifactId || !lib.version) continue

    const key = `${lib.groupId}:${lib.artifactId}`
    if (!grouped[key]) {
      grouped[key] = {
        key,
        groupId: lib.groupId,
        artifactId: lib.artifactId,
        versions: new Set(),
        files: []
      }
    }
    grouped[key].versions.add(lib.version)
    grouped[key].files.push(lib.cleanedName || lib.originalName)
  }

  return Object.values(grouped)
    .filter(g => g.versions.size > 1)
    .map(g => ({
      ...g,
      versions: Array.from(g.versions).sort()
    }))
    .sort((a, b) => a.key.localeCompare(b.key))
})
</script>
