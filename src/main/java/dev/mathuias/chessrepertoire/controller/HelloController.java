package dev.mathuias.chessrepertoire.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        log.info("Hello endpoint called");
        return "Hello World!";
    }
}
