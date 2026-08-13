import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task, Subtask } from '../../models/task.model';
import { TagSelectorComponent } from '../tag-selector/tag-selector.component';

@Component({
  selector: 'app-subtask-points-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, TagSelectorComponent],
  templateUrl: './subtask-points-modal.component.html',
  styleUrls: ['./subtask-points-modal.component.css']
})
export class SubtaskPointsModalComponent {
  @Input() task: Task | null = null;
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<{subtask: Subtask, tagIds: number[]}[]>();

  editableSubtasks: Subtask[] = [];
  subtaskTagIds: Map<number, number[]> = new Map();  // subtaskId -> tagIds

  ngOnChanges(): void {
    if (this.task && this.isOpen) {
      // Create a copy of subtasks for editing
      this.editableSubtasks = this.task.subtasks.map(subtask => ({ ...subtask }));
      
      // Initialize tag IDs map
      this.subtaskTagIds.clear();
      this.editableSubtasks.forEach(subtask => {
        const tagIds = subtask.tags?.map(tag => tag.id) || [];
        this.subtaskTagIds.set(subtask.id, tagIds);
      });
    }
  }

  onClose(): void {
    this.close.emit();
  }

  onSave(): void {
    // Validate that no actual points exceed planned points
    const invalidSubtasks = this.editableSubtasks.filter(subtask => 
      subtask.actualPoints != null && 
      subtask.plannedPoints != null && 
      subtask.actualPoints > subtask.plannedPoints
    );

    if (invalidSubtasks.length > 0) {
      const subtaskNumbers = invalidSubtasks.map(s => `#${s.subtaskNumber}`).join(', ');
      alert(`Actual points cannot exceed planned points for subtask(s): ${subtaskNumbers}`);
      return;
    }

    // Validate that actual points > 0 only if time was spent
    const noTimeSubtasks = this.editableSubtasks.filter(subtask =>
      subtask.actualPoints != null &&
      subtask.actualPoints > 0 &&
      (!subtask.totalTimeSeconds || subtask.totalTimeSeconds === 0)
    );

    if (noTimeSubtasks.length > 0) {
      const subtaskNumbers = noTimeSubtasks.map(s => `#${s.subtaskNumber}`).join(', ');
      alert(`Cannot assign actual points to subtask(s) with no time spent: ${subtaskNumbers}. Please track time first.`);
      return;
    }

    // Emit subtasks with their tag IDs
    const subtasksWithTags = this.editableSubtasks.map(subtask => ({
      subtask,
      tagIds: this.subtaskTagIds.get(subtask.id) || []
    }));
    
    console.log('Modal emitting subtasksWithTags:', subtasksWithTags);
    this.save.emit(subtasksWithTags);
  }

  updateTagIds(subtaskId: number, tagIds: number[]): void {
    this.subtaskTagIds.set(subtaskId, tagIds);
  }

  getTagIds(subtaskId: number): number[] {
    return this.subtaskTagIds.get(subtaskId) || [];
  }

  trackBySubtaskId(index: number, subtask: Subtask): number {
    return subtask.id;
  }
}
