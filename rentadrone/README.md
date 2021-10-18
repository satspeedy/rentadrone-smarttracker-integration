# rentadrone
Rent A Drone - Drone Rental System

## Quickstart with minikube

- Download and start a minikube cluster

Steps in _project folder_:

- Setup infrastructure
```bash
docker compose -f deploy-compose/docker-compose.infra.yml up -d
```
- Create the database pod
```bash
kubectl apply -f deploy-k8s/db.yaml
```
- Upload/refresh app image to minikube
```bash
minikube image load com.hha/rentadrone:latest
``` 
- Create the app pod
```bash
kubectl apply -f deploy-k8s/app.yaml
```
- Determine the current port
```bash
minikube service rentadrone-app --url
```
- Send requests to app via host `localhost` and given port. E.g., get all drones and format with `jq`
```bash
curl -X 'GET' 'http://localhost:<MINIKUBE PORT>/api/drones' -H 'accept: application/json' | jq
```
- Stop project
```bash
kubectl delete -f deploy-k8s/app.yaml
kubectl delete -f deploy-k8s/db.yaml
docker compose -f deploy-compose/docker-compose.infra.yml down --remove-orphans
```

## Quickstart with docker-compose
Steps in _project folder_:

- Setup project
```bash
docker compose up -d
```
- Stop project
```bash
docker compose down --remove-orphans
```

## Quickstart with plain docker containers
Steps in _project folder_:

- Setup Kafka Broker
```bash
docker compose -f deploy-compose/docker-compose.infra.yml up -d
```
- Create the database container
```bash
docker run \
--name rentadrone-db \
--rm \
-p 25432:5432 \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=postgres \
-e POSTGRES_DB=rentadrone-db \
-e TZ=Europe/Berlin \
-d postgres:13.1-alpine
```
- Build the app
```bash
mvn clean package
```
- Create the app container
>**Note to docker Usage:** It uses `host.docker.internal` to find the database container and Kafka Broker -- different to docker-compose.

```bash
docker run \
--name rentadrone-app \
--rm \
-p 8181:8181 \
-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:25432/rentadrone-db \
-e SPRING_DATASOURCE_USERNAME=postgres \
-e SPRING_DATASOURCE_PASSWORD=postgres \
-e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
-e SPRING_KAFKA_BOOTSTRAP-SERVERS=host.docker.internal:19092 \
-e TZ=Europe/Berlin \
-d com.hha/rentadrone:latest
```
- Stop project
```bash
docker rm -f rentadrone-app rentadrone-db
docker compose -f deploy-compose/docker-compose.infra.yml down --remove-orphans
```

## API Documentation
- Swagger UI: http://localhost:8181/swagger-ui.html
- OpenAPI 3 data: http://localhost:8181/v3/api-docs
- Postman Collection with example requests: [rent-a-drone.postman_collection.json](rent-a-drone.postman_collection.json)

## Further links
- UI to explore, publish and subscribe kafka messages: https://www.getkadeck.com/#/
- UI to explore database: https://www.pgadmin.org/

## Open Tasks
- [ ] No handling of exceptional cases because it is a demo project
- [ ] DroneDTO and UserDTO to decouple from domain entities
- [ ] Check reference between a drone and deliveries when deleting a drone
- [ ] ObjectMapper to avoid custom and distributed mapTo...-Methods
- [ ] Check Drone Operation Status when booking a delivery
