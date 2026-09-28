package com.cinesync.controller;

import com.cinesync.service.ShowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShowController {
    private final ShowService showService;

    public ShowController(ShowService showService) { this.showService = showService; }

    @GetMapping("/shows")
    public String shows(Model model) {
        model.addAttribute("shows", showService.findUpcoming());
        return "shows";
    }
}
