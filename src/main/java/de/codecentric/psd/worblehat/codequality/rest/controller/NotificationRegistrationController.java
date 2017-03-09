package de.codecentric.psd.worblehat.codequality.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationRegistrationController {

    @RequestMapping("/register")
    public String register() {
        return "Hello World";
    }
}
