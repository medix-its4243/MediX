package com.medical.billing.client;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
@Component
public class DoctorClient {
 private final KafkaLookupClient lookups;
 public DoctorClient(KafkaLookupClient lookups) {this.lookups=lookups;}
 public BigDecimal fee(Long id) {
  var result=lookups.lookup("doctor.lookup.requests",id);
  if(!result.path("found").asBoolean() || !result.path("consultationFee").isNumber()) return null;
  return result.path("consultationFee").decimalValue();
 }
}
