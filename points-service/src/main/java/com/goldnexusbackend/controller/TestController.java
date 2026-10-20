package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goldnexus/points")
public class TestController {

    @GetMapping("/ping")
    public Res ping() {
        return new Res(200, "points-service ok", null);
    }
}
