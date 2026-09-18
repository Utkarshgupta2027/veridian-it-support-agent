package com.veridian.agent.config;
import com.veridian.agent.entity.KnowledgeBase; import com.veridian.agent.repository.KnowledgeBaseRepository; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.*;
@Configuration public class SeedData {
 @Bean CommandLineRunner seed(KnowledgeBaseRepository r){return args->{if(r.count()>0)return;
  r.save(new KnowledgeBase("KB-01","Account Lockout","The supplied assignment analysis maps the password/account lockout scenario REQ-03 to KB-01. Load the official data-pack text for the complete procedure."));
  r.save(new KnowledgeBase("KB-02","VPN Credentials","The supplied assignment analysis states that expired VPN credentials map to KB-02 and that the employee should renew VPN credentials. Load the official data-pack text for the complete procedure."));
  r.save(new KnowledgeBase("KB-09","Security / Phishing","The supplied assignment analysis states that suspected phishing, malware, or unauthorized access must be reported immediately to Security and should not be forwarded to other employees. Load the official data-pack text for the exact address/procedure."));
  r.save(new KnowledgeBase("KB-EXPENSE","Expense System Ownership","The supplied assignment analysis states that expense software access is owned by Finance, not IT. IT can help with login/technical problems after an account already exists."));
 };}}
