package com.veridian.agent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class SupportRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employeeName;
    private String employeeEmail;
    private String category;
    private String status;
    private String requestCode;
    private String initialAction;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestText;
    private LocalDateTime createdAt;

    public SupportRequest() {}

    public SupportRequest(String name, String email, String message) {
        employeeName = name;
        employeeEmail = email;
        requestText = message;
        createdAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public String getRequestText() { return requestText; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRequestCode() { return requestCode; }
    public String getInitialAction() { return initialAction; }
    public void setCategory(String value) { category = value; }
    public void setStatus(String value) { status = value; }
    public void setRequestCode(String value) { requestCode = value; }
    public void setInitialAction(String value) { initialAction = value; }
}
