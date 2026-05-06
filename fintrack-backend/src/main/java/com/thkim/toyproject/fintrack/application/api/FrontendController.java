package com.thkim.toyproject.fintrack.application.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
    @GetMapping(value = {"/", "/{path:^(?!api|static|h2-console|actuator).*$}/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
