package com.vaishnavi.hrms.hrmsbackend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfJoining;
    private String departmentName;
    private String designationName;
    private String managerName; // "First Last" of manager, or null
    private Double salary;
}
