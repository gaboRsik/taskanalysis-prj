import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatbotService, ChatbotAnalysisResponse } from '../../services/chatbot.service';

@Component({
  selector: 'app-chatbot-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chatbot-modal.component.html',
  styleUrls: ['./chatbot-modal.component.scss']
})
export class ChatbotModalComponent {
  @Input() taskId: number | null = null;
  @Input() taskName: string = '';
  @Output() close = new EventEmitter<void>();

  analysis: string | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit() {
    if (this.taskId) {
      this.requestAnalysis();
    }
  }

  requestAnalysis() {
    if (!this.taskId) return;

    this.loading = true;
    this.error = null;

    this.chatbotService.analyzeTask(this.taskId).subscribe({
      next: (response: ChatbotAnalysisResponse) => {
        this.analysis = response.analysis;
        this.loading = false;
      },
      error: (err) => {
        console.error('Chatbot analysis error:', err);
        this.error = 'Nem sikerült az értékelést lekérni. Próbáld újra!';
        this.loading = false;
      }
    });
  }

  onClose() {
    this.close.emit();
  }
}
