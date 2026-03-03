import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TemplateService } from '../../services/template.service';
import { CategoryService } from '../../services/category.service';
import { Template, TemplateRequest, TemplateSubtask } from '../../models/template.model';
import { Category } from '../../models/task.model';

@Component({
  selector: 'app-templates',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.scss']
})
export class TemplatesComponent implements OnInit, OnDestroy {
  templates: Template[] = [];
  categories: Category[] = [];
  
  // Form state
  showForm: boolean = false;
  isEditMode: boolean = false;
  editingTemplateId: number | null = null;
  
  // Form data
  templateName: string = '';
  templateDescription: string = '';
  selectedCategoryId: number | null = null;
  subtaskCount: number = 3;
  templateSubtasks: TemplateSubtask[] = [];
  
  // UI state
  errorMessage: string = '';
  successMessage: string = '';
  
  private destroy$ = new Subject<void>();

  constructor(
    private templateService: TemplateService,
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTemplates();
    this.loadCategories();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadTemplates(): void {
    this.templateService.getAll().pipe(takeUntil(this.destroy$)).subscribe({
      next: (templates) => {
        this.templates = templates;
      },
      error: (error) => {
        console.error('Error loading templates:', error);
        this.showError('Failed to load templates');
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getCategories().pipe(takeUntil(this.destroy$)).subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => console.error('Error loading categories:', error)
    });
  }

  openCreateForm(): void {
    this.resetForm();
    this.showForm = true;
    this.isEditMode = false;
  }

  openEditForm(template: Template): void {
    this.resetForm();
    this.showForm = true;
    this.isEditMode = true;
    this.editingTemplateId = template.id;
    
    this.templateName = template.name;
    this.templateDescription = template.description || '';
    this.selectedCategoryId = template.categoryId;
    this.subtaskCount = template.subtaskCount;
    this.templateSubtasks = [...template.templateSubtasks];
  }

  closeForm(): void {
    this.showForm = false;
    this.resetForm();
  }

  resetForm(): void {
    this.templateName = '';
    this.templateDescription = '';
    this.selectedCategoryId = null;
    this.subtaskCount = 3;
    this.templateSubtasks = [];
    this.editingTemplateId = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onSubtaskCountChange(): void {
    const currentCount = this.templateSubtasks.length;
    
    if (this.subtaskCount > currentCount) {
      // Add new subtasks
      for (let i = currentCount; i < this.subtaskCount; i++) {
        this.templateSubtasks.push({
          subtaskNumber: i + 1,
          plannedPoints: 0
        });
      }
    } else if (this.subtaskCount < currentCount) {
      // Remove excess subtasks
      this.templateSubtasks = this.templateSubtasks.slice(0, this.subtaskCount);
    }
  }

  saveTemplate(): void {
    if (!this.validateForm()) {
      return;
    }

    const request: TemplateRequest = {
      name: this.templateName.trim(),
      description: this.templateDescription.trim() || undefined,
      categoryId: this.selectedCategoryId!,
      subtaskCount: this.subtaskCount,
      templateSubtasks: this.templateSubtasks
    };

    const operation = this.isEditMode && this.editingTemplateId
      ? this.templateService.update(this.editingTemplateId, request)
      : this.templateService.create(request);

    operation.pipe(takeUntil(this.destroy$)).subscribe({
      next: (template) => {
        this.showSuccess(this.isEditMode ? 'Template updated successfully' : 'Template created successfully');
        this.loadTemplates();
        this.closeForm();
      },
      error: (error) => {
        console.error('Error saving template:', error);
        this.showError(error.error?.message || 'Failed to save template');
      }
    });
  }

  validateForm(): boolean {
    this.errorMessage = '';

    if (!this.templateName.trim()) {
      this.showError('Template name is required');
      return false;
    }

    if (!this.selectedCategoryId) {
      this.showError('Category is required');
      return false;
    }

    if (this.subtaskCount < 1 || this.subtaskCount > 100) {
      this.showError('Subtask count must be between 1 and 100');
      return false;
    }

    return true;
  }

  deleteTemplate(template: Template): void {
    if (confirm(`Are you sure you want to delete template "${template.name}"?`)) {
      this.templateService.delete(template.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => {
          this.showSuccess('Template deleted successfully');
          this.loadTemplates();
        },
        error: (error) => {
          console.error('Error deleting template:', error);
          this.showError('Failed to delete template');
        }
      });
    }
  }

  createTaskFromTemplate(template: Template): void {
    if (confirm(`Create a new task from template "${template.name}"?`)) {
      this.templateService.createTaskFromTemplate(template.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: (task) => {
          this.showSuccess(`Task "${task.name}" created successfully from template`);
          // Navigate to tasks page after a brief delay
          setTimeout(() => {
            this.router.navigate(['/tasks']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error creating task from template:', error);
          this.showError('Failed to create task from template');
        }
      });
    }
  }

  showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    setTimeout(() => this.errorMessage = '', 5000);
  }

  showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => this.successMessage = '', 3000);
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }
}
