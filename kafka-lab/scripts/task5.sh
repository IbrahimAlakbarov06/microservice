#!/bin/bash
# Task 5: retry demos (payment-service on 8081, order-service on 8080)
echo "--- 5a: 80% failures, retry 3x";   curl -s -X POST "localhost:8080/orders/pay-retry?failRate=80&delayMs=0"; echo
echo "--- 5b: 400 bad request (no retry)"; curl -s -X POST "localhost:8080/orders/pay-retry?bad=true"; echo
echo "--- 5c: slow payment + idempotency key"; curl -s -X POST "localhost:8080/orders/pay-retry?orderId=ord-1&delayMs=3000"; echo
