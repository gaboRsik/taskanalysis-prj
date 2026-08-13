import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubtaskTagService, SubtaskTag } from '../../services/subtask-tag.service';

@Component({
  selector: 'app-tag-selector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tag-selector.component.html',
  styleUrls: ['./tag-selector.component.scss']
})
export class TagSelectorComponent implements OnInit {
  @Input() selectedTagIds: number[] = [];
  @Output() selectedTagIdsChange = new EventEmitter<number[]>();
  
  availableTags: SubtaskTag[] = [];
  loading = false;
  error: string | null = null;
  isDropdownOpen = false;

  constructor(private tagService: SubtaskTagService) {}

  ngOnInit(): void {
    this.loadTags();
  }

  loadTags(): void {
    this.loading = true;
    this.error = null;
    
    this.tagService.getAllTags().subscribe({
      next: (tags) => {
        this.availableTags = tags;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading tags:', err);
        this.error = 'Failed to load tags';
        this.loading = false;
      }
    });
  }

  toggleTag(tagId: number): void {
    const index = this.selectedTagIds.indexOf(tagId);
    
    if (index > -1) {
      // Remove tag
      this.selectedTagIds = this.selectedTagIds.filter(id => id !== tagId);
    } else {
      // Add tag
      this.selectedTagIds = [...this.selectedTagIds, tagId];
    }
    
    this.selectedTagIdsChange.emit(this.selectedTagIds);
  }

  isTagSelected(tagId: number): boolean {
    return this.selectedTagIds.includes(tagId);
  }

  getSelectedTags(): SubtaskTag[] {
    return this.availableTags.filter(tag => this.selectedTagIds.includes(tag.id));
  }

  removeTag(tagId: number): void {
    this.selectedTagIds = this.selectedTagIds.filter(id => id !== tagId);
    this.selectedTagIdsChange.emit(this.selectedTagIds);
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }
}
