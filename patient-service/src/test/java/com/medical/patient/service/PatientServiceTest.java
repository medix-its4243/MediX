package com.medical.patient.service;

import com.medical.patient.dto.PatientRequest;
import com.medical.patient.dto.PatientResponse;
import com.medical.patient.entity.Patient;
import com.medical.patient.repository.PatientRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
    @Mock PatientRepository repository;
    @InjectMocks PatientService service;

    @Test
    void createsPatientWithValidatedProfileFields() {
        PatientRequest request = new PatientRequest(8L, " Ada ", LocalDate.of(1990, 1, 1),
                "F", "123", "Colombo", "O+");
        when(repository.save(any(Patient.class))).thenAnswer(call -> call.getArgument(0));

        PatientResponse response = service.create(request);

        assertEquals(8L, response.userId());
        assertEquals("Ada", response.name());
        assertEquals("O+", response.bloodGroup());
    }

    @Test
    void rejectsDuplicateUserProfile() {
        when(repository.existsByUserId(8L)).thenReturn(true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(new PatientRequest(8L, "Ada", null, null, null, null, null)));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void deletesOnlyExistingPatient() {
        Patient patient = new Patient();
        when(repository.findById(3L)).thenReturn(Optional.of(patient));
        service.delete(3L);
        verify(repository).delete(patient);
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.delete(4L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
