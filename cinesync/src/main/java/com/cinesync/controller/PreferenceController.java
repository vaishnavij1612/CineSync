package com.cinesync.controller;

import com.cinesync.dto.PreferenceForm;
import com.cinesync.dto.PreferenceView;
import com.cinesync.model.ShowtimePreference;
import com.cinesync.service.GenreService;
import com.cinesync.service.PreferenceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/preferences")
public class PreferenceController {
    private final PreferenceService preferenceService;
    private final GenreService genreService;

    public PreferenceController(PreferenceService preferenceService, GenreService genreService) {
        this.preferenceService = preferenceService;
        this.genreService = genreService;
    }

    @GetMapping
    public String preferences(Authentication authentication, Model model) {
        PreferenceView current = preferenceService.getCurrentPreferences(authentication.getName());
        PreferenceForm form = new PreferenceForm();
        form.setGenreIds(new java.util.LinkedHashSet<>(current.preferredGenreIds()));
        form.setShowtimePreference(current.showtimePreference());
        addModel(model, form);
        return "preferences";
    }

    @PostMapping
    public String update(@ModelAttribute("preferenceForm") PreferenceForm form,
                         Authentication authentication, Model model) {
        try {
            preferenceService.update(authentication.getName(), form);
            return "redirect:/preferences?updated";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("preferenceError", exception.getMessage());
            addModel(model, form);
            return "preferences";
        }
    }

    @ModelAttribute("showtimePreferences")
    public ShowtimePreference[] showtimePreferences() { return ShowtimePreference.values(); }

    private void addModel(Model model, PreferenceForm form) {
        model.addAttribute("preferenceForm", form);
        model.addAttribute("genres", genreService.findAll());
    }
}
