package com.example.jenkins_pipeline.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FailureSimulationController {
    private boolean fail = false;
    @GetMapping("/simulate-failure")
    public ResponseEntity<String> toggleFailure(@RequestParam boolean fail) {
        this.fail = fail;
        return ResponseEntity.ok("Failure simulation set to: " + fail);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(){
        if(fail){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Application is unhealthy");
        }
        return ResponseEntity.ok("Application is healthy");
    }
}
