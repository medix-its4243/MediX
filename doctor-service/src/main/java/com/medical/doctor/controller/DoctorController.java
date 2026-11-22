package com.medical.doctor.controller;
import com.medical.doctor.dto.*;
import com.medical.doctor.service.DoctorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/doctors")
public class DoctorController {
 private final DoctorService doctors;
 public DoctorController(DoctorService doctors) { this.doctors=doctors; }
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public DoctorResponse create(@Valid @RequestBody DoctorRequest request,@RequestHeader(value="X-User-Role",required=false) String role) { return doctors.create(request,role); }
 @GetMapping public List<DoctorResponse> list() { return doctors.list(null); }
 @GetMapping("/search") public List<DoctorResponse> search(@RequestParam String specialization) { return doctors.list(specialization); }
 @GetMapping("/{id}") public DoctorResponse get(@PathVariable Long id) { return doctors.get(id); }
}
