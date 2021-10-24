# dronesim
Drone Sim - Drone Simulator System

>**Start with [rentadrone-smarttracker-integration project](https://github.com/satspeedy/rentadrone-smarttracker-integration) first before continuing on this page.**

## Quickstart with minikube

- Download and start a minikube cluster

Steps in `rentadrone-smarttracker-integration` _project_:

- Setup infrastructure

Steps in _project folder_:

- Upload/refresh app image to minikube
```bash
minikube image load com.hha/dronesim:latest
```
- Create the app pod
```bash
kubectl apply -f deploy-k8s/app.yaml
```
- Add environment variables
```bash
kubectl set env deploy dronesim-app GOOGLE_API_KEY=<YOUR_GOOGLE_API_KEY>
```
- Determine the pod full port
```bash
kubectl get pods | grep dronesim-app
```
- Display and follow the app logs
```bash
kubectl logs -f dronesim-app-<PREV DISPLAYED ID>
```
- Stop project
```bash
kubectl delete -f deploy-k8s/app.yaml
```

## Quickstart with docker-compose
Steps in `rentadrone-smarttracker-integration` _project_:

- Setup infrastructure

Steps in _project folder_:

- Start project
```bash
docker compose up -d
```
- Display and follow the app logs
```bash
docker logs -f dronesim-app
```
- Stop project
```bash
docker compose down --remove-orphans
```

## Quickstart with plain docker containers
Steps in `rentadrone-smarttracker-integration` _project_:

- Setup infrastructure

Steps in _project folder_:

- Build the app
```bash
mvn clean package
```
- Create the app container
>**Note to docker Usage:** It uses `host.docker.internal` to find the DB container and Kafka Broker -- different to docker-compose.

```bash
docker run \
--name dronesim-app \
--rm \
-p 8282:8282 \
-p 3082:3082 \
-p 52082:52082 \
-e SPRING_KAFKA_BOOTSTRAP-SERVERS=host.docker.internal:9094 \
-e DAPR_HTTP_PORT=3082 \
-e DAPR_GRPC_PORT=52082 \
-e GOOGLE_API_KEY=<YOUR_GOOGLE_API_KEY> \
-e TZ=Europe/Berlin \
-e NAMESPACE=default \
-d com.hha/dronesim:latest
```
- Display and follow the app logs
```bash
docker logs -f dronesim-app
```
- Stop project
```bash
docker rm -f dronesim-app
```

## Open Tasks
- [ ] Pass environment variables DAPR_TRUST_ANCHORS, DAPR_CERT_CHAIN and DAPR_CERT_KEY to execute plain docker container with mTLS
