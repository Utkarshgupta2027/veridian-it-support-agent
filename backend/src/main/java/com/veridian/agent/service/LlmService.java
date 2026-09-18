package com.veridian.agent.service;
import com.fasterxml.jackson.databind.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.net.URI; import java.net.http.*; import java.util.*;
@Service public class LlmService {
 private final ObjectMapper mapper=new ObjectMapper(); private final String key,model,base;
 public LlmService(@Value("${app.llm.api-key:}")String k,@Value("${app.llm.model:gpt-4o-mini}")String m,@Value("${app.llm.base-url:https://api.openai.com/v1}")String b){key=k;model=m;base=b;}
 public Optional<Decision> decide(String message,String knowledge){
  if(key==null||key.isBlank())return Optional.empty();
  try{
   String system="You are an internal IT support decision component. Use ONLY supplied knowledge. Never invent policy. Return JSON only with fields: category, decision, policyId, response, priority, assignedTo. decision must be RESOLVE, FOLLOW_UP, ESCALATE, or ROUTE_TO_OTHER_DEPARTMENT. If evidence is insufficient choose FOLLOW_UP or ESCALATE.";
   Map<String,Object> body=new LinkedHashMap<>(); body.put("model",model); body.put("temperature",0);
   body.put("response_format",Map.of("type","json_object"));
   body.put("messages",List.of(Map.of("role","system","content",system),Map.of("role","user","content","REQUEST:\\n"+message+"\\n\\nKNOWLEDGE:\\n"+knowledge)));
   HttpRequest req=HttpRequest.newBuilder().uri(URI.create(base+"/chat/completions")).header("Authorization","Bearer "+key).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
   HttpResponse<String> res=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());
   if(res.statusCode()<200||res.statusCode()>=300)return Optional.empty();
   JsonNode root=mapper.readTree(res.body()), json=mapper.readTree(root.path("choices").get(0).path("message").path("content").asText());
   return Optional.of(new Decision(json.path("category").asText("Unknown"),json.path("decision").asText("FOLLOW_UP"),json.path("policyId").asText("NONE"),json.path("response").asText("Please provide more information."),json.path("priority").asText("MEDIUM"),json.path("assignedTo").asText("IT")));
  }catch(Exception e){return Optional.empty();}
 }
 public record Decision(String category,String decision,String policyId,String response,String priority,String assignedTo){}
}
