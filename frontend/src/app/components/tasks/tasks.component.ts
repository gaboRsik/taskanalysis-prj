import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CategoryService } from '../../services/category.service';
import { TaskService } from '../../services/task.service';
import { Category, Task, TaskStatus } from '../../models/task.model';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.scss']
})
export class TasksComponent implements OnInit, OnDestroy {
  categories: Category[] = [];
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  selectedCategoryId: number | null = null;
  
  newTaskTitle: string = '';
  newTaskCategoryId: number | null = null;
  newTaskSubtaskCount: number = 3;

  editingTask: Task | null = null;
  editTaskName: string = '';

  private destroy$ = new Subject<void>();

  TaskStatus = TaskStatus;

  constructor(
    private categoryService: CategoryService,
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadTasks();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

  createTask(): void {
    if (this.newTaskTitle.trim() && this.newTaskCategoryId && this.newTaskSubtaskCount) {
      this.taskService.createTask({
        name: this.newTaskTitle,
        categoryId: this.newTaskCategoryId,
        subtaskCount: this.newTaskSubtaskCount
      }).pipe(takeUntil(this.destroy$)).subscribe({
        next: (task) => {
          this.newTaskTitle = '';
          this.newTaskCategoryId = null;
          this.newTaskSubtaskCount = 3;
          this.loadTasks(); // Reload the entire list to get correct order
        },
        error: (error) => console.error('Error creating task:', error)
      });
    }
  }

  startEdit(task: Task): void {
    this.editingTask = task;
    this.editTaskName = task.name;
  }

  cancelEdit(): void {
    this.editingTask = null;
    this.editTaskName = '';
  }

  updateTask(): void {
    if (this.editingTask && this.editTaskName.trim()) {
      this.taskService.update(this.editingTask.id, { name: this.editTaskName })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (updated: Task) => {
            const index = this.tasks.findIndex(t => t.id === updated.id);
            if (index !== -1) {
              this.tasks[index] = updated;
            }
            this.filterTasks();
            this.cancelEdit();
          },
          error: (error: any) => {
            console.error('Error updating task:', error);
            console.error('Error status:', error.status);
            console.error('Error message:', error.message);
            console.error('Error details:', error.error);
            alert('Hiba történt a task módosítása során: ' + (error.error?.message || error.message));
          }
        });
    }
  }

  deleteTask(task: Task): void {
    if (confirm(`Are you sure you want to delete "${task.name}"?`)) {
      this.taskService.delete(task.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => {
          this.tasks = this.tasks.filter(t => t.id !== task.id);
          this.filterTasks();
        },
        error: (error: any) => console.error('Error deleting task:', error)
      });
    }
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }
}
