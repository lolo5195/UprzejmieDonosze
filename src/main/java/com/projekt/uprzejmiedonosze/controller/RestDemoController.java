package com.projekt.uprzejmiedonosze.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RestDemoController {

    @GetMapping("/rest-demo")
    public String showRestDemo() {
        return "rest-demo";
    }
}