package com.medical.billing.dto;
import com.medical.billing.entity.Invoice;
import java.math.BigDecimal;
import java.time.Instant;
public record InvoiceResponse(Long id,Long appointmentId,Long patientId,Long doctorId,BigDecimal amount,String status,Instant createdAt,Instant paidAt) {
 public static InvoiceResponse from(Invoice i){return new InvoiceResponse(i.getId(),i.getAppointmentId(),i.getPatientId(),i.getDoctorId(),i.getAmount(),i.getStatus(),i.getCreatedAt(),i.getPaidAt());}
}
