# Setup host machine related components (Dapr, Consul, RentADrone and DroneSim) on minikube

## Install minikube on your machine
- Install according to product website
  - see https://minikube.sigs.k8s.io/docs/

## Install Dapr on minikube
- Install according to product website
  - see https://docs.dapr.io/getting-started/install-dapr-cli/

## Setup Dapr on minikube
- Make sure the correct cluster is set. Set a different context using the following command:
  kubectl config use-context <CONTEXT>
- Install Dapr
  dapr init -k

## Configure Dapr on minikube
```shell
kubectl apply -f k8s/dapr/config/controlplane-config.yaml
kubectl apply -f k8s/dapr/config/sidecar-config.yaml
```

## Setup Infra outside minikube
```shell
# switch to rentadrone project and start infrastructure 
docker-compose -f deploy-compose/docker-compose.infra.yml up -d

# switch to smarttracker project and start infrastructure 
docker-compose -f deploy-compose/docker-compose.infra.yml up -d
```

## Add Dapr Components
```shell
kubectl apply -f k8s/dapr/components/binding.yaml
kubectl apply -f k8s/dapr/components/pubsub.yaml
kubectl apply -f k8s/dapr/components/pubsub_kafka.yaml
kubectl apply -f k8s/dapr/components/secretstore_azure_key_vault.yaml
kubectl apply -f k8s/dapr/components/secretstore_kubernetes.yaml
kubectl apply -f k8s/dapr/components/statestore.yaml
```

### Configure Dapr
```shell
kubectl apply -f k8s/dapr/config/controlplane-config.yaml
kubectl apply -f k8s/dapr/config/sidecar-config.yaml
```

## Create required secrets on minikube
- Replace missing values in <...> depending on which machine you are currently customizing it on. Use `host.minikube.internal` if the component is running on the current machine.
```shell
kubectl create secret generic binding-secret \
--from-literal=urlGuestMachine="tcp://<YOUR_GUEST_MACHINE_IP>:1883"

kubectl create secret generic pubsub-secret \
--from-literal=urlBrokerHostMachine="host.minikube.internal:6379" \
--from-literal=redisPassword=""

kubectl create secret generic pubsub-kafka-secret \
--from-literal=urlBrokerHostMachine=...

kubectl create secret generic secret-store-azurekeyvault-secret \
--from-literal=vaultName="<YOUR_AZURE_VAULT_URL>" \
--from-literal=azureTenantId="<YOUR_AZURE_TENANT_ID>" \
--from-literal=azureClientId="<YOUR_AZURE_CLIENT_ID>" \
--from-literal=azureClientSecret="<YOUR_AZURE_CLIENT_SECRET>"

kubectl create secret generic state-store-secret \
--from-literal=urlBrokerHostMachine="host.minikube.internal:6379" \
--from-literal=redisPassword=""
```

## Deploy rentadrone app
- Switch to rentadrone project and checkout in [README.md](https://github.com/satspeedy/rentadrone/blob/main/README.md) how to deploy on minikube

- Switch to dronesim project and checkout in [README.md](https://github.com/satspeedy/dronesim/blob/main/README.md) how to deploy on minikube

# Install Consul on your K8s/Minikube Cluster
**TODO document**
