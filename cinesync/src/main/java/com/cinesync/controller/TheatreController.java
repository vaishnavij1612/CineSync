package com.cinesync.controller;

import com.cinesync.service.TheatreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TheatreController {
    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) { this.theatreService = theatreService; }

    @GetMapping("/theatres")
    public String theatres(Model model) {
        model.addAttribute("theatres", theatreService.findAll());
        return "theatres";
    }
}
