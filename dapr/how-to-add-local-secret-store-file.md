# How to add local secret store file

## Add local secret store file
- Crete `secrets.json` in folder _dapr_
  - This file is already excluded in `.gitignore`

### Host machine
- Add following snippet and replace missing values in <...> depending on which machine you are currently customizing it on. Use `localhost` if the component is running on the current machine. 
```shell
{
    "pubsub":{
        "redisPassword": "",
        "urlBrokerHostMachine": "localhost:6379"
    },
    "statestore":{
        "redisPassword": "",
        "urlBrokerHostMachine": "localhost:6379"
    },
    "secret-store-azurekeyvault-rentadrone":{
        "vaultName": "<YOUR_AZURE_VAULT_URL>",
        "azureTenantId": "<YOUR_AZURE_TENANT_ID>",
        "azureClientId": "<YOUR_AZURE_CLIENT_ID>",
        "azureClientSecret": "<YOUR_AZURE_CLIENT_SECRET>"
    },
    "binding": {
        "urlGuestMachine": "tcp://<YOUR_GUEST_MACHINE_IP>:1883"
    },
    "pubsub-kafka" :{
        "urlBrokerHostMachine": "localhost:9092"
    }
}
```
### Guest machine
- Add following snippet and replace missing values in <...> depending on which machine you are currently customizing it on. Use `localhost` if the component is running on the current machine.
```shell
{
  "pubsub":{
    "redisPassword": "",
    "urlBrokerHostMachine": "<YOUR_HOST_MACHINE_IP>:6379"
  },
  "statestore":{
    "redisPassword": "",
    "urlBrokerHostMachine": "<YOUR_HOST_MACHINE_IP>:6379"
  },
  "secret-store-azurekeyvault-rentadrone":{
        "vaultName": "<YOUR_AZURE_VAULT_URL>",
        "azureTenantId": "<YOUR_AZURE_TENANT_ID>",
        "azureClientId": "<YOUR_AZURE_CLIENT_ID>",
        "azureClientSecret": "<YOUR_AZURE_CLIENT_SECRET>"
  },
  "binding": {
    "urlGuestMachine": "tcp://localhost:1883"
  },
  "pubsub-kafka" :{
    "urlBrokerHostMachine": "<YOUR_HOST_MACHINE_IP>:29094"
  }
}
```
