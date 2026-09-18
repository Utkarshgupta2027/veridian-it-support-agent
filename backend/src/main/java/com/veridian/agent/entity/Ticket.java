package com.veridian.agent.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private SupportRequest request;
    private String status;
    private String decision;
    private String priority;
    private String assignedTo;
    private String employeeName;
    private String employeeEmail;
    private String category;
    private String knowledgeSource;
    private String nextAction;
    private String referenceCode;
    private String issueSummary;
    @Column(columnDefinition = "TEXT")
    private String resolution;
    private LocalDateTime createdAt;

    public Ticket() {}

    public Ticket(SupportRequest request, String status, String decision, String priority, String assignedTo, String resolution) {
        this.request = request;
        this.status = status;
        this.decision = decision;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.resolution = resolution;
        this.createdAt = LocalDateTime.now();
    }

    public Ticket(
        SupportRequest request,
        String status,
        String decision,
        String priority,
        String assignedTo,
        String resolution,
        String category,
        String knowledgeSource,
        String nextAction
    ) {
        this(request, status, decision, priority, assignedTo, resolution);
        this.category = category;
        this.knowledgeSource = knowledgeSource;
        this.nextAction = nextAction;
        if (request != null) {
            this.employeeName = request.getEmployeeName();
            this.employeeEmail = request.getEmployeeEmail();
            this.issueSummary = request.getRequestText();
        }
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SupportRequest getRequest() {
        return request;
    }

    public String getStatus() {
        return status;
    }

    public String getDecision() {
        return decision;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public String getPriority() {
        return priority;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getCategory() {
        return category;
    }

    public String getKnowledgeSource() {
        return knowledgeSource;
    }

    public String getNextAction() {
        return nextAction;
    }

    public String getResolution() {
        return resolution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setReferenceCode(String value) {
        referenceCode = value;
    }

    public void setIssueSummary(String value) {
        issueSummary = value;
    }
}
