package com.medical.record.service;

import com.medical.record.client.AppointmentClient;
import com.medical.record.client.PatientClient;
import com.medical.record.dto.RecordRequest;
import com.medical.record.dto.RecordResponse;
import com.medical.record.entity.MedicalRecord;
import com.medical.record.repository.MedicalRecordRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {
    @Mock MedicalRecordRepository repository;
    @Mock PatientClient patients;
    @Mock AppointmentClient appointments;
    @InjectMocks MedicalRecordService service;

    private RecordRequest request() { return new RecordRequest(1L, 2L, 3L, "Flu", "Rest", null); }

    @Test
    void onlyDoctorCanCreate() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(), "PATIENT"));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(patients, never()).get(1L);
    }

    @Test
    void rejectsRecordForMismatchedAppointment() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(appointments.get(3L)).thenReturn(new AppointmentClient.AppointmentRef(3L, 9L, 2L, "COMPLETED"));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(), "DOCTOR"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rejectsRecordUntilAppointmentCompleted() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(appointments.get(3L)).thenReturn(new AppointmentClient.AppointmentRef(3L, 1L, 2L, "BOOKED"));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(), "DOCTOR"));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void createsRecordForCompletedMatchingAppointment() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(appointments.get(3L)).thenReturn(new AppointmentClient.AppointmentRef(3L, 1L, 2L, "COMPLETED"));
        when(repository.save(any(MedicalRecord.class))).thenAnswer(call -> call.getArgument(0));

        RecordResponse response = service.create(request(), "DOCTOR");

        assertEquals("Flu", response.diagnosis());
        assertEquals(3L, response.appointmentId());
    }

    @Test
    void rejectsSecondRecordForSameAppointment() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(appointments.get(3L)).thenReturn(new AppointmentClient.AppointmentRef(3L, 1L, 2L, "COMPLETED"));
        when(repository.existsByAppointmentId(3L)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(), "DOCTOR"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void patientCanReadOnlyOwnHistory() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(repository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        assertEquals(List.of(), service.forPatient(1L, "PATIENT", 5L));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.forPatient(1L, "PATIENT", 6L));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }
}
