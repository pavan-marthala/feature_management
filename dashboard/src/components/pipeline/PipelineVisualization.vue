<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import {
  Check,
  ChevronRight,
  ChevronDown,
  GripVertical,
  Pencil,
  Trash2,
  Zap,
  Calendar,
  Shield,
  ArrowRight,
  Loader2
} from 'lucide-vue-next'
import type { Stage, StageType } from '@/types'

const props = defineProps<{
  stages: Stage[]
  currentStageId?: string
  mode: 'BUILDER' | 'OPERATIONAL'
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'add'): void
  (e: 'edit', stage: Stage): void
  (e: 'delete', stage: Stage): void
  (e: 'reorder', stages: Stage[]): void
  (e: 'promote'): void
}>()

// Internal copy for drag reorder in builder mode
const localStages = ref<Stage[]>([])
watch(() => props.stages, (val) => {
  localStages.value = [...val]
}, { immediate: true, deep: true })

// --- Dynamic container-width row calculation ---
const containerRef = ref<HTMLElement | null>(null)
const containerWidth = ref(600)

let ro: ResizeObserver | null = null

function measure() {
  if (!containerRef.value) return
  containerWidth.value = containerRef.value.clientWidth
}

onMounted(() => {
  if (containerRef.value) {
    ro = new ResizeObserver(measure)
    ro.observe(containerRef.value)
    measure()
  }
})
onBeforeUnmount(() => { ro?.disconnect() })

// --- Orchestration Row Builder ---
// Rows are built as a single continuous orchestration flow.
// Direction alternates: LTR, RTL, LTR, RTL...
// Data is reversed in JS for RTL rows — NO CSS flex-direction reversal.
// Vertical transition connectors attach to the logical continuation node.

interface OrcRow {
  idx: number
  direction: 'LTR' | 'RTL'
  stages: (Stage & { gi: number })[]
}

const CARD_W = 156
const GAP_W = 44

const maxPerRow = computed(() => {
  const available = containerWidth.value - 32 // padding
  return Math.max(2, Math.floor((available + GAP_W) / (CARD_W + GAP_W)))
})

// Fixed row width ensures all rows occupy the same horizontal space
// for consistent vertical connector alignment across the entire graph
const maxRowWidth = computed(() => {
  return maxPerRow.value * CARD_W + (maxPerRow.value - 1) * GAP_W
})

const pipelineRows = computed((): OrcRow[] => {
  const total = localStages.value.length
  if (total === 0) return []

  const perRow = maxPerRow.value

  // Orchestration-aware row building:
  // Fill rows to capacity except prevent single-orphan last rows.
  // If last row would have 1 item and there are multiple rows, steal one from previous.
  const rowSizes: number[] = []
  let remaining = total
  while (remaining > 0) {
    const take = Math.min(perRow, remaining)
    rowSizes.push(take)
    remaining -= take
  }

  // Prevent orphan: if last row has 1 and prev row has > 2, rebalance
  if (rowSizes.length > 1) {
    const lastIdx = rowSizes.length - 1
    if (rowSizes[lastIdx] === 1 && rowSizes[lastIdx - 1]! > 2) {
      rowSizes[lastIdx - 1]!--
      rowSizes[lastIdx]!++
    }
  }

  const rows: OrcRow[] = []
  let cursor = 0
  for (let ri = 0; ri < rowSizes.length; ri++) {
    const size = rowSizes[ri]!
    const direction: 'LTR' | 'RTL' = ri % 2 === 0 ? 'LTR' : 'RTL'
    const slice = localStages.value.slice(cursor, cursor + size)
    const withMeta = slice.map((s, j) => ({ ...s, gi: cursor + j }))

    // RTL rows: reverse data order so rendering is always left-to-right in DOM
    const stages = direction === 'RTL' ? [...withMeta].reverse() : withMeta
    rows.push({ idx: ri, direction, stages })
    cursor += size
  }
  return rows
})

const activeStage = computed(() => {
  if (props.mode !== 'OPERATIONAL') return null

  const currentIndex = props.stages.findIndex(
    s => s.environmentId === props.currentStageId
  )

  const current = props.stages[currentIndex]
  if (!current) return null

  return {
    current,
    next: props.stages[currentIndex + 1] || null,
    currentIndex
  }
})

