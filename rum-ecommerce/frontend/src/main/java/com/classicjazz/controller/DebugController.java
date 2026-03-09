package com.classicjazz.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class DebugController {

    @GetMapping("/debug/error")
    public String error() {
        throw new RuntimeException("Intentional 500 error for Datadog Error Tracking practice");
    }

    @GetMapping("/debug/slow")
    @ResponseBody
    public String slow() throws InterruptedException {
        // Sleep for 5-10 seconds to simulate a slow request
        long delay = 5000 + (long)(Math.random() * 5000);
        Thread.sleep(delay);
        return "Responded after " + delay + "ms";
    }

    @GetMapping("/debug/not-found")
    public String notFound() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Intentional 404 for Datadog Error Tracking practice");
    }
}
