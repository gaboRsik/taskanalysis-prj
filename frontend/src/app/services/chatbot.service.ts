import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatbotAnalysisResponse {
  analysis: string;
  timestamp: string;
  tokensUsed?: number;
  model: string;
  taskId: number;
  taskName: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = `${environment.apiUrl}/chatbot`;

  constructor(private http: HttpClient) {}

  /**
   * Request AI analysis for a task
   */
  analyzeTask(taskId: number): Observable<ChatbotAnalysisResponse> {
    const token = localStorage.getItem('access_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<ChatbotAnalysisResponse>(
      `${this.apiUrl}/analyze/${taskId}`,
      {},
      { headers }
    );
  }
}
