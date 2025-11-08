package org.example.controller;

import org.example.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/required")
    public String testRequired() {
        return transactionService.testRequired();
    }

    @GetMapping("/supports")
    public String testSupports() {
        return transactionService.testSupports();
    }

    @GetMapping("/mandatory")
    public String testMandatory() {
        return transactionService.testMandatory();
    }

    @GetMapping("/requires-new")
    public String testRequiresNew() {
        return transactionService.testRequiresNew();
    }

    @GetMapping("/not-supported")
    public String testNotSupported() {
        return transactionService.testNotSupported();
    }

    @GetMapping("/never")
    public String testNever() {
        return transactionService.testNever();
    }

    @GetMapping("/nested")
    public String testNested() {
        return transactionService.testNested();
    }
}