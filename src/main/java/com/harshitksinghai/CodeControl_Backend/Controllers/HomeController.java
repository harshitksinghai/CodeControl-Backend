package com.harshitksinghai.CodeControl_Backend.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    @GetMapping
    public String homePoint(){
        System.out.println("at home");
        return "hii";
    }
}
