package com.medical.billing.service;
import com.medical.billing.client.*;
import com.medical.billing.dto.*;
import com.medical.billing.entity.Invoice;
import com.medical.billing.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service @Transactional
public class BillingService {
 private final InvoiceRepository invoices; private final AppointmentClient appointments; private final DoctorClient doctors; private final PatientClient patients;
 public BillingService(InvoiceRepository invoices,AppointmentClient appointments,DoctorClient doctors,PatientClient patients){this.invoices=invoices;this.appointments=appointments;this.doctors=doctors;this.patients=patients;}
 public InvoiceResponse create(InvoiceRequest request,String role){
  manage(role);
  var a=appointments.get(request.appointmentId());
  if(a==null) throw error(HttpStatus.BAD_REQUEST,"Appointment does not exist");
  if(!"COMPLETED".equals(a.status())) throw error(HttpStatus.CONFLICT,"Appointment must be COMPLETED");
  if(invoices.existsByAppointmentId(a.id())) throw error(HttpStatus.CONFLICT,"Invoice already exists for appointment");
  var fee=doctors.fee(a.doctorId());
  if(fee==null || fee.signum()<0 || fee.scale()>2 || fee.precision()-fee.scale()>10) throw error(HttpStatus.BAD_REQUEST,"Doctor consultation fee is missing or invalid");
  Invoice i=new Invoice();i.setAppointmentId(a.id());i.setPatientId(a.patientId());i.setDoctorId(a.doctorId());i.setAmount(fee);
  return InvoiceResponse.from(invoices.saveAndFlush(i));
 }
 private void manage(String role){if(!"ADMIN".equals(role)&&!"STAFF".equals(role)) throw error(HttpStatus.FORBIDDEN,"Only ADMIN and STAFF can manage invoices");}
 private ResponseStatusException error(HttpStatus status,String message){return new ResponseStatusException(status,message);}
}
