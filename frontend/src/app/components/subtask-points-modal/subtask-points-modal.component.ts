import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task, Subtask } from '../../models/task.model';

@Component({
  selector: 'app-subtask-points-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subtask-points-modal.component.html',
  styleUrls: ['./subtask-points-modal.component.css']
})
export class SubtaskPointsModalComponent {
  @Input() task: Task | null = null;
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<Subtask[]>();

  editableSubtasks: Subtask[] = [];

  ngOnChanges(): void {
    if (this.task && this.isOpen) {
      // Create a copy of subtasks for editing
      this.editableSubtasks = this.task.subtasks.map(subtask => ({ ...subtask }));
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

    this.save.emit(this.editableSubtasks);
  }

  trackBySubtaskId(index: number, subtask: Subtask): number {
    return subtask.id;
  }
}
