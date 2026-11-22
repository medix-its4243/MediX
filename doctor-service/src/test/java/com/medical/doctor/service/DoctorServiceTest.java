package com.medical.doctor.service;
import com.medical.doctor.dto.*;
import com.medical.doctor.entity.Doctor;
import com.medical.doctor.repository.DoctorRepository;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class DoctorServiceTest {
 DoctorRepository repo=mock(DoctorRepository.class);DoctorService service=new DoctorService(repo);
 DoctorRequest request(String days){return new DoctorRequest(1L," Doctor ","Cardiology",null,new BigDecimal("2500.00"),days);}
 @Test void normalizesDaysAndName(){when(repo.save(any())).thenAnswer(i->i.getArgument(0));var d=service.create(request("monday, FRI,monday"),"ADMIN");assertEquals("MON,FRI",d.availableDays());assertEquals("Doctor",d.name());}
 @Test void rejectsInvalidDays(){assertEquals(400,assertThrows(ResponseStatusException.class,()->service.create(request("FUNDAY"),"ADMIN")).getStatusCode().value());verify(repo,never()).save(any());}
 @Test void rejectsNonAdminWrites(){for(String role:List.of("PATIENT","STAFF","DOCTOR")){assertThrows(ResponseStatusException.class,()->service.create(request("MONDAY"),role));assertThrows(ResponseStatusException.class,()->service.update(1L,request("MONDAY"),role));assertThrows(ResponseStatusException.class,()->service.delete(1L,role));}verifyNoInteractions(repo);}
 @Test void searchesSpecialization(){when(repo.findBySpecializationContainingIgnoreCase("cardio")).thenReturn(List.of(new Doctor()));assertEquals(1,service.list(" cardio ").size());}
 @Test void updateAndDeleteRequireExistingDoctor(){assertEquals(404,assertThrows(ResponseStatusException.class,()->service.update(1L,request("MONDAY"),"ADMIN")).getStatusCode().value());assertEquals(404,assertThrows(ResponseStatusException.class,()->service.delete(1L,"ADMIN")).getStatusCode().value());}
}
