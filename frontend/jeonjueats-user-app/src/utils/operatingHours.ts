/**
 * 운영시간 파싱 및 표시 유틸리티
 * 
 * 지원하는 형식:
 * 1. 기존 문자열 형식: "매일 09:00-22:00", "09:00 - 22:00"
 * 2. JSON 형식: {"monday": {"open": "09:00", "close": "22:00", "closed": false}, ...}
 */

export interface DayHours {
  open: string
  close: string
  closed: boolean
}

export interface WeeklyHours {
  monday: DayHours
  tuesday: DayHours
  wednesday: DayHours
  thursday: DayHours
  friday: DayHours
  saturday: DayHours
  sunday: DayHours
}

export const dayNames = {
  monday: '월',
  tuesday: '화',
  wednesday: '수',
  thursday: '목',
  friday: '금',
  saturday: '토',
  sunday: '일'
}

/**
 * 운영시간 문자열을 파싱하여 사용자 친화적인 형태로 변환
 */
export function parseOperatingHours(operatingHours: string): string {
  if (!operatingHours || operatingHours.trim() === '') {
    return '운영시간 미정'
  }

  try {
    // JSON 형식인지 확인 (요일별 운영시간)
    if (operatingHours.startsWith('{')) {
      const weeklyHours: WeeklyHours = JSON.parse(operatingHours)
      return formatWeeklyHours(weeklyHours)
    }

    // 기존 문자열 형식 처리
    return formatSimpleHours(operatingHours)
  } catch (error) {
    console.error('운영시간 파싱 에러:', error)
    return operatingHours // 파싱 실패 시 원본 반환
  }
}

/**
 * 요일별 운영시간을 사용자 친화적으로 포맷팅
 */
function formatWeeklyHours(weeklyHours: WeeklyHours): string {
  const days = Object.keys(weeklyHours) as (keyof WeeklyHours)[]
  const openDays = days.filter(day => !weeklyHours[day].closed)
  const closedDays = days.filter(day => weeklyHours[day].closed)

  if (openDays.length === 0) {
    return '모든 요일 휴무'
  }

  if (closedDays.length === 0) {
    // 모든 요일 영업하는지 확인
    const firstDayHours = weeklyHours[openDays[0]]
    const allSameHours = openDays.every(day => {
      const dayHours = weeklyHours[day]
      return dayHours.open === firstDayHours.open && dayHours.close === firstDayHours.close
    })

    if (allSameHours) {
      return `매일 ${firstDayHours.open} - ${firstDayHours.close}`
    }
  }

  // 요일별로 다른 시간이거나 휴무일이 있는 경우 간단히 표시
  if (closedDays.length > 0) {
    const closedDayNames = closedDays.map(day => dayNames[day]).join(', ')
    
    // 가장 일반적인 운영시간 찾기
    const hourGroups: { [key: string]: string[] } = {}
    openDays.forEach(day => {
      const dayHours = weeklyHours[day]
      const timeKey = `${dayHours.open}-${dayHours.close}`
      if (!hourGroups[timeKey]) {
        hourGroups[timeKey] = []
      }
      hourGroups[timeKey].push(dayNames[day])
    })

    // 가장 많은 요일을 가진 그룹을 기본 시간으로 사용
    const mainTimeGroup = Object.entries(hourGroups).sort((a, b) => b[1].length - a[1].length)[0]
    if (mainTimeGroup) {
      const [timeKey] = mainTimeGroup
      const [open, close] = timeKey.split('-')
      return `${open} - ${close} (${closedDayNames} 휴무)`
    }
  }

  // 복잡한 경우 첫 번째 영업 요일의 시간을 표시
  const firstOpenDay = weeklyHours[openDays[0]]
  return `${firstOpenDay.open} - ${firstOpenDay.close} (요일별 상이)`
}

/**
 * 기존 문자열 형식의 운영시간 포맷팅
 */
function formatSimpleHours(operatingHours: string): string {
  // "매일 XX:XX-XX:XX" 형식 처리
  const dailyMatch = operatingHours.match(/매일\s*(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
  if (dailyMatch) {
    const [, open, close] = dailyMatch
    return `${open} - ${close}`
  }

  // "XX:XX - XX:XX" 형식 처리
  const timeMatch = operatingHours.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
  if (timeMatch) {
    const [, open, close] = timeMatch
    return `${open} - ${close}`
  }

  // 기타 형식은 그대로 반환
  return operatingHours
}

/**
 * 현재 시간이 운영시간 내인지 확인
 */
export function isOpenNow(operatingHours: string): boolean {
  if (!operatingHours || operatingHours.trim() === '') {
    return false
  }

  try {
    const now = new Date()
    const currentDay = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'][now.getDay()]
    const currentTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`

    // JSON 형식인지 확인
    if (operatingHours.startsWith('{')) {
      const weeklyHours: WeeklyHours = JSON.parse(operatingHours)
      const todayHours = weeklyHours[currentDay as keyof WeeklyHours]
      
      if (todayHours.closed) {
        return false
      }

      return isTimeInRange(currentTime, todayHours.open, todayHours.close)
    }

    // 기존 문자열 형식 처리
    const timeMatch = operatingHours.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
    if (timeMatch) {
      const [, open, close] = timeMatch
      return isTimeInRange(currentTime, open, close)
    }

    return false
  } catch (error) {
    console.error('운영시간 확인 에러:', error)
    return false
  }
}

/**
 * 주어진 시간이 운영 시간 범위 내인지 확인
 */
function isTimeInRange(currentTime: string, openTime: string, closeTime: string): boolean {
  const [currentHour, currentMinute] = currentTime.split(':').map(Number)
  const [openHour, openMinute] = openTime.split(':').map(Number)
  const [closeHour, closeMinute] = closeTime.split(':').map(Number)

  const currentMinutes = currentHour * 60 + currentMinute
  const openMinutes = openHour * 60 + openMinute
  let closeMinutes = closeHour * 60 + closeMinute

  // 다음날 새벽까지 운영하는 경우 (예: 18:00-03:00)
  if (closeMinutes <= openMinutes) {
    closeMinutes += 24 * 60
    // 현재 시간이 자정 이전인지 이후인지 확인
    if (currentMinutes < openMinutes) {
      return currentMinutes + 24 * 60 <= closeMinutes
    }
  }

  return currentMinutes >= openMinutes && currentMinutes <= closeMinutes
}

/**
 * 운영 상태 텍스트 반환
 */
export function getOperatingStatus(operatingHours: string): { text: string; isOpen: boolean } {
  const isOpen = isOpenNow(operatingHours)
  
  return {
    text: isOpen ? '영업 중' : '영업 종료',
    isOpen
  }
}