// --- Drag & Drop ---
const dragIdx = ref<number | null>(null)

function onDragStart(gi: number) {
  if (props.mode !== 'BUILDER') return
  dragIdx.value = gi
}
function onDragOver(e: DragEvent, gi: number) {
  if (props.mode !== 'BUILDER' || dragIdx.value === null) return
  e.preventDefault()
  if (dragIdx.value === gi) return
  const items = [...localStages.value]
  const item = items.splice(dragIdx.value, 1)[0]!
  items.splice(gi, 0, item)
  localStages.value = items
  dragIdx.value = gi
}
function onDrop() {
  if (props.mode !== 'BUILDER') return
  dragIdx.value = null
  emit('reorder', localStages.value)
}

// --- Status helpers ---
function status(gi: number): string {
  if (props.mode !== 'OPERATIONAL') return 'default'

  if (!props.currentStageId) {
    return 'upcoming'
  }

  const ci = props.stages.findIndex(
    s => s.environmentId === props.currentStageId
  )
  if (ci === -1) return 'upcoming'
  if (gi < ci) return 'completed'
  if (gi === ci) return 'active'
  return 'upcoming'
}

function icon(t: StageType) {
  return t === 'AUTOMATIC' ? Zap : t === 'SCHEDULED' ? Calendar : Shield
}

function typeLabel(t: StageType) {
  return t === 'AUTOMATIC' ? 'Auto' : t === 'SCHEDULED' ? 'Scheduled' : 'Manual'
}

// Connector status: reflects the stage the connector leads FROM in pipeline order
function connectorStatus(row: OrcRow, ci: number): string {
  // In LTR rows, connector before ci leads FROM ci-1
  // In RTL rows (data reversed), connector before ci leads FROM ci-1 in render order,
  // but we need the pipeline-order "from" stage.
  // Since RTL data is reversed, render index ci-1 has a HIGHER gi than ci.
  // The connector leads FROM the higher gi stage TO the lower gi stage visually,
  // but in pipeline order it leads FROM lower gi TO higher gi.
  // So we use the MIN gi of the two adjacent stages as the "from" status.
  const a = row.stages[ci - 1]!.gi
  const b = row.stages[ci]!.gi
  return status(Math.min(a, b))
}

// Transition node: the stage card from which the vertical connector drops to next row
function isTransitionNode(row: OrcRow, ci: number): boolean {
  if (row.idx >= pipelineRows.value.length - 1) return false
  // LTR: last rendered item (rightmost) is transition
  // RTL: first rendered item (rightmost, since data is reversed) is transition
  // Since RTL data is reversed in JS, the continuation node is the first item (ci===0)
  // because it has the highest gi and sits at the right visually (rendered left-to-right
  // but represents rightmost pipeline position)
  // Wait — RTL rows are reversed data rendered left-to-right.
  // Visually: [stageF, stageE, stageD] rendered left to right.
  // stageD is rightmost in DOM = rightmost visually. NO — stageF is ci=0 = leftmost.
  // Actually with reversed data [F,E,D] rendered LTR: F is left, D is right.
  // Pipeline flow for RTL row goes from right (D=lowest gi) to left (F=highest gi).
  // The next row (LTR) starts at the left.
  // So continuation drops from the LEFTMOST card of RTL row (F, ci=0) which is the
  // last in pipeline order (highest gi).
  // For LTR rows: continuation drops from rightmost (last item, ci=length-1).
  if (row.direction === 'LTR') return ci === row.stages.length - 1
  return ci === 0
}

</script>

