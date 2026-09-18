package com.veridian.agent.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AgentRequest(@NotBlank @Size(max=120) String employeeName,@NotBlank @Email @Size(max=254) String employeeEmail,@NotBlank @Size(max=4000) String message){}
