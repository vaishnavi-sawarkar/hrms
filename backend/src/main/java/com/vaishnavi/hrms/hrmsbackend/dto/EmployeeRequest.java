
package com.vaishnavi.hrms.hrmsbackend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfJoining;
    private Long departmentId;
    private Long designationId;
    private Long managerId; // optional, can be null
    private Double salary;
}