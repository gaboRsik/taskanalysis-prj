import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/task.model';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss']
})
export class CategoriesComponent implements OnInit, OnDestroy {
  categories: Category[] = [];
  newCategoryName: string = '';
  editingCategory: Category | null = null;
  editCategoryName: string = '';
  
  private destroy$ = new Subject<void>();

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
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

  startEdit(category: Category): void {
    this.editingCategory = category;
    this.editCategoryName = category.name;
  }

  cancelEdit(): void {
    this.editingCategory = null;
    this.editCategoryName = '';
  }

  updateCategory(): void {
    if (this.editingCategory && this.editCategoryName.trim()) {
      this.categoryService.update(this.editingCategory.id, { name: this.editCategoryName })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (updated) => {
            const index = this.categories.findIndex(c => c.id === updated.id);
            if (index !== -1) {
              this.categories[index] = updated;
            }
            this.cancelEdit();
          },
          error: (error) => {
            console.error('Error updating category:', error);
          }
        });
    }
  }

  deleteCategory(id: number): void {
    if (confirm('Are you sure you want to delete this category? This will also delete all related tasks.')) {
      this.categoryService.delete(id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => {
          this.categories = this.categories.filter(c => c.id !== id);
        },
        error: (error) => console.error('Error deleting category:', error)
      });
    }
  }
}
