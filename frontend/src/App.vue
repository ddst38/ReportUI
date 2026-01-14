<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Header -->
    <header class="bg-primary-800 text-white shadow-lg">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <router-link to="/" class="flex items-center space-x-2">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <span class="font-bold text-lg">DGD Migration Reports</span>
        </router-link>
        <nav class="flex items-center space-x-4 text-sm">
          <router-link to="/" class="hover:text-primary-200 transition-colors">
            Rapports
          </router-link>
          <a href="/swagger-ui.html" target="_blank"
             class="hover:text-primary-200 transition-colors flex items-center">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"/>
            </svg>
            API
          </a>
        </nav>
      </div>
    </header>

    <!-- Main content -->
    <main class="max-w-7xl mx-auto px-4 py-6">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="bg-gray-800 text-gray-400 text-xs py-3 mt-8">
      <div class="max-w-7xl mx-auto px-4 text-center">
        DGD Migration Reports - Rapports de migration ant2maven
      </div>
    </footer>
  </div>
</template>

<script setup>
</script>
