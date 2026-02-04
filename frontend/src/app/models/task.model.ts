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
}

export interface TaskRequest {
  name: string;
  description?: string;
  categoryId?: number;
  subtaskCount?: number;
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
