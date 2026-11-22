package com.medical.billing.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="invoices")
public class Invoice {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private Long appointmentId;
 @Column(nullable=false) private Long patientId;
 @Column(nullable=false) private Long doctorId;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false) private String status="UNPAID";
 @Column(nullable=false) private Instant createdAt=Instant.now();
 private Instant paidAt;
 @Version private Long version;
 public Long getId(){return id;}
 public Long getAppointmentId(){return appointmentId;} public void setAppointmentId(Long v){appointmentId=v;}
 public Long getPatientId(){return patientId;} public void setPatientId(Long v){patientId=v;}
 public Long getDoctorId(){return doctorId;} public void setDoctorId(Long v){doctorId=v;}
 public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
 public String getStatus(){return status;}
 public Instant getCreatedAt(){return createdAt;} public Instant getPaidAt(){return paidAt;}
 public void pay(){status="PAID";paidAt=Instant.now();}
}
