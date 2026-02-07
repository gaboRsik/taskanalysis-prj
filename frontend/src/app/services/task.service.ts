import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
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
    return this.http.put<Subtask>(`${this.subtaskUrl}/${subtaskId}`, request);
  }

  updateSubtaskPoints(subtasks: Subtask[]): Observable<Subtask[]> {
    const updates = subtasks.map(subtask => 
      this.updateSubtask(subtask.id, {
        plannedPoints: subtask.plannedPoints,
        actualPoints: subtask.actualPoints
      })
    );
    return new Observable(observer => {
      Promise.all(updates.map(obs => obs.toPromise()))
        .then(results => {
          observer.next(results as Subtask[]);
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
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
