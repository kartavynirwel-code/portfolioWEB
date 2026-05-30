package com.kartavya.portfolio.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepo {

    private String name;

    private String description;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String language;

    @JsonProperty("stargazers_count")
    private int stars;

    @JsonProperty("forks_count")
    private int forks;

    @JsonProperty("updated_at")
    private String updatedAt;

    private boolean fork;

    private String[] topics;

    // Display name - formatted version of repo name
    public String getDisplayName() {
        if (name == null) return "";
        return name.replace("-", " ")
                .replace("_", " ");
    }

    // Language color for display
    public String getLanguageColor() {
        if (language == null) return "#8b949e";
        return switch (language.toLowerCase()) {
            case "java" -> "#b07219";
            case "javascript" -> "#f1e05a";
            case "python" -> "#3572A5";
            case "html" -> "#e34c26";
            case "css" -> "#563d7c";
            case "typescript" -> "#2b7489";
            default -> "#8b949e";
        };
    }

    // Icon based on repo name/description
    public String getIcon() {
        if (name == null) return "🔧";
        String n = name.toLowerCase();
        if (n.contains("ai") || n.contains("assistant")) return "🤖";
        if (n.contains("career") || n.contains("student")) return "🎓";
        if (n.contains("browser")) return "🌐";
        if (n.contains("snap") || n.contains("photo") || n.contains("image")) return "📸";
        if (n.contains("gravity")) return "🚀";
        if (n.contains("library")) return "📚";
        if (n.contains("cicd") || n.contains("pipeline") || n.contains("spring-boot-cicd")) return "⚙️";
        if (n.contains("portfolio")) return "💼";
        return "💻";
    }
}