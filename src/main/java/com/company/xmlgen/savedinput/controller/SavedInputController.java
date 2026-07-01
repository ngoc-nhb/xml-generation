package com.company.xmlgen.savedinput.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.savedinput.dto.response.SavedInputResponse;
import com.company.xmlgen.savedinput.service.SavedInputService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Saved Input HTTP endpoints.
 */
@RestController
@RequestMapping("/api/v1/saved-inputs")
public class SavedInputController {

    private final SavedInputService savedInputService;

    public SavedInputController(SavedInputService savedInputService) {
        this.savedInputService = savedInputService;
    }

    @GetMapping("/template/{templateId}")
    public ApiResponse<SavedInputResponse> findByTemplate(@PathVariable Long templateId) {
        return ApiResponse.ok(savedInputService.findByTemplate(templateId));
    }
}
