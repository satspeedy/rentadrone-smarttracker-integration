# How to add local secret store file

## Add local secret store file
- Crete `secrets.json` in folder _dapr_
  - This file is already excluded in `.gitignore`
- Add following snippet and replace missing values in <...> depending on which machine you are currently customizing it on. Use `localhost` if the component is running on the current machine. 
```shell
{
    "pubsub":{
        "redisPassword": "",
        "urlBrokerHostMachine": "<YOUR HOST_MACHINE_IP>:6379"
    },
    "statestore":{
        "redisPassword": "",
        "urlBrokerHostMachine": "<YOUR HOST_MACHINE_IP>:6379"
    },
    "secret-store-azurekeyvault-rentadrone":{
        "vaultName": "<YOUR AZURE_VAULT_URL>",
        "azureTenantId": "<YOUR AZURE_TENANT_ID>",
        "azureClientId": "<YOUR AZURE_CLIENT_ID>",
        "azureClientSecret": "<YOUR AZURE_CLIENT_SECRET>"
    },
    "binding": {
        "urlGuestMachine": "tcp://<YOUR GUEST_MACHINE_IP>:1883"
    },
    "pubsub-kafka" :{
        "urlBrokerHostMachine": "<YOUR HOST_MACHINE_IP>:9092"
    }
}
```
