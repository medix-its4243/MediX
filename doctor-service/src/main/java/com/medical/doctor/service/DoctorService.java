package com.medical.doctor.service;
import com.medical.doctor.dto.*;
import com.medical.doctor.entity.Doctor;
import com.medical.doctor.repository.DoctorRepository;
import java.time.DayOfWeek;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
@Transactional
public class DoctorService {
 private final DoctorRepository doctors;
 public DoctorService(DoctorRepository doctors) { this.doctors=doctors; }
 public DoctorResponse create(DoctorRequest request, String role) { admin(role); Doctor d=new Doctor(); apply(d,request); return DoctorResponse.from(doctors.save(d)); }
 public DoctorResponse update(Long id, DoctorRequest request, String role) { admin(role); Doctor d=find(id); apply(d,request); return DoctorResponse.from(doctors.save(d)); }
 public void delete(Long id,String role) { admin(role); doctors.delete(find(id)); }
 @Transactional(readOnly=true)
 public DoctorResponse get(Long id) { return DoctorResponse.from(find(id)); }
 @Transactional(readOnly=true)
 public List<DoctorResponse> list(String specialization) {
  if(specialization!=null && specialization.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Specialization must not be blank");
  return (specialization==null?doctors.findAll():doctors.findBySpecializationContainingIgnoreCase(specialization.trim())).stream().map(DoctorResponse::from).toList();
 }
 private static String normalizeDay(String value) {
  String day=value.toUpperCase(Locale.ROOT);
  for(DayOfWeek candidate:DayOfWeek.values()) {
   String shortName=candidate.name().substring(0,3);
   if(candidate.name().equals(day)||shortName.equals(day)) return shortName;
  }
  throw new IllegalArgumentException("Unknown weekday");
 }
 private Doctor find(Long id) { return doctors.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found with id "+id)); }
 private void admin(String role) { if(!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only ADMIN can manage doctors"); }
 private void apply(Doctor d,DoctorRequest r) {
  String days;
  try { days=Arrays.stream(r.availableDays().split(",",-1)).map(String::trim).map(s->normalizeDay(s)).distinct().reduce((a,b)->a+","+b).orElseThrow(); }
  catch(RuntimeException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"availableDays must contain comma-separated weekday names such as MON or MONDAY"); }
  d.setUserId(r.userId());d.setName(r.name().trim());d.setSpecialization(r.specialization().trim());d.setPhone(r.phone());d.setConsultationFee(r.consultationFee());d.setAvailableDays(days);
 }
}
