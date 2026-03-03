export interface Template {
  id: number;
  name: string;
  description?: string;
  categoryId: number;
  categoryName: string;
  subtaskCount: number;
  taskCount: number;
  templateSubtasks: TemplateSubtask[];
  createdAt: string;
  updatedAt: string;
}

export interface TemplateSubtask {
  subtaskNumber: number;
  plannedPoints: number;
}

export interface TemplateRequest {
  name: string;
  description?: string;
  categoryId: number;
  subtaskCount: number;
  taskCount: number;
  templateSubtasks: TemplateSubtask[];
}
