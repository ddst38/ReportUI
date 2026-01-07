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
                <span v-for="versionInfo in conflict.versionDetails" :key="versionInfo.version"
                      :class="versionBadgeClass(versionInfo.isLoaded)">
                  {{ versionInfo.version }}
                  <span v-if="versionInfo.isLoaded"> ✓</span>
                </span>
              </div>
            </td>
            <td class="text-xs">
              <div class="max-w-xs flex flex-wrap gap-1">
                <span v-for="fileInfo in conflict.fileDetails" :key="fileInfo.file"
                      :class="fileBadgeClass(fileInfo.isLoaded)">
                  {{ fileInfo.file }}
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-xs text-amber-600 mt-2">
      <strong>Vert</strong> = version chargée dans le classpath (dernière déclarée dans le POM).
      <strong>Rouge</strong> = version non chargée.
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

  // Indexer toutes les bibliothèques pour retrouver leur position
  props.libraries.forEach((lib, index) => {
    if (!lib.groupId || !lib.artifactId || !lib.version) return

    const key = `${lib.groupId}:${lib.artifactId}`
    if (!grouped[key]) {
      grouped[key] = {
        key,
        groupId: lib.groupId,
        artifactId: lib.artifactId,
        entries: []
      }
    }
    grouped[key].entries.push({
      version: lib.version,
      file: lib.cleanedName || lib.originalName,
      index: index
    })
  })

  return Object.values(grouped)
    .filter(g => {
      // Vérifier qu'il y a au moins 2 versions différentes
      const uniqueVersions = new Set(g.entries.map(e => e.version))
      return uniqueVersions.size > 1
    })
    .map(g => {
      // Trouver l'entrée avec l'index le plus élevé (version chargée)
      const maxIndex = Math.max(...g.entries.map(e => e.index))
      const loadedEntry = g.entries.find(e => e.index === maxIndex)
      const loadedVersion = loadedEntry?.version

      // Créer les détails de versions avec indication loaded/not-loaded
      const versionMap = new Map()
      for (const entry of g.entries) {
        if (!versionMap.has(entry.version)) {
          versionMap.set(entry.version, {
            version: entry.version,
            isLoaded: entry.version === loadedVersion,
            maxIndex: entry.index
          })
        } else {
          // Mettre à jour maxIndex si cette version apparaît plus tard
          const existing = versionMap.get(entry.version)
          if (entry.index > existing.maxIndex) {
            existing.maxIndex = entry.index
            existing.isLoaded = entry.version === loadedVersion
          }
        }
      }

      // Créer les détails de fichiers avec indication loaded/not-loaded
      const fileDetails = g.entries.map(entry => ({
        file: entry.file,
        version: entry.version,
        isLoaded: entry.version === loadedVersion
      }))

      return {
        key: g.key,
        groupId: g.groupId,
        artifactId: g.artifactId,
        versionDetails: Array.from(versionMap.values()).sort((a, b) =>
          a.version.localeCompare(b.version)
        ),
        fileDetails: fileDetails
      }
    })
    .sort((a, b) => a.key.localeCompare(b.key))
})

function versionBadgeClass(isLoaded) {
  return isLoaded
    ? 'badge bg-green-100 text-green-800'
    : 'badge bg-red-100 text-red-800'
}

function fileBadgeClass(isLoaded) {
  return isLoaded
    ? 'px-1 py-0.5 rounded bg-green-50 text-green-700'
    : 'px-1 py-0.5 rounded bg-red-50 text-red-700'
}
</script>
