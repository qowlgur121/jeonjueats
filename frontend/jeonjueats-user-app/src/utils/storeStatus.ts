/**
 * 가게 영업 상태 관련 유틸리티 함수
 */

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

/**
 * 현재 시간이 운영시간 내인지 확인
 */
export const isStoreOpenNow = (operatingHours: string): boolean => {
  if (!operatingHours) return false
  
  const now = new Date()
  const currentDay = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'][now.getDay()]
  const currentTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  
  try {
    // JSON 형태인지 확인
    if (operatingHours.startsWith('{')) {
      const weeklyHours: WeeklyHours = JSON.parse(operatingHours)
      const todayHours = weeklyHours[currentDay as keyof WeeklyHours]
      
      if (!todayHours || todayHours.closed) {
        return false
      }
      
      return isTimeInRange(currentTime, todayHours.open, todayHours.close)
    }
    
    // 기존 "09:00 - 22:00" 형태 처리
    const match = operatingHours.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
    if (match) {
      const [, openTime, closeTime] = match
      return isTimeInRange(currentTime, openTime, closeTime)
    }
  } catch (error) {
    console.error('운영시간 파싱 에러:', error)
  }
  
  return false
}

/**
 * 시간이 범위 내에 있는지 확인 (새벽까지 운영하는 가게도 지원)
 */
const isTimeInRange = (currentTime: string, openTime: string, closeTime: string): boolean => {
  const current = timeToMinutes(currentTime)
  const open = timeToMinutes(openTime)
  const close = timeToMinutes(closeTime)
  
  // 새벽까지 운영하는 경우 (예: 18:00-03:00)
  if (close < open) {
    return current >= open || current <= close
  }
  
  // 일반적인 경우 (예: 09:00-22:00)
  return current >= open && current <= close
}

/**
 * 시간을 분 단위로 변환
 */
const timeToMinutes = (time: string): number => {
  const [hours, minutes] = time.split(':').map(Number)
  return hours * 60 + minutes
}

/**
 * 가게 영업 상태 확인 (사장님 강제 설정이 우선)
 */
export const getStoreOperatingStatus = (store: any): {
  isOpen: boolean
  displayStatus: string
  statusClass: string
} => {
  // 사장님이 강제로 설정한 상태가 우선
  // DB의 status가 'CLOSED'면 무조건 영업 종료
  if (store.status === 'CLOSED') {
    return {
      isOpen: false,
      displayStatus: '영업종료',
      statusClass: 'closed'
    }
  }
  
  // DB의 status가 'OPEN'이면 운영시간에 관계없이 영업 중
  // (사장님이 원하면 운영시간 밖에도 영업 가능)
  if (store.status === 'OPEN') {
    return {
      isOpen: true,
      displayStatus: '영업중',
      statusClass: 'open'
    }
  }
  
  // 기본값 (status가 없거나 알 수 없는 경우)
  return {
    isOpen: false,
    displayStatus: '영업종료',
    statusClass: 'closed'
  }
}