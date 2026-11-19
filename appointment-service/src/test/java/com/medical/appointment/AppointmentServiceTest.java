package com.medical.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository repository;
    @Mock PatientClient patients;
    @Mock DoctorClient doctors;
    @Mock EntityManager entityManager;
    @Mock Query lockQuery;
    @InjectMocks AppointmentService service;

    private final LocalDate date = LocalDate.now().plusDays(2);
    private BookingRequest request() {
        return new BookingRequest(1L, 2L, date, LocalTime.of(10, 0), LocalTime.of(10, 30), "Checkup");
    }

    private void validReferencesAndLock() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(doctors.get(2L)).thenReturn(new DoctorClient.DoctorRef(2L,
                date.getDayOfWeek().name().substring(0, 3)));
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(lockQuery);
        when(lockQuery.setParameter(eq("key"), anyLong())).thenReturn(lockQuery);
    }

    @Test
    void rejectsOverlappingBookedSlot() {
        validReferencesAndLock();
        when(repository.hasOverlap(2L, date, LocalTime.of(10, 0), LocalTime.of(10, 30))).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.book(request()));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void booksFreeSlotAfterReferenceChecks() {
        validReferencesAndLock();
        when(repository.save(any(Appointment.class))).thenAnswer(call -> call.getArgument(0));

        AppointmentResponse response = service.book(request());

        assertEquals(AppointmentStatus.BOOKED, response.status());
        assertEquals(1L, response.patientId());
        verify(lockQuery).getSingleResult();
    }

    @Test
    void rejectsInvalidTimeRangeBeforeCallingServices() {
        BookingRequest invalid = new BookingRequest(1L, 2L, date, LocalTime.NOON, LocalTime.NOON, null);
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.book(invalid));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(patients, never()).get(anyLong());
    }

    @Test
    void rejectsDayWhenDoctorIsUnavailable() {
        when(patients.get(1L)).thenReturn(new PatientClient.PatientRef(1L, 5L));
        when(doctors.get(2L)).thenReturn(new DoctorClient.DoctorRef(2L, "NODAY"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.book(request()));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void onlyBookedAppointmentsCanComplete() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(repository.findForUpdate(7L)).thenReturn(Optional.of(appointment));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.complete(7L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }
}
