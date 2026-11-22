package com.medical.patient.controller;

import com.medical.patient.dto.PatientRequest;
import com.medical.patient.dto.PatientResponse;
import com.medical.patient.service.PatientService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {
    @Mock PatientService service;
    @InjectMocks PatientController controller;

    @Test
    void patientCannotCreateProfileForAnotherUser() {
        PatientRequest request = new PatientRequest(22L, "Alice", null, null, null, null, null);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(request, "PATIENT", 11L));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(service, never()).create(request);
    }

    @Test
    void patientListContainsOnlyOwnProfile() {
        PatientResponse own = new PatientResponse(5L, 11L, "Alice", null, null, null, null, null);
        when(service.forUser(11L)).thenReturn(List.of(own));
        assertEquals(List.of(own), controller.list("PATIENT", 11L));
        verify(service, never()).list();
    }
}
