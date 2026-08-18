package com.vaishnavi.hrms.hrmsbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // "ADMIN", "HR", "EMPLOYEE"
    private String email; // must match an existing employee record for EMPLOYEE role
}
