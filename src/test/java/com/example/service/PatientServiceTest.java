package com.example.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.entity.PatientEntity;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Client;
import com.example.model.Patient;
import com.example.repository.PatientRepository;

public class PatientServiceTest {
   private static final String DUMMY_NHS_NUMBER = "dummy nhs number";
   private static final Patient DUMMY_PATIENT = new Patient(DUMMY_NHS_NUMBER, "dummy name", "2001-01-01", "dummy address", "dummy postcode", "dummy telephone",
               "dummy email address");
   private static final Client DUMMY_CLIENT = new Client(42, "dummy client name");

   private PatientService patientService;
   private PatientRepository patientRepository;

   @BeforeEach
   void setUp() {
      patientRepository = mock(PatientRepository.class);
      patientService = new PatientService(patientRepository);
   }

   @Test
	void testCreateSuccess() {
		when(patientRepository.existsByNhsNumber(DUMMY_NHS_NUMBER)).thenReturn(false);

		patientService.create(DUMMY_PATIENT, DUMMY_CLIENT);

		verify(patientRepository).existsByNhsNumber(DUMMY_NHS_NUMBER);
		verify(patientRepository).save(argThat(PatientServiceTest::verifyEntity));
		verifyNoMoreInteractions(patientRepository);
	}

   @Test
	void testCreateResourceAlreadyExistsException() {
		when(patientRepository.existsByNhsNumber(DUMMY_NHS_NUMBER)).thenReturn(true);

		assertThrows(ResourceAlreadyExistsException.class, () -> patientService.create(DUMMY_PATIENT, DUMMY_CLIENT));

		verify(patientRepository).existsByNhsNumber(DUMMY_NHS_NUMBER);
		verifyNoMoreInteractions(patientRepository);
	}

   @Test
	void testCreateResourceDataIntegrityViolationException() {
		when(patientRepository.existsByNhsNumber(DUMMY_NHS_NUMBER)).thenReturn(false);
		when(patientRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

		assertThrows(DataIntegrityViolationException.class, () -> patientService.create(DUMMY_PATIENT, DUMMY_CLIENT));

		verify(patientRepository).existsByNhsNumber(DUMMY_NHS_NUMBER);
		verify(patientRepository).save(any());
		verifyNoMoreInteractions(patientRepository);
	}

   @Test
   void testUpdateSuccess() {
      PatientEntity existingPatient = new PatientEntity();
      when(patientRepository.findByNhsNumber(DUMMY_NHS_NUMBER)).thenReturn(Optional.of(existingPatient));

      patientService.update(DUMMY_PATIENT, DUMMY_CLIENT);

      verifyEntity(existingPatient);
      verify(patientRepository).findByNhsNumber(DUMMY_NHS_NUMBER);
      verify(patientRepository).save(existingPatient);
      verifyNoMoreInteractions(patientRepository);
   }

   @Test
	void testUpdateResourceNotFoundException() {
		when(patientRepository.findByNhsNumber(DUMMY_NHS_NUMBER)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> patientService.update(DUMMY_PATIENT, DUMMY_CLIENT));

		verify(patientRepository).findByNhsNumber(DUMMY_NHS_NUMBER);
		verifyNoMoreInteractions(patientRepository);
	}

   private static boolean verifyEntity(PatientEntity entity) {
      EqualsBuilder b = new EqualsBuilder();
      b.append(DUMMY_PATIENT.nhsNumber(), entity.getNhsNumber());
      b.append(DUMMY_PATIENT.name(), entity.getName());
      b.append(DUMMY_PATIENT.dateOfBirth(), entity.getDateOfBirth().toString());
      b.append(DUMMY_PATIENT.address(), entity.getAddress());
      b.append(DUMMY_PATIENT.postcode(), entity.getPostcode());
      b.append(DUMMY_PATIENT.telephoneNumber(), entity.getTelephoneNumber());
      b.append(DUMMY_PATIENT.emailAddress(), entity.getEmailAddress());
      b.append(DUMMY_CLIENT.id(), entity.getUpdatedBy());
      return b.isEquals();
   }
}
