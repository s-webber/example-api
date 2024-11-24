package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Client;
import com.example.model.Patient;
import com.example.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/patient")
public class PatientController {
   private final PatientService patientService;

   public PatientController(PatientService patientService) {
      this.patientService = patientService;
   }

   @PostMapping("/create")
   @ResponseStatus(HttpStatus.CREATED)
   public void create(@RequestAttribute Client client, @Valid @RequestBody Patient patient) {
      patientService.create(patient, client);
   }

   @PostMapping("/update")
   public void update(@RequestAttribute Client client, @Valid @RequestBody Patient patient) {
      patientService.update(patient, client);
   }
}
