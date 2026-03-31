import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { UserDTO } from '../../models/admin.model';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  users: UserDTO[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getAllUsers().subscribe({
      next: (data: UserDTO[]) => {
        this.users = data;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to load users';
      }
    });
  }

  onRoleChange(user: UserDTO, newRole: string): void {
    if (confirm(`Are you sure you want to change ${user.name}'s role to ${newRole}?`)) {
      this.successMessage = '';
      this.errorMessage = '';

      this.adminService.updateUserRole(user.id, { role: newRole }).subscribe({
        next: (updatedUser: UserDTO) => {
          // Update local user list
          const index = this.users.findIndex(u => u.id === user.id);
          if (index !== -1) {
            this.users[index] = updatedUser;
          }
          this.successMessage = `Successfully updated ${user.name}'s role to ${newRole}`;
          
          // Clear success message after 3 seconds
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error: any) => {
          this.errorMessage = error.error?.message || 'Failed to update role';
        }
      });
    }
  }
}
