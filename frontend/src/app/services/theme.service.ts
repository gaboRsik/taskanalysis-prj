import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'taskanalysis-theme';
  private themeSubject: BehaviorSubject<Theme>;
  public theme$: Observable<Theme>;

  constructor() {
    // Initialize theme from localStorage or system preference
    const savedTheme = this.getSavedTheme();
    const initialTheme = savedTheme || this.getSystemTheme();
    
    this.themeSubject = new BehaviorSubject<Theme>(initialTheme);
    this.theme$ = this.themeSubject.asObservable();
    
    // Apply initial theme
    this.applyTheme(initialTheme);
    
    // Listen for system theme changes
    this.listenToSystemThemeChanges();
  }

  /**
   * Get current theme
   */
  getCurrentTheme(): Theme {
    return this.themeSubject.value;
  }

  /**
   * Toggle between light and dark theme
   */
  toggleTheme(): void {
    const newTheme: Theme = this.getCurrentTheme() === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  /**
   * Set specific theme
   */
  setTheme(theme: Theme): void {
    this.themeSubject.next(theme);
    this.applyTheme(theme);
    this.saveTheme(theme);
  }

  /**
   * Check if dark mode is active
   */
  isDarkMode(): boolean {
    return this.getCurrentTheme() === 'dark';
  }

  /**
   * Apply theme to document
   */
  private applyTheme(theme: Theme): void {
    const body = document.body;
    
    if (theme === 'dark') {
      body.classList.add('dark-theme');
      body.classList.remove('light-theme');
    } else {
      body.classList.add('light-theme');
      body.classList.remove('dark-theme');
    }
    
    // Update Bootstrap data-bs-theme attribute
    body.setAttribute('data-bs-theme', theme);
  }

  /**
   * Save theme to localStorage
   */
  private saveTheme(theme: Theme): void {
    try {
      localStorage.setItem(this.THEME_KEY, theme);
    } catch (error) {
      console.error('Failed to save theme to localStorage', error);
    }
  }

  /**
   * Get saved theme from localStorage
   */
  private getSavedTheme(): Theme | null {
    try {
      const saved = localStorage.getItem(this.THEME_KEY);
      return saved === 'dark' || saved === 'light' ? saved : null;
    } catch (error) {
      console.error('Failed to read theme from localStorage', error);
      return null;
    }
  }

  /**
   * Get system theme preference
   */
  private getSystemTheme(): Theme {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    return 'light';
  }

  /**
   * Listen to system theme changes
   */
  private listenToSystemThemeChanges(): void {
    if (window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        // Only apply system theme if no user preference is saved
        if (!this.getSavedTheme()) {
          const systemTheme: Theme = e.matches ? 'dark' : 'light';
          this.setTheme(systemTheme);
        }
      });
    }
  }
}
