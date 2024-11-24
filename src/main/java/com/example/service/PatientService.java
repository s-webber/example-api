package com.example.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.entity.PatientEntity;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Client;
import com.example.model.Patient;
import com.example.repository.PatientRepository;

@Service
public class PatientService {
   private final PatientRepository patientRepository;

   public PatientService(PatientRepository patientRepository) {
      this.patientRepository = patientRepository;
   }

   public void create(Patient patient, Client client) {
      if (patientRepository.existsByNhsNumber(patient.nhsNumber())) {
         throw new ResourceAlreadyExistsException();
      }

      PatientEntity entity = new PatientEntity();
      mapToEntity(entity, patient, client);
      patientRepository.save(entity);
   }

   public void update(Patient patient, Client client) {
      PatientEntity entity = patientRepository.findByNhsNumber(patient.nhsNumber()).orElseThrow(ResourceNotFoundException::new);
      mapToEntity(entity, patient, client);
      patientRepository.save(entity);
   }

   private void mapToEntity(PatientEntity entity, Patient model, Client client) {
      entity.setNhsNumber(model.nhsNumber());
      entity.setName(model.name());
      entity.setDateOfBirth(LocalDate.parse(model.dateOfBirth()));
      entity.setAddress(model.address());
      entity.setPostcode(model.postcode());
      entity.setTelephoneNumber(model.telephoneNumber());
      entity.setEmailAddress(model.emailAddress());
      entity.setUpdatedBy(client.id());
   }
}
