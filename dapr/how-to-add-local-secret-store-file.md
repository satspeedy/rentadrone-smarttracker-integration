# How to add local secret store file

## Add local secret store file
- Crete `secrets.json` in folder _dapr_
  - This file is already excluded in `.gitignore`
- Add following snippet and replace missing <...> values
```shell
{
    "pubsub":{
        "redisPassword": ""
    },
    "statestore":{
        "redisPassword": ""
    },
    "secret-store-azurekeyvault-rentadrone":{
        "vaultName": "<YOUR AZURE_VAULT_URL>",
        "azureTenantId": "<YOUR AZURE_TENANT_ID>",
        "azureClientId": "<YOUR AZURE_CLIENT_ID>",
        "azureClientSecret": "<YOUR AZURE_CLIENT_SECRET>"
    }
}
```
