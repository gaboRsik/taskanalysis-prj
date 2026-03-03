export interface Template {
  id: number;
  name: string;
  description?: string;
  categoryId: number;
  categoryName: string;
  subtaskCount: number;
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
  templateSubtasks: TemplateSubtask[];
}
