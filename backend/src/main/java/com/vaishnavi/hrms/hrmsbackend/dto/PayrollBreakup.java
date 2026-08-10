package com.vaishnavi.hrms.hrmsbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBreakup {
    private Long employeeId;
    private String employeeName;
    private double grossSalary;
    private double basic;
    private double hra;
    private double pfDeduction;
    private double professionalTax;
    private double netSalary;
}
