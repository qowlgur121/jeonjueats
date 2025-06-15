<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface DayHours {
  open: string
  close: string
  closed: boolean
}

interface WeeklyHours {
  monday: DayHours
  tuesday: DayHours
  wednesday: DayHours
  thursday: DayHours
  friday: DayHours
  saturday: DayHours
  sunday: DayHours
}

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const days = [
  { key: 'monday', name: '월요일', short: '월' },
  { key: 'tuesday', name: '화요일', short: '화' },
  { key: 'wednesday', name: '수요일', short: '수' },
  { key: 'thursday', name: '목요일', short: '목' },
  { key: 'friday', name: '금요일', short: '금' },
  { key: 'saturday', name: '토요일', short: '토' },
  { key: 'sunday', name: '일요일', short: '일' }
]

const currentDay = ref('monday')

// 시간 옵션 생성 (30분 단위)
const generateTimeOptions = () => {
  const times = []
  for (let hour = 0; hour < 24; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
      const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
      const displayString = formatTimeDisplay(timeString)
      times.push({ value: timeString, display: displayString })
    }
  }
  return times
}

const formatTimeDisplay = (time: string) => {
  const [hour, minute] = time.split(':').map(Number)
  const period = hour < 12 ? '오전' : '오후'
  const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour
  return `${period} ${displayHour}:${minute.toString().padStart(2, '0')}`
}

const timeOptions = generateTimeOptions()

// 기본 요일별 운영시간 설정
const defaultWeeklyHours: WeeklyHours = {
  monday: { open: '09:00', close: '22:00', closed: false },
  tuesday: { open: '09:00', close: '22:00', closed: false },
  wednesday: { open: '09:00', close: '22:00', closed: false },
  thursday: { open: '09:00', close: '22:00', closed: false },
  friday: { open: '09:00', close: '22:00', closed: false },
  saturday: { open: '10:00', close: '23:00', closed: false },
  sunday: { open: '10:00', close: '21:00', closed: false }
}

const weeklyHours = ref<WeeklyHours>({ ...defaultWeeklyHours })

// props 값에서 초기 데이터 파싱
const parseOperatingHours = (operatingHours: string): WeeklyHours => {
  if (!operatingHours) return { ...defaultWeeklyHours }
  
  try {
    // JSON 형태인지 확인
    if (operatingHours.startsWith('{')) {
      return JSON.parse(operatingHours)
    }
    
    // 기존 "09:00 - 22:00" 형태 처리
    const match = operatingHours.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
    if (match) {
      const [, open, close] = match
      const converted: WeeklyHours = { ...defaultWeeklyHours }
      
      // 모든 요일에 동일한 시간 적용
      Object.keys(converted).forEach(day => {
        converted[day as keyof WeeklyHours] = { open, close, closed: false }
      })
      
      return converted
    }
  } catch (error) {
    console.error('운영시간 파싱 에러:', error)
  }
  
  return { ...defaultWeeklyHours }
}

// 초기값 설정
weeklyHours.value = parseOperatingHours(props.modelValue)

// 요일별 운영시간을 JSON 문자열로 변환
const generateOperatingHoursString = (hours: WeeklyHours): string => {
  return JSON.stringify(hours)
}

// 모든 요일에 동일한 시간 적용
const applyToAllDays = () => {
  const currentDayHours = weeklyHours.value[currentDay.value as keyof WeeklyHours]
  
  Object.keys(weeklyHours.value).forEach(day => {
    weeklyHours.value[day as keyof WeeklyHours] = { ...currentDayHours }
  })
}

// 요약 정보 생성
const summaryText = computed(() => {
  const openDays = days.filter(day => !weeklyHours.value[day.key as keyof WeeklyHours].closed)
  const closedDays = days.filter(day => weeklyHours.value[day.key as keyof WeeklyHours].closed)
  
  if (openDays.length === 0) {
    return '모든 요일 휴무'
  }
  
  if (closedDays.length === 0) {
    // 모든 요일 영업하는지 확인
    const firstDayHours = weeklyHours.value[openDays[0].key as keyof WeeklyHours]
    const allSameHours = openDays.every(day => {
      const dayHours = weeklyHours.value[day.key as keyof WeeklyHours]
      return dayHours.open === firstDayHours.open && dayHours.close === firstDayHours.close
    })
    
    if (allSameHours) {
      return `매일 ${firstDayHours.open} - ${firstDayHours.close}`
    }
  }
  
  // 요일별로 다른 시간이거나 휴무일이 있는 경우
  const summary = []
  
  if (closedDays.length > 0) {
    summary.push(`휴무: ${closedDays.map(d => d.short).join(', ')}`)
  }
  
  // 가장 일반적인 운영시간 찾기
  const hourGroups: { [key: string]: string[] } = {}
  openDays.forEach(day => {
    const dayHours = weeklyHours.value[day.key as keyof WeeklyHours]
    const timeKey = `${dayHours.open}-${dayHours.close}`
    if (!hourGroups[timeKey]) {
      hourGroups[timeKey] = []
    }
    hourGroups[timeKey].push(day.short)
  })
  
  Object.entries(hourGroups).forEach(([timeKey, dayList]) => {
    const [open, close] = timeKey.split('-')
    summary.push(`${dayList.join(', ')}: ${open} - ${close}`)
  })
  
  return summary.join(' | ')
})

