export interface UserDTO {
  id: number;
  email: string;
  name: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateRoleRequest {
  role: string;
}
