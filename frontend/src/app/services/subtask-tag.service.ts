import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SubtaskTag {
  id: number;
  name: string;
  color: string;
  isGlobal: boolean;
  userId: number | null;
  createdById: number;
  createdAt: string;
  usageCount?: number;
}

export interface CreateTagRequest {
  name: string;
  color: string;
  isGlobal: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SubtaskTagService {
  private apiUrl = `${environment.apiUrl}/subtask-tags`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Get all visible tags (global + user's own)
   */
  getAllTags(): Observable<SubtaskTag[]> {
    return this.http.get<SubtaskTag[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  /**
   * Get global tags only (admin only)
   */
  getGlobalTags(): Observable<SubtaskTag[]> {
    return this.http.get<SubtaskTag[]>(`${this.apiUrl}/global`, { headers: this.getHeaders() });
  }

  /**
   * Get tag by ID
   */
  getTagById(id: number): Observable<SubtaskTag> {
    return this.http.get<SubtaskTag>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  /**
   * Create new tag
   */
  createTag(request: CreateTagRequest): Observable<SubtaskTag> {
    return this.http.post<SubtaskTag>(this.apiUrl, request, { headers: this.getHeaders() });
  }

  /**
   * Update tag
   */
  updateTag(id: number, request: CreateTagRequest): Observable<SubtaskTag> {
    return this.http.put<SubtaskTag>(`${this.apiUrl}/${id}`, request, { headers: this.getHeaders() });
  }

  /**
   * Delete tag
   */
  deleteTag(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  /**
   * Get tags by IDs (batch)
   */
  getTagsByIds(ids: number[]): Observable<SubtaskTag[]> {
    return this.http.post<SubtaskTag[]>(`${this.apiUrl}/batch`, ids, { headers: this.getHeaders() });
  }
}
