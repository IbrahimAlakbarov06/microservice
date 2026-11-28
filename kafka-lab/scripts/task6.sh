#!/bin/bash
# Task 6: circuit breaker. Stop payment-service first, then run this; restart it around call 12.
for i in $(seq 1 15); do
  echo -n "call $i: "; curl -s -X POST "localhost:8080/orders/pay-cb?orderId=o$i"; echo
  sleep 1
done
curl -s localhost:8080/actuator/circuitbreakerevents/payment-cb
