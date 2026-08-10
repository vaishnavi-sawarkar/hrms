package com.vaishnavi.hrms.hrmsbackend.repository;

import com.vaishnavi.hrms.hrmsbackend.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
