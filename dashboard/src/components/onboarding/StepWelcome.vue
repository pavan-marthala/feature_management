<script setup lang="ts">
import { Layers, FolderKanban, Flag, Zap, ArrowRight } from 'lucide-vue-next'

const emit = defineEmits<{ next: [] }>()
</script>

<template>
  <div class="welcome">
    <div class="welcome__hero animate-fadeInUp">
      <div class="welcome__icon-row">
        <div class="welcome__floating-card welcome__floating-card--1">
          <Layers :size="24" />
          <span>Environments</span>
        </div>
        <div class="welcome__floating-card welcome__floating-card--2">
          <FolderKanban :size="24" />
          <span>Workspaces</span>
        </div>
        <div class="welcome__floating-card welcome__floating-card--3">
          <Flag :size="24" />
          <span>Features</span>
        </div>
      </div>

      <h1 class="welcome__title">
        <Zap :size="32" class="welcome__title-icon" />
        Welcome to Feature Management
      </h1>
      <p class="welcome__subtitle">
        Set up your environments, workspace, and workflow in under 2 minutes.
      </p>
      <p class="welcome__hint">This guided setup will walk you through everything step by step.</p>

      <button class="welcome__cta" @click="emit('next')">
        Let's Get Started
        <ArrowRight :size="18" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.welcome {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.welcome__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  max-width: 600px;
}

.welcome__icon-row {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.welcome__floating-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 1.25rem 1.5rem;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  font-size: 0.8rem;
  font-weight: 600;
  animation: float 4s ease-in-out infinite;
}

.welcome__floating-card--1 { animation-delay: 0s; color: var(--accent-emerald); }
.welcome__floating-card--2 { animation-delay: 0.5s; color: var(--accent-cyan); }
.welcome__floating-card--3 { animation-delay: 1s; color: var(--accent-indigo); }

.welcome__floating-card span { color: var(--text-secondary); }

.welcome__title {
  font-size: 2.5rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  display: flex;
  align-items: center;
  gap: 12px;
  line-height: 1.2;
}

.welcome__title-icon { color: var(--accent-cyan); }

.welcome__subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.welcome__hint {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.welcome__cta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 1rem;
  padding: 14px 32px;
  background: var(--gradient-accent);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 20px rgba(34, 211, 238, 0.25);
}

.welcome__cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(34, 211, 238, 0.35);
}

@media (max-width: 640px) {
  .welcome__title { font-size: 1.75rem; }
  .welcome__icon-row { gap: 0.75rem; }
  .welcome__floating-card { padding: 1rem; font-size: 0.7rem; }
}
</style>