<template>
  <div ref="containerRef" class="pl" :class="'pl--' + mode.toLowerCase()">

    <!-- ====== DESKTOP / TABLET ORCHESTRATION GRAPH ====== -->
    <div class="pl-desk" :style="{ '--row-w': maxRowWidth + 'px' }">
      <div
        v-for="row in pipelineRows"
        :key="row.idx"
        class="pl-row"
        :class="'pl-row--' + row.direction.toLowerCase()"
      >
        <template v-for="(stage, ci) in row.stages" :key="stage.id || stage.gi">
          <!-- horizontal connector (between cards) -->
          <div v-if="ci > 0" class="cn-h" :class="'cn--' + connectorStatus(row, ci)">
            <div class="cn-h__line"></div>
            <ChevronRight :size="13" class="cn-h__tip" />
          </div>

          <!-- stage card wrapper -->
          <div
            class="sw"
            :class="['sw--' + status(stage.gi), { 'sw--drag': dragIdx === stage.gi }]"
            :draggable="mode === 'BUILDER'"
            @dragstart="onDragStart(stage.gi)"
            @dragover="onDragOver($event, stage.gi)"
            @drop="onDrop"
          >
            <div class="sc" :class="{ 'sc--active': status(stage.gi) === 'active' }">
              <div v-if="mode === 'BUILDER'" class="sc__grip"><GripVertical :size="12" /></div>

              <div class="sc__body">
                <div class="sc__top">
                  <div class="sc__dot">
                    <Check v-if="status(stage.gi) === 'completed'" :size="10" />
                    <component :is="icon(stage.type)" v-else :size="10" />
                  </div>
                  <span class="sc__name">{{ stage.environmentName || 'Stage' }}</span>
                  <div v-if="mode === 'BUILDER'" class="sc__btns">
                    <button class="ib" @click="emit('edit', stage)" title="Edit"><Pencil :size="10" /></button>
                    <button class="ib ib--del" @click="emit('delete', stage)" title="Delete"><Trash2 :size="10" /></button>
                  </div>
                </div>

                <span class="sc__type">{{ typeLabel(stage.type) }}</span>

                <!-- <div
                  v-if="mode === 'OPERATIONAL' && status(stage.gi) === 'active' && stage.gi < stages.length - 1"
                  class="sc__promo"
                >
                  <button class="promo" @click="emit('promote')" :disabled="loading">
                    <Loader2 v-if="loading" :size="10" class="spin" />
                    <template v-else>
                      <span class="promo__label">Promote to {{ stages[stage.gi + 1]?.environmentName }}</span>
                      <ArrowRight :size="10" />
                    </template>
                  </button>
                </div> -->
              </div>

              <div v-if="status(stage.gi) === 'active'" class="sc__pulse"></div>
            </div>

            <!-- vertical transition connector to next row -->
            <div
              v-if="isTransitionNode(row, ci)"
              class="cn-v"
              :class="'cn--' + status(stage.gi)"
            >
              <div class="cn-v__line"></div>
              <ChevronDown :size="13" class="cn-v__tip" />
            </div>
          </div>
        </template>
      </div>
    </div>
    <!-- Operational Action Panel -->
<div
  v-if="mode === 'OPERATIONAL' && activeStage"
  class="op-panel"
>
  <div class="op-panel__info">
    <div class="op-panel__badge">
      Current Environment
    </div>

    <div class="op-panel__env">
      {{ activeStage.current.environmentName }}
    </div>

    <div
      v-if="activeStage.next"
      class="op-panel__next"
    >
      Next Target:
      <strong>{{ activeStage.next.environmentName }}</strong>
    </div>
  </div>

  <button
    v-if="activeStage.next"
    class="op-panel__btn"
    @click="emit('promote')"
    :disabled="loading"
  >
      <Loader2
      v-if="loading"
      :size="14"
      class="spin"
    />

    <template v-else>
      Promote to {{ activeStage.next.environmentName }}
      <ArrowRight :size="14" />
    </template>
  </button>
</div>

    <!-- ====== MOBILE COMPACT TIMELINE ====== -->
    <div class="pl-mob">
      <div
        v-for="(stage, i) in localStages"
        :key="stage.id || i"
        class="mn"
        :class="'mn--' + status(i)"
      >
        <div class="mn__dot">
          <Check v-if="status(i) === 'completed'" :size="10" />
          <component :is="icon(stage.type)" v-else :size="10" />
        </div>

        <div class="mn__body">
          <span class="mn__name">{{ stage.environmentName || 'Stage' }}</span>
          <span class="mn__type">{{ typeLabel(stage.type) }}</span>
        </div>

        <button
          v-if="mode === 'OPERATIONAL' && status(i) === 'active' && i < stages.length - 1"
          class="mn__promo"
          @click="emit('promote')"
          :disabled="loading"
        >
          <Loader2 v-if="loading" :size="12" class="spin" />
          <template v-else>Promote to {{ stages[i + 1]?.environmentName }} <ArrowRight :size="12" /></template>
        </button>

        <div v-if="mode === 'BUILDER'" class="mn__acts">
          <button class="ib" @click="emit('edit', stage)"><Pencil :size="11" /></button>
          <button class="ib ib--del" @click="emit('delete', stage)"><Trash2 :size="11" /></button>
        </div>

        <div v-if="i < localStages.length - 1" class="mn__line" :class="'mn__line--' + status(i)"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== Root ===== */
