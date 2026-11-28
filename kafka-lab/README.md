# kafka-lab
Week 11-12: Kafka ordering + Resilience4j.
1. `docker compose up -d`
2. Run payment-service (8081) and order-service (8080, JDK 17).
   Flags: `--lab.mode=ordering|db --lab.crash=true --lab.idempotent=true`
3. `scripts/task1.sh`, `task5.sh`, `task6.sh`; Task 2/3: `POST /orders/send?keyed=true|false`, `/orders/send-db`.
