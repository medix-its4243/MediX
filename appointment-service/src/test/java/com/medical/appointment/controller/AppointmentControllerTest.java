package com.medical.appointment.controller;

import com.medical.appointment.client.PatientClient;
import com.medical.appointment.dto.BookingRequest;
import com.medical.appointment.service.AppointmentService;
import java.time.LocalDate;
import java.time.LocalTime;
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
class AppointmentControllerTest {
    @Mock AppointmentService service;
    @Mock PatientClient patients;
    @InjectMocks AppointmentController controller;

    @Test
    void patientCannotBookForAnotherUser() {
        BookingRequest request = new BookingRequest(5L, 9L, LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), null);
        when(patients.get(5L)).thenReturn(new PatientClient.PatientRef(5L, 22L));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.book(request, "PATIENT", 11L));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(service, never()).book(request);
    }

    @Test
    void onlyDoctorCanCompleteAppointment() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.complete(5L, "PATIENT"));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(service, never()).complete(5L);
    }
}
