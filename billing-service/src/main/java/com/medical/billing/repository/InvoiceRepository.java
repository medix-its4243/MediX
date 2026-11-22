package com.medical.billing.repository;
import com.medical.billing.entity.Invoice;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
 boolean existsByAppointmentId(Long appointmentId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from Invoice i where i.id=:id")
 Optional<Invoice> findForPayment(@Param("id") Long id);
}
