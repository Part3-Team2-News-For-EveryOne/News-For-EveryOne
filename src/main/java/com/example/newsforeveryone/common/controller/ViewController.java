package com.example.newsforeveryone.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

  @RequestMapping(value = {"/", "/interests", "/user-activities", "/articles"})
  public String forward() {
    return "forward:/index.html";
  }
}
