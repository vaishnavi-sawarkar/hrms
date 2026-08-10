package com.vaishnavi.hrms.hrmsbackend.controller;

import com.vaishnavi.hrms.hrmsbackend.dto.PayrollBreakup;
import com.vaishnavi.hrms.hrmsbackend.service.PayrollService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<PayrollBreakup> getPayroll(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.calculatePayroll(employeeId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPayroll() throws IOException {
        byte[] excelBytes = payrollService.exportPayrollToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
