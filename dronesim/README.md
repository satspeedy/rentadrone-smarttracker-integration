# dronesim
Drone Sim - Drone Simulator System

## Quickstart with minikube

- Download and start a minikube cluster

Steps in `rentadrone` _project_:

- Setup infrastructure
- Setup `rentadrone` project

Steps in _project folder_:

- Upload/refresh app image to minikube
```bash
minikube image load com.hha/dronesim:latest
```
- Create the app pod
```bash
kubectl apply -f deploy-k8s/app.yaml
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
Steps in `rentadrone` _project_:

- Setup infrastructure
- Setup `rentadrone` project

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
Steps in `rentadrone` _project_:

- Setup infrastructure
- Setup `rentadrone` project

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
-e SPRING_KAFKA_BOOTSTRAP-SERVERS=host.docker.internal:9092 \
-e TZ=Europe/Berlin \
-d com.hha/dronesim:latest
```
- Display and follow the app logs
```bash
docker logs -f
```
- Stop project
```bash
docker rm -f dronesim-app
```

## Open Tasks
- [ ] ...
