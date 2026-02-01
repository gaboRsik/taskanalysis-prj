import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { TimerService } from '../../services/timer.service';
import { Task, TaskStatus, TimerResponse } from '../../models/task.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: any;
  tasks: Task[] = [];
  selectedTask: Task | null = null;
  selectedTaskId: number | null = null;
  
  activeTimer: TimerResponse | null = null;
  timerInterval: any;
  elapsedTime: string = '00:00:00';

  private destroy$ = new Subject<void>();

  TaskStatus = TaskStatus;

  constructor(
    private authService: AuthService,
    private taskService: TaskService,
    private timerService: TimerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(
      user => this.currentUser = user
    );

    this.loadTasks();
    this.checkActiveTimer();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  loadTasks(): void {
    this.taskService.getTasks().pipe(takeUntil(this.destroy$)).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        // If a task was selected, update it
        if (this.selectedTaskId) {
          this.selectedTask = this.tasks.find(t => t.id === this.selectedTaskId) || null;
        }
      },
      error: (error) => console.error('Error loading tasks:', error)
    });
  }

  onTaskChange(): void {
    this.selectedTask = this.tasks.find(t => t.id === this.selectedTaskId) || null;
  }

  checkActiveTimer(): void {
    this.timerService.getActiveTimer().pipe(takeUntil(this.destroy$)).subscribe({
      next: (timer) => {
        if (timer) {
          this.activeTimer = timer;
          this.startTimerDisplay();
          
          // Auto-select the task that has the active timer
          const activeTask = this.tasks.find(t => 
            t.subtasks?.some(st => st.id === timer.subtaskId)
          );
          if (activeTask) {
            this.selectedTaskId = activeTask.id;
            this.selectedTask = activeTask;
          }
        }
      },
      error: (error) => {
        if (error.status !== 404) {
          console.error('Error checking active timer:', error);
        }
      }
    });
  }

  startTimer(subtaskId: number): void {
    this.timerService.startTimer(subtaskId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (timer) => {
        this.activeTimer = timer;
        this.startTimerDisplay();
        this.loadTasks();
      },
      error: (error) => console.error('Error starting timer:', error)
    });
  }

  stopTimer(): void {
    if (this.activeTimer) {
      this.timerService.stopTimer().pipe(takeUntil(this.destroy$)).subscribe({
        next: () => {
          this.activeTimer = null;
          this.stopTimerDisplay();
          this.loadTasks();
        },
        error: (error) => console.error('Error stopping timer:', error)
      });
    }
  }

  startTimerDisplay(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    this.timerInterval = setInterval(() => {
      if (this.activeTimer) {
        const startTime = new Date(this.activeTimer.startTime).getTime();
        const now = new Date().getTime();
        const elapsed = Math.floor((now - startTime) / 1000);
        
        const hours = Math.floor(elapsed / 3600);
        const minutes = Math.floor((elapsed % 3600) / 60);
        const seconds = elapsed % 60;
        
        this.elapsedTime = `${this.pad(hours)}:${this.pad(minutes)}:${this.pad(seconds)}`;
      }
    }, 1000);
  }

  stopTimerDisplay(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    this.elapsedTime = '00:00:00';
  }

  pad(num: number): string {
    return num.toString().padStart(2, '0');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
