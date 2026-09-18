package com.veridian.agent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", unique = true, nullable = false)
    private String policyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public KnowledgeBase() {}

    public KnowledgeBase(String policyId, String title, String content) {
        this.policyId = policyId;
        this.title = title;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getPolicyId() { return policyId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}
