import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Template, TemplateRequest } from '../models/template.model';
import { Task } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private apiUrl = `${environment.apiUrl}/templates`;

  constructor(private http: HttpClient) {}

  /**
   * Get all templates for the current user
   */
  getAll(): Observable<Template[]> {
    return this.http.get<Template[]>(this.apiUrl);
  }

  /**
   * Get a specific template by ID
   */
  getById(id: number): Observable<Template> {
    return this.http.get<Template>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create a new template
   */
  create(request: TemplateRequest): Observable<Template> {
    return this.http.post<Template>(this.apiUrl, request);
  }

  /**
   * Update an existing template
   */
  update(id: number, request: TemplateRequest): Observable<Template> {
    return this.http.put<Template>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete a template
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create a task from a template
   */
  createTaskFromTemplate(templateId: number): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/${templateId}/create-task`, {});
  }
}
