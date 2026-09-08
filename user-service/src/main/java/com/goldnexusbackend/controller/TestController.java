package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goldnexus/user")
public class TestController {

    @RequestMapping("/test")
    public Res test() {
        return new Res(200,"123",null);
    }
}