.pl { width: 100%; padding: 0.5rem 0; }

/* ===== Desktop / Tablet Orchestration Graph ===== */
.pl-desk {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
  width: 100%;
  overflow-x: hidden;
  padding: 1rem 0;
}

.pl-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  width: 100%;
  max-width: var(--row-w);
  margin: 0 auto;
  position: relative;
}

.pl-row--ltr,
.pl-row--rtl {
  justify-content: center;
}

/* RTL visual indicator: flip connector chevrons */
.pl-row--rtl .cn-h__tip {
  transform: scaleX(-1);
}

/* ===== Stage wrapper ===== */
.sw {
  position: relative;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.sw--drag {
  opacity: 0.35;
  transform: scale(0.9);
}

/* ===== Stage card ===== */
.sc {
  width: 172px;
  min-height: 70px;
  padding: 10px 12px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  display: flex;
  gap: 6px;
  position: relative;
  overflow: visible;
  transition: all var(--transition-fast);
}

.sc--active {
  border-color: var(--accent-indigo);
  background: rgba(99, 102, 241, 0.06);
}

.sc__grip {
  color: var(--text-muted);
  cursor: grab;
  display: flex;
  align-items: flex-start;
  padding-top: 2px;
  opacity: 0.25;
  transition: opacity 0.15s;
}

.sc:hover .sc__grip { opacity: 1; }

.sc__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.sc__top {
  display: flex;
  align-items: center;
  gap: 5px;
}

.sc__dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--bg-tertiary);
  color: var(--text-muted);
  border: 1.5px solid var(--glass-border);
  transition: all var(--transition-fast);
}

.sc__name {
  font-weight: 700;
  font-size: 0.8rem;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  line-height: 1.2;
}

.sc__type {
  font-size: 0.65rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 600;
  padding-left: 25px;
}

.sc__btns {
  display: flex;
  gap: 1px;
  opacity: 0;
  transition: opacity 0.15s;
}

.sc:hover .sc__btns { opacity: 1; }

