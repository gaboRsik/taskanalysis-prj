package com.taskanalysis.dto.admin;

import com.taskanalysis.entity.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateRoleRequest {
    
    @NotNull(message = "Role is required")
    private Role role;

    public UpdateRoleRequest() {
    }

    public UpdateRoleRequest(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
