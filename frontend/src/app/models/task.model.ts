export interface Category {
  id: number;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface Task {
  id: number;
  name: string;
  title?: string;
  description?: string;
  categoryId: number;
  categoryName?: string;
  subtaskCount: number;
  status: TaskStatus;
  createdAt: string;
  updatedAt: string;
  subtasks: Subtask[];
  totalPlannedPoints?: number;
  totalActualPoints?: number;
  plannedTotalTimeMinutes?: number;
  totalActualTimeSeconds?: number;
  
  // Performance Metrics
  plannedEfficiencyScore?: number;        // plannedPoints / plannedTimeHours
  actualEfficiencyScore?: number;          // actualPoints / actualTimeHours
  plannedTimePerPoint?: number;            // plannedTimeMinutes / plannedPoints
  actualTimePerPoint?: number;             // actualTimeMinutes / actualPoints
  efficiencyVariancePercent?: number;      // (actual - planned) / planned * 100
}

export interface TaskRequest {
  name: string;
  description?: string;
  categoryId?: number;
  subtaskCount?: number;
  plannedTotalTimeMinutes?: number;
}

export interface Subtask {
  id: number;
  taskId: number;
  title: string;
  subtaskNumber: number;
  plannedPoints?: number;
  actualPoints?: number;
  estimatedTime: number;
  actualTime: number;
  status: SubtaskStatus;
  totalTimeSeconds: number;
  createdAt: string;
  updatedAt: string;

  // Computed metrics
  proportionalPlannedTimeMinutes?: number;
  plannedEfficiencyScore?: number;
  actualEfficiencyScore?: number;
  plannedTimePerPoint?: number;
  actualTimePerPoint?: number;
  efficiencyVariancePercent?: number;
  timeVariancePercent?: number;
}

export interface SubtaskRequest {
  plannedPoints?: number;
  actualPoints?: number;
}

export interface TimerResponse {
  timeEntryId: number;
  subtaskId: number;
  subtaskNumber: number;
  taskTitle: string;
  subtaskTitle: string;
  startTime: string;
  endTime?: string;
  durationSeconds: number;
  isRunning: boolean;
}

export enum TaskStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED'
}

export enum SubtaskStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED'
}
