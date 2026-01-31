import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TimerResponse } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TimerService {
  private apiUrl = `${environment.apiUrl}/timer`;

  constructor(private http: HttpClient) {}

  startTimer(subtaskId: number): Observable<TimerResponse> {
    return this.http.post<TimerResponse>(`${this.apiUrl}/start/${subtaskId}`, {});
  }

  stopTimer(): Observable<TimerResponse> {
    return this.http.post<TimerResponse>(`${this.apiUrl}/stop`, {});
  }

  getActiveTimer(): Observable<TimerResponse> {
    return this.http.get<TimerResponse>(`${this.apiUrl}/active`);
  }
}
