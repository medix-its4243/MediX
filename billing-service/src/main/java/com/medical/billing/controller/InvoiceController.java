package com.medical.billing.controller;
import com.medical.billing.dto.*;
import com.medical.billing.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/invoices")
public class InvoiceController {
 private final BillingService billing;
 public InvoiceController(BillingService billing){this.billing=billing;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public InvoiceResponse create(@Valid @RequestBody InvoiceRequest request,@RequestHeader(value="X-User-Role",required=false) String role){return billing.create(request,role);}
}
