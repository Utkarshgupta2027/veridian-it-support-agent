package com.veridian.agent.entity;
import java.time.LocalDateTime;

 import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
@Entity @Table(name="requests")
public class SupportRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String employeeName, employeeEmail, category, status,requestCode,initialAction;
 @Column(nullable=false,columnDefinition="TEXT") private String requestText;
 private LocalDateTime createdAt;
 public SupportRequest(){}
 public SupportRequest(String n,String e,String m){employeeName=n;employeeEmail=e;requestText=m;createdAt=LocalDateTime.now();}
 @PrePersist void pre(){if(createdAt==null)createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getEmployeeName(){return employeeName;} public String getEmployeeEmail(){return employeeEmail;}
 public String getRequestText(){return requestText;} public String getCategory(){return category;} public String getStatus(){return status;} public LocalDateTime getCreatedAt(){return createdAt;}
 public String getRequestCode(){return requestCode;} public String getInitialAction(){return initialAction;}
 public void setCategory(String v){category=v;} public void setStatus(String v){status=v;}
 public void setRequestCode(String v){requestCode=v;} public void setInitialAction(String v){initialAction=v;}
}
