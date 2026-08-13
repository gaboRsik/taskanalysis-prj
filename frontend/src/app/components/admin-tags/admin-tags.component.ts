import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubtaskTagService, SubtaskTag, CreateTagRequest } from '../../services/subtask-tag.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-admin-tags',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-tags.component.html',
  styleUrls: ['./admin-tags.component.scss']
})
export class AdminTagsComponent implements OnInit {
  tags: SubtaskTag[] = [];
  loading = false;
  error: string | null = null;
  currentUser: User | null = null;

  // Create/Edit form
  showForm = false;
  editingTag: SubtaskTag | null = null;
  formData: CreateTagRequest = {
    name: '',
    color: '#667eea',
    isGlobal: false
  };

  // Predefined colors
  predefinedColors = [
    { name: 'Blue', value: '#3b82f6' },
    { name: 'Green', value: '#10b981' },
    { name: 'Orange', value: '#f59e0b' },
    { name: 'Purple', value: '#8b5cf6' },
    { name: 'Red', value: '#ef4444' },
    { name: 'Pink', value: '#ec4899' },
    { name: 'Indigo', value: '#6366f1' },
    { name: 'Teal', value: '#14b8a6' },
    { name: 'Yellow', value: '#f97316' },
    { name: 'Rose', value: '#f43f5e' },
    { name: 'Cyan', value: '#06b6d4' },
    { name: 'Violet', value: '#7c3aed' }
  ];

  constructor(
    private tagService: SubtaskTagService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadTags();
  }

  loadTags(): void {
    this.loading = true;
    this.error = null;

    this.tagService.getAllTags().subscribe({
      next: (tags) => {
        this.tags = tags;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading tags:', err);
        this.error = 'Failed to load tags';
        this.loading = false;
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.editingTag = null;
    this.formData = {
      name: '',
      color: '#667eea',
      isGlobal: false  // Default to personal tag
    };
  }

  openEditForm(tag: SubtaskTag): void {
    this.showForm = true;
    this.editingTag = tag;
    this.formData = {
      name: tag.name,
      color: tag.color,
      isGlobal: tag.isGlobal
    };
  }

  closeForm(): void {
    this.showForm = false;
    this.editingTag = null;
  }

  saveTag(): void {
    if (!this.formData.name.trim()) {
      alert('Tag name is required');
      return;
    }

    if (this.editingTag) {
      // Update existing tag
      this.tagService.updateTag(this.editingTag.id, this.formData).subscribe({
        next: () => {
          this.closeForm();
          this.loadTags();
        },
        error: (err) => {
          console.error('Error updating tag:', err);
          alert('Failed to update tag: ' + (err.error?.message || 'Unknown error'));
        }
      });
    } else {
      // Create new tag
      this.tagService.createTag(this.formData).subscribe({
        next: () => {
          this.closeForm();
          this.loadTags();
        },
        error: (err) => {
          console.error('Error creating tag:', err);
          console.error('Full error:', JSON.stringify(err, null, 2));
          console.error('Error status:', err.status);
          console.error('Error body:', err.error);
          const errorMessage = err.error?.message || err.message || 'Unknown error';
          alert('Failed to create tag: ' + errorMessage);
        }
      });
    }
  }

  deleteTag(tag: SubtaskTag): void {
    if (!confirm(`Are you sure you want to delete the tag "${tag.name}"?${tag.usageCount ? ` It is used ${tag.usageCount} time(s).` : ''}`)) {
      return;
    }

    this.tagService.deleteTag(tag.id).subscribe({
      next: () => {
        this.loadTags();
      },
      error: (err) => {
        console.error('Error deleting tag:', err);
        alert('Failed to delete tag: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  selectColor(color: string): void {
    this.formData.color = color;
  }

  getGlobalTags(): SubtaskTag[] {
    return this.tags.filter(tag => tag.isGlobal);
  }

  getUserTags(): SubtaskTag[] {
    return this.tags.filter(tag => !tag.isGlobal);
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }
}
