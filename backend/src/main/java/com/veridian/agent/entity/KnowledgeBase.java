package com.veridian.agent.entity;
import jakarta.persistence.*;
@Entity @Table(name="knowledge_base")
public class KnowledgeBase {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="policy_id",unique=true,nullable=false) private String policyId;
 @Column(nullable=false) private String title;
 @Column(nullable=false,columnDefinition="TEXT") private String content;
 public KnowledgeBase() {}
 public KnowledgeBase(String p,String t,String c){policyId=p;title=t;content=c;}
 public Long getId(){return id;} public String getPolicyId(){return policyId;} public String getTitle(){return title;} public String getContent(){return content;}
}
