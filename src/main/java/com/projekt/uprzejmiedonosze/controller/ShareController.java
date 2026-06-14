package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.service.ShareService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping("/share/{token}")
    public String showSharedReport(@PathVariable String token, Model model) {
        model.addAttribute("report", shareService.findByShareToken(token));
        return "share/view";
    }
}