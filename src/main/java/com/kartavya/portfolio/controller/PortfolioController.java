package com.kartavya.portfolio.controller;

import com.kartavya.portfolio.service.GitHubService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortfolioController {

    private final GitHubService gitHubService;

    public PortfolioController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // GitHub pinned repos - real time
        model.addAttribute("repos", gitHubService.getPinnedRepos());

        // Profile info
        model.addAttribute("username", "kartavynirwel-code");
        model.addAttribute("githubUrl", "https://github.com/kartavynirwel-code");

        return "index";
    }

    // API endpoint to refresh cache
    @GetMapping("/refresh")
    public String refresh() {
        return "redirect:/";
    }
}