package com.projekt.uprzejmiedonosze.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
