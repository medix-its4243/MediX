package com.medical.billing.service;
import com.medical.billing.client.*;
import com.medical.billing.dto.*;
import com.medical.billing.entity.Invoice;
import com.medical.billing.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BillingServiceTest {
 InvoiceRepository repo=mock(InvoiceRepository.class);
 AppointmentClient appointments=mock(AppointmentClient.class);
 DoctorClient doctors=mock(DoctorClient.class);
 PatientClient patients=mock(PatientClient.class);
 BillingService service=new BillingService(repo,appointments,doctors,patients);
 @Test void snapshotsDoctorFeeForCompletedAppointment(){
  when(appointments.get(1L)).thenReturn(new AppointmentClient.AppointmentRef(1L,2L,3L,"COMPLETED"));
  when(doctors.fee(3L)).thenReturn(new BigDecimal("2500.00"));
  when(repo.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
  var result=service.create(new InvoiceRequest(1L),"STAFF");
  assertEquals(new BigDecimal("2500.00"),result.amount()); assertEquals("UNPAID",result.status());assertEquals(2L,result.patientId());
 }
 @Test void rejectsIncompleteAppointment(){
  when(appointments.get(1L)).thenReturn(new AppointmentClient.AppointmentRef(1L,2L,3L,"BOOKED"));
  assertEquals(409,assertThrows(ResponseStatusException.class,()->service.create(new InvoiceRequest(1L),"ADMIN")).getStatusCode().value());verifyNoInteractions(doctors);verify(repo,never()).saveAndFlush(any());
 }
 @Test void rejectsDuplicateInvoice(){
  when(appointments.get(1L)).thenReturn(new AppointmentClient.AppointmentRef(1L,2L,3L,"COMPLETED"));when(repo.existsByAppointmentId(1L)).thenReturn(true);
  assertEquals(409,assertThrows(ResponseStatusException.class,()->service.create(new InvoiceRequest(1L),"STAFF")).getStatusCode().value());
 }
 @Test void patientCannotWrite(){assertThrows(ResponseStatusException.class,()->service.create(new InvoiceRequest(1L),"PATIENT"));assertThrows(ResponseStatusException.class,()->service.pay(1L,"PATIENT"));verifyNoInteractions(repo,appointments);}
 @Test void patientReadUsesAuthUserId(){Invoice i=new Invoice();i.setPatientId(2L);when(repo.findById(1L)).thenReturn(Optional.of(i));when(patients.get(2L)).thenReturn(new PatientClient.PatientRef(2L,20L));assertNotNull(service.get(1L,"PATIENT",20L));assertEquals(403,assertThrows(ResponseStatusException.class,()->service.get(1L,"PATIENT",2L)).getStatusCode().value());}
 @Test void recordsPaymentOnce(){Invoice i=new Invoice();when(repo.findForPayment(1L)).thenReturn(Optional.of(i));when(repo.saveAndFlush(i)).thenReturn(i);assertEquals("PAID",service.pay(1L,"STAFF").status());assertNotNull(i.getPaidAt());assertEquals(409,assertThrows(ResponseStatusException.class,()->service.pay(1L,"STAFF")).getStatusCode().value());}
 @Test void missingInvoiceIsNotFound(){assertEquals(404,assertThrows(ResponseStatusException.class,()->service.get(1L,"STAFF",null)).getStatusCode().value());}
}
