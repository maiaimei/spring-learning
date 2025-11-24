package org.example.controller;

import org.example.service.TransactionDebugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class TransactionDebugController {

    @Autowired
    private TransactionDebugService debugService;

    @GetMapping("/required-nested")
    public String testRequiredWithNested() {
        return debugService.demonstrateRequiredWithNested();
    }

    @GetMapping("/required-new")
    public String testRequiredWithRequiresNew() {
        return debugService.demonstrateRequiredWithRequiresNew();
    }
}