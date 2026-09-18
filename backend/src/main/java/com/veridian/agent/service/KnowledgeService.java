package com.veridian.agent.service;

import com.veridian.agent.entity.KnowledgeBase;
import com.veridian.agent.repository.KnowledgeBaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class KnowledgeService {
    private final KnowledgeBaseRepository repository;

    public KnowledgeService(KnowledgeBaseRepository repository) {
        this.repository = repository;
    }

    public List<KnowledgeBase> search(String text) {
        String query = text.toLowerCase(Locale.ROOT);

        return repository.findAll().stream()
            .filter(policy -> matchesQuery(policy, query))
            .limit(5)
            .toList();
    }

    private boolean matchesQuery(KnowledgeBase policy, String query) {
        String searchableText = (
            policy.getPolicyId() + " " + policy.getTitle() + " " + policy.getContent()
        ).toLowerCase(Locale.ROOT);

        for (String word : query.split("\\W+")) {
            if (word.length() >= 4 && searchableText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
