

## Install K8s/Minikube on your machine

## Install Dapr on your machine


## Setup Dapr on K8s
- Make sure the correct cluster is set. Set a different context using the following command:
  kubectl config use-context <CONTEXT>
- Install Dapr
  dapr init -k

## Configure Dapr
kubectl apply -f dapr/k8s/config/controlplane-config.yaml
kubectl apply -f dapr/k8s/config/sidecar-config.yaml

## Install Infra outside K8s

## Add Dapr Components
kubectl apply -f dapr/k8s/components/binding.yaml
kubectl apply -f dapr/k8s/components/pubsub.yaml
kubectl apply -f dapr/k8s/components/pubsub_kafka.yaml
kubectl apply -f dapr/k8s/components/secretstore_azure_key_vault.yaml
kubectl apply -f dapr/k8s/components/secretstore_kubernetes.yaml
kubectl apply -f dapr/k8s/components/statestore.yaml

### Kubernetes: Configure Dapr on K8s
```shell
kubectl apply -f ../dapr/k8s/config/controlplane-config.yaml
kubectl apply -f ../dapr/k8s/config/sidecar-config.yaml
```

## K8s secrets
kubectl create secret generic binding-secret \
--from-literal=urlGuestMachine=...

kubectl create secret generic pubsub-secret \
--from-literal=urlBrokerHostMachine=... \
--from-literal=redisPassword=...

kubectl create secret generic pubsub-kafka-secret \
--from-literal=urlBrokerHostMachine=...

kubectl create secret generic secret-store-azurekeyvault-secret \
--from-literal=vaultName=... \
--from-literal=azureTenantId=... \
--from-literal=azureClientId=... \
--from-literal=azureClientSecret=...

kubectl create secret generic state-store-secret \
--from-literal=urlBrokerHostMachine=... \
--from-literal=redisPassword=...

## Deploy rentadrone app
Switch to rentadrone project and
Check in Readme how to deploy on K8s

## Deploy dronesim app
Switch to dronesim project and
Check in Readme how to deploy on K8s

# Install Consul on your K8s/Minikube Cluster