/* icon buttons */
.ib {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 3px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.ib:hover { background: var(--glass-bg-hover); color: var(--text-primary); }
.ib--del:hover { color: var(--accent-rose); }

/* ===== Status dot colors ===== */
.sw--completed .sc__dot {
  background: rgba(16, 185, 129, 0.2);
  border-color: var(--accent-emerald);
  color: var(--accent-emerald);
}

.sw--active .sc__dot {
  background: var(--accent-indigo);
  border-color: var(--accent-indigo);
  color: #fff;
}

/* ===== Pulse ===== */
.sc__pulse {
  position: absolute;
  inset: -2px;
  border-radius: inherit;
  border: 2px solid var(--accent-indigo);
  opacity: 0;
  animation: pulse 2s infinite;
  pointer-events: none;
}

@keyframes pulse {
  0%   { transform: scale(1);    opacity: 0.5; }
  100% { transform: scale(1.06); opacity: 0;   }
}

/* ===== Promote button ===== */
.sc__promo {
  margin-top: 4px;
  border-top: 1px solid var(--glass-border);
  padding-top: 5px;
}

.promo {
  width: 100%;
  padding: 4px 6px;
  background: var(--gradient-accent);
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.65rem;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.15s;
}

.promo:hover:not(:disabled) {
  filter: brightness(1.15);
  transform: translateY(-1px);
}

.promo:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.promo__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== Horizontal connector ===== */
.cn-h {
  width: 44px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  position: relative;
}

.cn-h__line {
  height: 2.5px;
  flex: 1;
  background: var(--text-muted);
  opacity: 0.35;
  border-radius: 2px;
  transition: all var(--transition-fast);
}

.cn-h__tip {
  color: var(--text-muted);
  opacity: 0.5;
  margin-left: -8px;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

/* connector status colors */
.cn--completed .cn-h__line {
  background: var(--accent-emerald);
  opacity: 1;
  box-shadow: 0 0 6px rgba(52, 211, 153, 0.35);
}
.cn--completed .cn-h__tip { color: var(--accent-emerald); opacity: 1; }

.cn--active .cn-h__line {
  background: var(--accent-indigo);
  opacity: 1;
  box-shadow: 0 0 8px rgba(99, 102, 241, 0.35);
}
.cn--active .cn-h__tip { color: var(--accent-indigo); opacity: 1; }

/* ===== Vertical connector (row-to-row transition) ===== */
.cn-v {
  position: absolute;
  top: 100%;
  height: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* Dynamic centering — no hardcoded px offsets */
  left: 50%;
  transform: translateX(-50%);
}

.cn-v__line {
  width: 2.5px;
  flex: 1;
  background: var(--text-muted);
  opacity: 0.35;
  border-radius: 2px;
  transition: all var(--transition-fast);
}

.cn-v__tip {
  color: var(--text-muted);
  opacity: 0.5;
  margin-top: -6px;
  transition: all var(--transition-fast);
}

.cn--completed .cn-v__line {
  background: var(--accent-emerald);
  opacity: 1;
  box-shadow: 0 0 6px rgba(52, 211, 153, 0.35);
}
.cn--completed .cn-v__tip { color: var(--accent-emerald); opacity: 1; }

.cn--active .cn-v__line {
  background: var(--accent-indigo);
  opacity: 1;
  box-shadow: 0 0 8px rgba(99, 102, 241, 0.35);
}
.cn--active .cn-v__tip { color: var(--accent-indigo); opacity: 1; }

/* ===== Mobile compact timeline ===== */
.pl-mob {
  display: none;
  padding: 0.5rem 1rem;
}

.mn {
  display: grid;
  grid-template-columns: 28px 1fr auto;
  align-items: center;
  gap: 10px;
  position: relative;
  padding: 8px 0;
}

.mn__dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  color: var(--text-muted);
  border: 1.5px solid var(--glass-border);
  z-index: 1;
  transition: all var(--transition-fast);
}

.mn--completed .mn__dot {
  background: rgba(16, 185, 129, 0.2);
  border-color: var(--accent-emerald);
  color: var(--accent-emerald);
}

.mn--active .mn__dot {
  background: var(--accent-indigo);
  border-color: var(--accent-indigo);
  color: #fff;
}

.mn__body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.mn__name {
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mn__type {
  font-size: 0.65rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 600;
}

.mn__promo {
  padding: 5px 10px;
  background: var(--gradient-accent);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  transition: all 0.15s;
}

.mn__promo:hover:not(:disabled) { filter: brightness(1.15); }
.mn__promo:disabled { opacity: 0.5; cursor: not-allowed; }

.mn__acts {
  display: flex;
  gap: 2px;
}
/* ===== Operational Panel ===== */
.op-panel {
  margin-top: 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
  padding: 1rem 1.25rem;
  border: 1px solid var(--glass-border);
  background: rgba(255,255,255,0.03);
  border-radius: var(--radius-lg);
}

.op-panel__info {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.op-panel__badge {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-muted);
  font-weight: 700;
}

.op-panel__env {
  font-size: 1rem;
  font-weight: 800;
  color: var(--text-primary);
}

.op-panel__next {
  font-size: 0.82rem;
  color: var(--text-secondary);
}

.op-panel__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.85rem 1.25rem;
  border: none;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  color: white;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}


.op-panel__btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.08);
}

.op-panel__btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* vertical connecting line between mobile nodes */
.mn__line {
  position: absolute;
  left: 13px;
  top: calc(8px + 28px);
  bottom: -8px;
  width: 2.5px;
  background: var(--text-muted);
  opacity: 0.25;
  border-radius: 2px;
  transition: all var(--transition-fast);
}

.mn__line--completed {
  background: var(--accent-emerald);
  opacity: 1;
}

.mn__line--active {
  background: var(--accent-indigo);
  opacity: 1;
}

/* ===== Responsive Breakpoints ===== */
@media (max-width: 640px) {
  .pl-desk { display: none; }
  .pl-mob  { display: block; }
}

@media (min-width: 641px) and (max-width: 1024px) {
  .sc { width: 140px; padding: 8px 10px; }
}

/* ===== Utility ===== */
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
