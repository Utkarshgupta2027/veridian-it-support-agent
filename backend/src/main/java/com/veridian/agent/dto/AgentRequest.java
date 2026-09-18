package com.veridian.agent.dto;
import jakarta.validation.constraints.*;
public record AgentRequest(@NotBlank String employeeName,@NotBlank @Email String employeeEmail,@NotBlank String message){}
