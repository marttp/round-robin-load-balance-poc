# POC - HTTP Round Robin API

## Simple API

In order to mock behavior of round-robin load balancer, need to simulate the environment to mock behavior.

### Start service

```bash
# Terminal 1
./simple-service/mvnw -f simple-service/pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# Terminal 2  
./simple-service/mvnw -f simple-service/pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

# Terminal 3
./simple-service/mvnw -f simple-service/pom.xml spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
```

### curl command

```bash
curl -X POST http://localhost:8080/api/v1/echo \
  -H "Content-Type: application/json" \
  -d '{"game":"Mobile Legends", "gamerID":"GYUTDTE", "points":20}'
```