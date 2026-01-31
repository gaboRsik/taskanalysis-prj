import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { CategoryService } from '../../services/category.service';
import { TaskService } from '../../services/task.service';
import { TimerService } from '../../services/timer.service';
import { Category, Task, TaskStatus, TimerResponse } from '../../models/task.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: any;
  categories: Category[] = [];
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  selectedCategoryId: number | null = null;
  
  activeTimer: TimerResponse | null = null;
  timerInterval: any;
  elapsedTime: string = '00:00:00';
  
  newCategoryName: string = '';
  newTaskTitle: string = '';
  newTaskCategoryId: number | null = null;
  newTaskSubtaskCount: number = 3;

  private destroy$ = new Subject<void>();

  TaskStatus = TaskStatus;

  constructor(
    private authService: AuthService,
    private categoryService: CategoryService,
    private taskService: TaskService,
    private timerService: TimerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(
      user => this.currentUser = user
    );

    this.loadCategories();
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

  loadCategories(): void {
    this.categoryService.getCategories().pipe(takeUntil(this.destroy$)).subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => console.error('Error loading categories:', error)
    });
  }

  loadTasks(): void {
    this.taskService.getTasks().pipe(takeUntil(this.destroy$)).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.filterTasks();
      },
      error: (error) => console.error('Error loading tasks:', error)
    });
  }

  filterTasks(): void {
    if (this.selectedCategoryId === null) {
      this.filteredTasks = this.tasks;
    } else {
      this.filteredTasks = this.tasks.filter(t => t.categoryId === this.selectedCategoryId);
    }
  }

  onCategoryFilterChange(): void {
    this.filterTasks();
  }

  createCategory(): void {
    if (this.newCategoryName.trim()) {
      this.categoryService.createCategory({ name: this.newCategoryName }).pipe(takeUntil(this.destroy$)).subscribe({
        next: (category) => {
          this.categories.push(category);
          this.newCategoryName = '';
        },
        error: (error) => console.error('Error creating category:', error)
      });
    }
  }

  createTask(): void {
    if (this.newTaskTitle.trim() && this.newTaskCategoryId && this.newTaskSubtaskCount) {
      this.taskService.createTask({
        name: this.newTaskTitle,
        categoryId: this.newTaskCategoryId,
        subtaskCount: this.newTaskSubtaskCount
      }).pipe(takeUntil(this.destroy$)).subscribe({
        next: (task) => {
          this.tasks.push(task);
          this.filterTasks();
          this.newTaskTitle = '';
          this.newTaskCategoryId = null;
          this.newTaskSubtaskCount = 3;
        },
        error: (error) => console.error('Error creating task:', error)
      });
    }
  }

  checkActiveTimer(): void {
    this.timerService.getActiveTimer().pipe(takeUntil(this.destroy$)).subscribe({
      next: (timer) => {
        if (timer) {
          this.activeTimer = timer;
          this.startTimerDisplay();
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

  getCategoryName(categoryId: number): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
