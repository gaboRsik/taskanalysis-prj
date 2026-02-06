import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { TimerService } from '../../services/timer.service';
import { Task, TaskStatus, TimerResponse, Subtask } from '../../models/task.model';
import { SubtaskPointsModalComponent } from '../subtask-points-modal/subtask-points-modal.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NgxChartsModule, SubtaskPointsModalComponent],
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

  // Chart data
  chartData: any[] = [];
  view: any = undefined; // Auto-size based on container
  showXAxis = true;
  showYAxis = true;
  gradient = false;
  showLegend = false;
  showXAxisLabel = true;
  xAxisLabel = 'Subtasks';
  showYAxisLabel = true;
  yAxisLabel = 'Time (seconds)';
  animations = true;
  colorScheme: any = {
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA', '#7aa3e5', '#a27ea8']
  };

  // Points modal
  isPointsModalOpen = false;

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
          this.updateChartData();
        }
      },
      error: (error) => console.error('Error loading tasks:', error)
    });
  }

  onTaskChange(): void {
    this.selectedTask = this.tasks.find(t => t.id === this.selectedTaskId) || null;
    this.updateChartData();
  }

  updateChartData(): void {
    if (!this.selectedTask || !this.selectedTask.subtasks) {
      this.chartData = [];
      return;
    }

    this.chartData = this.selectedTask.subtasks
      .filter(subtask => {
        const time = subtask.totalTimeSeconds;
        return time != null && !isNaN(time) && isFinite(time) && time > 0;
      })
      .map(subtask => {
        return {
          name: `Subtask #${subtask.subtaskNumber}`,
          value: subtask.totalTimeSeconds, // Use seconds directly for better precision
          extra: {
            seconds: subtask.totalTimeSeconds,
            formattedTime: this.formatTime(subtask.totalTimeSeconds)
          }
        };
      });
  }

  formatTime(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    if (hours > 0) {
      return `${hours}h ${minutes}m ${secs}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${secs}s`;
    } else {
      return `${secs}s`;
    }
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
        // Ignore 404 (not found) and 204 (no content) - these mean no active timer
        if (error.status !== 404 && error.status !== 204) {
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

  openPointsModal(): void {
    if (this.selectedTask) {
      this.isPointsModalOpen = true;
    }
  }

  closePointsModal(): void {
    this.isPointsModalOpen = false;
  }

  saveSubtaskPoints(subtasks: Subtask[]): void {
    this.taskService.updateSubtaskPoints(subtasks)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.closePointsModal();
          if (this.selectedTaskId) {
            this.loadTasks(); // Reload to get updated data
          }
        },
        error: (error) => {
          console.error('Error updating subtask points:', error);
          alert('Hiba történt a pontok mentése során');
        }
      });
  }
}
