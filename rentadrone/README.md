# rentadrone
Rent A Drone - Drone Rental System

>**Start with [rentadrone-smarttracker-integration project](https://github.com/satspeedy/rentadrone-smarttracker-integration) first before continuing on this page.**

## Quickstart with minikube

- Download and start a minikube cluster

Steps in _project folder_:

- Setup infrastructure
```bash
docker compose -f deploy-compose/docker-compose.infra.yml up -d
```
- Upload/refresh app image to minikube
```bash
minikube image load com.hha/rentadrone:latest
``` 
- Create the app pod
```bash
kubectl apply -f deploy-k8s/app.yaml
```
- Add environment variables
```bash
kubectl set env deploy rentadrone-app AZURE_CLIENT_ID=<YOUR_AZURE_CLIENT_ID>
kubectl set env deploy rentadrone-app AZURE_CLIENT_SECRET=<YOUR_AZURE_CLIENT_SECRET>
kubectl set env deploy rentadrone-app AZURE_TENANT_ID=<YOUR_AZURE_TENANT_ID>
kubectl set env deploy rentadrone-app AZURE_VAULT_URL=<YOUR_AZURE_VAULT_URL>
kubectl set env deploy rentadrone-app GOOGLE_API_KEY=<YOUR_GOOGLE_API_KEY>
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
-p 3081:3081 \
-p 52081:52081 \
-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:25432/rentadrone-db \
-e SPRING_DATASOURCE_USERNAME=postgres \
-e SPRING_DATASOURCE_PASSWORD=postgres \
-e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
-e SPRING_KAFKA_BOOTSTRAP-SERVERS=host.docker.internal:9094 \
-e TZ=Europe/Berlin \
-e DAPR_HTTP_PORT=3081 \
-e DAPR_GRPC_PORT=52081 \
-d com.hha/rentadrone:latest
```
- Stop project
```bash
docker rm -f rentadrone-app
docker compose -f deploy-compose/docker-compose.infra.yml down --remove-orphans
```

## API Documentation
- Swagger UI: http://localhost:8181/swagger-ui.html
- OpenAPI 3 data: http://localhost:8181/v3/api-docs
- Postman Collection with example requests: [rent-a-drone.postman_collection.json](rent-a-drone.postman_collection.json)

## Open Tasks
- [ ] DroneDTO and UserDTO to decouple from domain entities
- [ ] Check reference between a drone and deliveries when deleting a drone
- [ ] ObjectMapper to avoid custom and distributed mapTo...-Methods
- [ ] Check Drone Operation Status when booking a delivery
- [ ] Delete also the assigned Tracking to a Delivery whenever a full update of the Delivery is executed because a new Tracking will also be created
- [ ] Add tests
- [ ] Pass environment variables DAPR_TRUST_ANCHORS, DAPR_CERT_CHAIN and DAPR_CERT_KEY to execute plain docker container with mTLS
- [ ] Pass environment variable GOOGLE_API_KEY to execute plain docker container with Azure key vault
- [ ] Pass environment variables AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, AZURE_TENANT_ID and AZURE_VAULT_URL to execute plain docker container with Azure key vault
