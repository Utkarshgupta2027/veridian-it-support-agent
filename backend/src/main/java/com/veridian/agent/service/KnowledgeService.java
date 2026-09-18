package com.veridian.agent.service;
import com.veridian.agent.entity.KnowledgeBase; import com.veridian.agent.repository.KnowledgeBaseRepository; import org.springframework.stereotype.Service; import java.util.*;
@Service public class KnowledgeService {
 private final KnowledgeBaseRepository repo; public KnowledgeService(KnowledgeBaseRepository r){repo=r;}
 public List<KnowledgeBase> all(){return repo.findAll();}
 public List<KnowledgeBase> search(String text){
  String q=text.toLowerCase(Locale.ROOT);
  return repo.findAll().stream().filter(k->{
   String s=(k.getPolicyId()+" "+k.getTitle()+" "+k.getContent()).toLowerCase(Locale.ROOT);
   for(String w:q.split("\\W+")) if(w.length()>=4 && s.contains(w)) return true;
   return false;
  }).limit(5).toList();
 }
}
