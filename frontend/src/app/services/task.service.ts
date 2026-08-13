import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Task, TaskRequest, Subtask, SubtaskRequest } from '../models/task.model';
import { ExportRequest, ExportResponse, ExportFormat, DeliveryMethod } from '../models/export.model';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = `${environment.apiUrl}/tasks`;
  private subtaskUrl = `${environment.apiUrl}/subtasks`;
  private exportUrl = `${environment.apiUrl}/export`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Task[]> {
    return this.http.get<Task[]>(this.apiUrl);
  }

  getById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  create(request: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, request);
  }

  update(id: number, request: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateSubtask(subtaskId: number, request: SubtaskRequest): Observable<Subtask> {
    console.log(`Sending PUT request to ${this.subtaskUrl}/${subtaskId}`, request);
    return this.http.put<Subtask>(`${this.subtaskUrl}/${subtaskId}`, request).pipe(
      tap(
        result => console.log(`Subtask ${subtaskId} updated successfully:`, result),
        error => console.error(`Error updating subtask ${subtaskId}:`, error)
      )
    );
  }

  updateSubtaskPoints(subtasksWithTags: {subtask: Subtask, tagIds: number[]}[]): Observable<Subtask[]> {
    console.log('TaskService.updateSubtaskPoints called with:', subtasksWithTags);
    
    if (subtasksWithTags.length === 0) {
      console.log('No subtasks to update, returning empty array');
      return of([]);
    }
    
    const updates = subtasksWithTags.map(item => {
      console.log(`Creating update request for subtask ${item.subtask.id}:`, {
        plannedPoints: item.subtask.plannedPoints,
        actualPoints: item.subtask.actualPoints,
        tagIds: item.tagIds
      });
      
      return this.updateSubtask(item.subtask.id, {
        plannedPoints: item.subtask.plannedPoints,
        actualPoints: item.subtask.actualPoints,
        tagIds: item.tagIds
      });
    });
    
    console.log(`Sending ${updates.length} update requests with forkJoin`);
    return forkJoin(updates).pipe(
      tap(results => console.log('forkJoin completed, results:', results))
    );
  }

  getTasks(): Observable<Task[]> {
    return this.getAll();
  }

  createTask(request: TaskRequest): Observable<Task> {
    return this.create(request);
  }

  /**
   * Export task data as Excel with email delivery
   * @param taskId Task ID to export
   * @param format Export format (XLSX or PDF)
   * @returns Observable with export response
   */
  exportTaskByEmail(taskId: number, format: ExportFormat = ExportFormat.XLSX): Observable<ExportResponse> {
    const request: ExportRequest = {
      format,
      delivery: DeliveryMethod.EMAIL
    };
    return this.http.post<ExportResponse>(`${this.exportUrl}/task/${taskId}`, request);
  }

  /**
   * Update task status (COMPLETED, IN_PROGRESS, NOT_STARTED)
   * @param taskId Task ID to update
   * @param status New status
   * @returns Observable with updated task
   */
  updateTaskStatus(taskId: number, status: string): Observable<Task> {
    return this.http.patch<Task>(`${this.apiUrl}/${taskId}/status`, { status });
  }

  /**
   * Export task data as Excel with direct download
   * @param taskId Task ID to export
   * @param format Export format (XLSX or PDF)
   * @returns Observable with file blob
   */
  exportTaskByDownload(taskId: number, format: ExportFormat = ExportFormat.XLSX): Observable<HttpResponse<Blob>> {
    const request: ExportRequest = {
      format,
      delivery: DeliveryMethod.DOWNLOAD
    };
    return this.http.post(`${this.exportUrl}/task/${taskId}`, request, {
      responseType: 'blob',
      observe: 'response'
    });
  }

  /**
   * Generic export method with delivery choice
   * @param taskId Task ID to export
   * @param format Export format
   * @param delivery Delivery method (EMAIL or DOWNLOAD)
   */
  exportTask(taskId: number, format: ExportFormat, delivery: DeliveryMethod): Observable<any> {
    if (delivery === DeliveryMethod.EMAIL) {
      return this.exportTaskByEmail(taskId, format);
    } else {
      return this.exportTaskByDownload(taskId, format);
    }
  }
}
