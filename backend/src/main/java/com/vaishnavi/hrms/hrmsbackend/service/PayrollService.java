package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.dto.PayrollBreakup;
import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PayrollService {

    private final EmployeeRepository employeeRepository;

    // Simplified fixed percentages for salary breakup
    private static final double HRA_PERCENT = 0.40;       // 40% of basic as HRA
    private static final double BASIC_PERCENT = 0.50;     // 50% of gross salary is "basic"
    private static final double PF_PERCENT = 0.12;        // 12% of basic deducted as PF
    private static final double PROFESSIONAL_TAX = 200.0;  // flat monthly deduction

    public PayrollService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public PayrollBreakup calculatePayroll(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        double gross = employee.getSalary() != null ? employee.getSalary() : 0.0;
        double basic = gross * BASIC_PERCENT;
        double hra = basic * HRA_PERCENT;
        double pf = basic * PF_PERCENT;
        double netSalary = gross - pf - PROFESSIONAL_TAX;

        return PayrollBreakup.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .grossSalary(gross)
                .basic(basic)
                .hra(hra)
                .pfDeduction(pf)
                .professionalTax(PROFESSIONAL_TAX)
                .netSalary(netSalary)
                .build();
    }

    public byte[] exportPayrollToExcel() throws IOException {
        List<Employee> employees = employeeRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payroll");

            // Header row
            Row header = sheet.createRow(0);
            String[] columns = {"Employee ID", "Name", "Gross Salary", "Basic", "HRA", "PF Deduction", "Professional Tax", "Net Salary"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data rows
            int rowIndex = 1;
            for (Employee employee : employees) {
                PayrollBreakup payroll = calculatePayroll(employee.getId());
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(payroll.getEmployeeId());
                row.createCell(1).setCellValue(payroll.getEmployeeName());
                row.createCell(2).setCellValue(payroll.getGrossSalary());
                row.createCell(3).setCellValue(payroll.getBasic());
                row.createCell(4).setCellValue(payroll.getHra());
                row.createCell(5).setCellValue(payroll.getPfDeduction());
                row.createCell(6).setCellValue(payroll.getProfessionalTax());
                row.createCell(7).setCellValue(payroll.getNetSalary());
            }

            // Auto-size columns for readability
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