// 변경사항을 부모 컴포넌트에 전달
watch(weeklyHours, (newHours) => {
  emit('update:modelValue', generateOperatingHoursString(newHours))
}, { deep: true })

// props 변경 감지
watch(() => props.modelValue, (newValue) => {
  weeklyHours.value = parseOperatingHours(newValue)
})
</script>

<template>
  <div class="weekly-operating-hours" @submit.stop.prevent>
    <!-- 요일 선택 탭 -->
    <div class="day-tabs">
      <button 
        v-for="day in days" 
        :key="day.key"
        type="button"
        @click.stop="currentDay = day.key"
        :class="['day-tab', { active: currentDay === day.key }]"
      >
        {{ day.short }}
      </button>
    </div>
    
    <!-- 선택된 요일의 시간 설정 -->
    <div class="day-settings">
      <div class="day-header">
        <h4>{{ days.find(d => d.key === currentDay)?.name }} 운영시간</h4>
        <label class="closed-toggle">
          <input 
            type="checkbox" 
            v-model="weeklyHours[currentDay as keyof WeeklyHours].closed"
            @click.stop
          >
          <span>휴무일</span>
        </label>
      </div>
      
      <div 
        v-if="!weeklyHours[currentDay as keyof WeeklyHours].closed" 
        class="time-picker-section"
      >
        <div class="time-picker-row">
          <select 
            v-model="weeklyHours[currentDay as keyof WeeklyHours].open" 
            class="time-select"
            @click.stop
          >
            <option v-for="time in timeOptions" :key="time.value" :value="time.value">
              {{ time.display }}
            </option>
          </select>
          <span class="time-separator">부터</span>
          <select 
            v-model="weeklyHours[currentDay as keyof WeeklyHours].close" 
            class="time-select"
            @click.stop
          >
            <option v-for="time in timeOptions" :key="time.value" :value="time.value">
              {{ time.display }}
            </option>
          </select>
          <span class="time-separator">까지</span>
        </div>
        
        <button @click.stop="applyToAllDays" class="btn-apply-all" type="button">
          모든 요일에 동일 적용
        </button>
      </div>
      
      <div v-else class="closed-message">
        이 날은 휴무일입니다.
      </div>
    </div>
    
    <!-- 운영시간 요약 -->
    <div class="summary-section">
      <div class="summary-header">운영시간 요약</div>
      <div class="summary-content">{{ summaryText }}</div>
    </div>
  </div>
</template>

<style scoped>
.weekly-operating-hours {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  background: #f9fafb;
}

/* 요일 탭 */
.day-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 1rem;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 6px;
}

.day-tab {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  color: #6b7280;
}

.day-tab.active {
  background: #3b82f6;
  color: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.day-tab:hover:not(.active) {
  background: #e5e7eb;
  color: #374151;
}

/* 요일 설정 */
.day-settings {
  margin-bottom: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.day-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.closed-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
}

.closed-toggle input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #ef4444;
}

/* 시간 선택 */
.time-picker-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;
}

.time-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.time-separator {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  min-width: 30px;
  text-align: center;
}

.btn-apply-all {
  padding: 8px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  align-self: flex-start;
}

.btn-apply-all:hover {
  background: #059669;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.4);
}

.closed-message {
  padding: 1rem;
  text-align: center;
  color: #6b7280;
  font-style: italic;
  background: #f3f4f6;
  border-radius: 6px;
}

/* 요약 섹션 */
.summary-section {
  padding: 1rem;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
}

.summary-header {
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 8px;
}

.summary-content {
  font-size: 13px;
  color: #0c4a6e;
  line-height: 1.5;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .day-tabs {
    flex-wrap: wrap;
  }
  
  .day-tab {
    min-width: 40px;
    padding: 6px 8px;
    font-size: 13px;
  }
  
  .time-picker-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .time-select {
    width: 100%;
  }
  
  .time-separator {
    min-width: auto;
  }
  
  .day-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>