package com.kartavya.portfolio.service;

import com.kartavya.portfolio.model.GitHubRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    @Value("${github.token}")
    private String token;

    @Value("${github.username}")
    private String username;

    private final WebClient webClient;

    public GitHubService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    // Fetch pinned repos using GraphQL API
    @Cacheable("pinnedRepos")
    public List<GitHubRepo> getPinnedRepos() {
        String query = """
            {
              "query": "{ user(login: \\"%s\\") { pinnedItems(first: 6, types: REPOSITORY) { nodes { ... on Repository { name description url primaryLanguage { name } stargazerCount forkCount updatedAt repositoryTopics(first: 5) { nodes { topic { name } } } } } } } }"
            }
            """.formatted(username);

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .bodyValue(query)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            return parsePinnedRepos(response);
        } catch (Exception e) {
            // Fallback to REST API if GraphQL fails
            return getFallbackRepos();
        }
    }

    @SuppressWarnings("unchecked")
    private List<GitHubRepo> parsePinnedRepos(Map<String, Object> response) {
        List<GitHubRepo> repos = new ArrayList<>();
        try {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> user = (Map<String, Object>) data.get("user");
            Map<String, Object> pinnedItems = (Map<String, Object>) user.get("pinnedItems");
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) pinnedItems.get("nodes");

            for (Map<String, Object> node : nodes) {
                GitHubRepo repo = new GitHubRepo();
                repo.setName((String) node.get("name"));
                repo.setDescription((String) node.getOrDefault("description", ""));
                repo.setHtmlUrl((String) node.get("url"));
                repo.setStars((Integer) node.getOrDefault("stargazerCount", 0));
                repo.setForks((Integer) node.getOrDefault("forkCount", 0));
                repo.setUpdatedAt((String) node.get("updatedAt"));

                // Language
                Map<String, Object> lang = (Map<String, Object>) node.get("primaryLanguage");
                if (lang != null) {
                    repo.setLanguage((String) lang.get("name"));
                }

                repos.add(repo);
            }
        } catch (Exception e) {
            return getFallbackRepos();
        }
        return repos;
    }

    // Fallback - fetch top repos via REST if GraphQL fails
    @Cacheable("fallbackRepos")
    public List<GitHubRepo> getFallbackRepos() {
        try {
            List<GitHubRepo> repos = webClient.get()
                    .uri("/users/{username}/repos?sort=updated&per_page=6&type=owner", username)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitHubRepo>>() {})
                    .block();

            if (repos != null) {
                return repos.stream()
                        .filter(r -> !r.isFork())
                        .limit(6)
                        .toList();
            }
        } catch (Exception e) {
            // Return empty if all fails
        }
        return new ArrayList<>();
    }
}