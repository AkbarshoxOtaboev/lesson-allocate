<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-99999 flex justify-end">
      <div
        class="absolute inset-0 bg-gray-400/50 backdrop-blur-[32px] transition-opacity"
        aria-hidden="true"
        @click="$emit('close')"
      />
      <aside
        class="relative flex h-full w-full max-w-md flex-col bg-white shadow-xl dark:bg-gray-900"
        role="dialog"
        aria-modal="true"
        @click.stop
      >
        <div
          class="flex items-center justify-between border-b border-gray-200 px-5 py-4 dark:border-gray-800"
        >
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
            <slot name="title">Ma'lumot</slot>
          </h3>
          <button
            type="button"
            class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-white/5"
            aria-label="Yopish"
            @click="$emit('close')"
          >
            <span class="text-xl leading-none">&times;</span>
          </button>
        </div>
        <div class="flex-1 overflow-y-auto px-5 py-5">
          <slot />
        </div>
      </aside>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineEmits<{ close: [] }>()
</script>
