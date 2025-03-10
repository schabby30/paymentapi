## Kibit Home Assignment : Instant Payment API
## Sebestyen Szabo

---
### dependencies
    java 21
    maven 3.8.8
    docker 27.3.1
    docker compose v2.30.3-desktop.1

---
### running
#### in a terminal go to the project folder ../paymentapi and run the following command:
> mvn clean package
> docker compose up --build -d

### testing
#### in a terminal go to the project folder ../paymentapi and run the following command:
> mvn test

---
### OPEN API
> http://localhost:8082/payment/api/v3/api-docs

### SWAGGER UI
> http://localhost:8082/payment/api/swagger-ui/index.html

---
### POSTMAN testing request is in the folder

---