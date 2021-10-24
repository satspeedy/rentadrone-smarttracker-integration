# rentadrone-smarttracker-integration
Integration of Rent A Drone (Drone Rental System) and SmartTracker (Live-Tracking System)

## Goal
Project work for obtaining the academic degree Master of Science (M.Sc.) in the part-time study program Business Information Systems.

Title of the project work: **Practical introduction to Distributed Application Runtime and Service Mesh for architecture integration**

Two independent systems are being developed. Use cases that span both systems are realized with the help of Dapr and Consul. Consequently, both system architectures are integrated with each other.

Contact: https://github.com/satspeedy

## Services
| Service      | Operates on   | App Port | Dapr sidecar HTTP port | Dapr sidecar gRPC port |
| -------------| ------------- | -------- | ---------------------- | ---------------------- |
| RentADrone   | Host Machine  | 8181     | 3081                   | 52081                  |
| DroneSim     | Host Machine  | 8282     | 3082                   | 52082                  |
| SmartTracker | Guest Machine | 8383     | 3083                   | 52083                  |

## Repositories
| Service      | GitHub Repo                                |
| -------------| ------------------------------------------ |
| RentADrone   | https://github.com/satspeedy/rentadrone    |
| DroneSim     | https://github.com/satspeedy/dronesim      |
| SmartTracker | https://github.com/satspeedy/smarttracker  |

## Components ()
Docker/Compose/K8s

| Type         | Component                     | Part of      | Operates on   | Host port |Docker/Compos/K8s| Ext port  |
| ------------ | ------------------------------| -------------| ------------- | --------- | --------------- |---------- |
| PubSub       | Kafka                         | RentADrone   | Host Machine  | 9092      | 9094/29092/9093 | 29094     |
| DB           | PostgreSQL                    | RentADrone   | Host Machine  | 25432     | 5432            | -         |
| StateStore   | Redis                         | SmartTracker | Guest Machine | 26379     | 6379            | 26379     |
| Binding      | Eclipse Mosquitto MQTT Broker | SmartTracker | Guest Machine | 1883      | 1883            | 1883      |
| StateStore   | Redis (Provided by Dapr)      | Shared       | Host Machine  | 6379      | -               | 6379      |
| SecretStore  | Azure Key Vault               | Shared       | Azure         | -         | -               | -         |
| SecretStore  | Local file                    | -            | Host & Guest  | -         | -               | -         |

## Setup
>**Note: Scope is to set up everything on VMs. Checkout [install-k8s-minikube.md](k8s/install-k8s-minikube.md) to set up host machine related components (Dapr, Consul, RentADrone and DroneSim) on minikube.**

- Clone git repositories in same folder on host machine
```shell
mkdir -p $HOME/projectwork
cd $HOME/projectwork
git clone https://github.com/satspeedy/rentadrone-smarttracker-integration
git clone https://github.com/satspeedy/rentadrone
git clone https://github.com/satspeedy/dronesim
```

- Clone git repositories in same folder on guest machine
```shell
mkdir -p $HOME/projectwork
cd $HOME/projectwork
git clone https://github.com/satspeedy/rentadrone-smarttracker-integration
git clone https://github.com/satspeedy/smarttracker
```

- Install consul according to product website on host and guest machine
  - see https://www.consul.io/docs/install#install-consul

- Install envoy according to func-e website and add binary to PATH on host and guest machine
  - see https://func-e.io/

- Install dapr according to product website on host and guest machine
  - see https://docs.dapr.io/getting-started/install-dapr-cli/

- Configure Dapr on host machine
  - Add adjusted dapr configuration file `dapr/config/advanced-config.yaml` in user folder as `config.yaml`
    - Windows: `%USERPROFILE%\.dapr\`
    - Linux: `$HOME/.dapr`
  - Add local secret store file
    - see [dapr/how-to-add-local-secret-store-file.md](dapr/how-to-add-local-secret-store-file.md)
  - Download sentry binary and unzip to folder `$HOME/.dapr`
    - See https://github.com/dapr/dapr/releases
  - Create a directory for sentry self signed root certs
    - `mkdir -p $HOME/.dapr/certs`

- Configure Dapr on guest machine
  - Add adjusted dapr configuration file `dapr/config/advanced-config.yaml` in user folder as `config.yaml` and replace _tracing_ endpointAddress `localhost` via host machine ip
    - Windows: `%USERPROFILE%\.dapr\`
    - Linux: `$HOME/.dapr`
  - Add local secret store file
    - see [dapr/how-to-add-local-secret-store-file.md](dapr/how-to-add-local-secret-store-file.md)
  - Download sentry binary and unzip to folder `$HOME/.dapr`
    - See https://github.com/dapr/dapr/releases
  - Create a directory for sentry self signed root certs
    - `mkdir -p $HOME/.dapr/certs`

- Set required environment variables
>**Note: Add also in IDE to Run/Debug directly from IDE and furthermore add the individual DAPR_HTTP_PORT=... and DAPR_GRPC_PORT=... per service (see below for individual ports).**
```bash
# Linux
## Open the current user’s .bashrc file
vi ~/.bashrc
## Add the export command for every environment variable and save
export AZURE_CLIENT_ID="<YOUR_AZURE_CLIENT_ID>"
export AZURE_CLIENT_SECRET="<YOUR_AZURE_CLIENT_SECRET>"
export AZURE_TENANT_ID="<YOUR_AZURE_TENANT_ID>"
export AZURE_VAULT_URL="<YOUR_AZURE_VAULT_URL>"
export GOOGLE_API_KEY="<YOUR_GOOGLE_API_KEY>"
export NAMESPACE="default"

# Windows
setx AZURE_CLIENT_ID "<YOUR_AZURE_CLIENT_ID>"
setx AZURE_CLIENT_SECRET "<YOUR_AZURE_CLIENT_SECRET>"
setx AZURE_TENANT_ID "<YOUR_AZURE_TENANT_ID>"
setx AZURE_VAULT_URL "<YOUR_AZURE_VAULT_URL>"
setx GOOGLE_API_KEY "<YOUR_GOOGLE_API_KEY>"
setx NAMESPACE "default"
```

## Start-up

### Start consul agent on host machine
- Determine host ip address. E.g., with ipconfig/ifconfig. like 192.168.178.31
- Set host ip address as value for "bind" attribute in command below
```shell
consul agent \
  -server \
  -bootstrap-expect=1 \
  -ui \
  -data-dir=consul/data \
  -config-dir=consul/config \
  -dev \
  -bind=<HOST_IP_ADDRESS>
```

#### or reload when agent already running and configuration is changed
```shell
consul reload
```

### Start consul agent on guest machine - Only if one of the projects is running on a second machine
- Determine host ip address. E.g., with ipconfig/ifconfig. like 192.168.178.83
- Set host ip address as value for "bind" attribute in command below
```shell
consul agent \
  -enable-script-checks=true \
  -data-dir=consul/data \
  -config-dir=consul/config \
  -bind=<GUEST_IP_ADDRESS>
```

### Join consul agent on host machine server - Only if one of the projects is running on a second machine
- Determine host ip address. E.g., with ipconfig/ifconfig. like 192.168.178.31
- Set host ip address as value for "bind" attribute in command below
```shell
consul join <HOST_IP_ADDRESS>
# `consul leave` to leave gracefully
```

### Check consul members for both nodes - Only if one of the projects is running on a second machine
```shell
# should display 2 nodes
consul members 
``` 

### Update current host ip address in rentadrone project `docker-compose.infra.yml` file 
- Determine host ip address. E.g., with ipconfig/ifconfig. like 192.168.178.31
- Switch to [rentadrone](https://github.com/satspeedy/rentadrone) project
- Set host ip address as value for entry "KAFKA_ADVERTISED_LISTENERS" with key "LISTENER_EXT"

### Update `secrets.json` file with current host and guest ip addresses
- Determine ip addresses and update `secrets.json`. E.g., with ipconfig/ifconfig. like  192.168.178.83
>**Note: This currently does not work with `bindings.yaml`, so the ip address must also be adjusted as a workaround in the `bindings.yaml` file.**

### Start sentry agent on host machine
- Start Dapr Sentry
```shell
$HOME/.dapr/sentry --issuer-credentials $HOME/.dapr/certs --trust-domain cluster.local
```

### Start sentry agent on guest machine
- Copy files from host machine `$HOME/.dapr/certs` to guest machine
- Start Dapr Sentry
```shell
$HOME/.dapr/sentry --issuer-credentials $HOME/.dapr/certs --trust-domain cluster.local
```

### Build projects
- in _[rentadrone](https://github.com/satspeedy/rentadrone)_ project folder
- in _[smarttracker](https://github.com/satspeedy/smarttracker)_ project folder
- in _[dronesim](https://github.com/satspeedy/dronesim)_ project folder
```shell
mvn clean package
```

### Start Infra, Dapr and Services in each project folder
- in _[rentadrone](https://github.com/satspeedy/rentadrone)_ project folder
```shell
# switch to rentadrone project folder
cd ../rentadrone

# start infrastructure
docker-compose -f deploy-compose/docker-compose.infra.yml up -d

# register service in consul
consul services register -name=rentadrone-app-id
# to deregister: consul services deregister -id=rentadrone-app-id

# set defined dapr sentry environment variables (even in your IDE)
export DAPR_TRUST_ANCHORS=`cat $HOME/.dapr/certs/ca.crt`
export DAPR_CERT_CHAIN=`cat $HOME/.dapr/certs/issuer.crt`
export DAPR_CERT_KEY=`cat $HOME/.dapr/certs/issuer.key`
export NAMESPACE=default

# start dapr sidecar (updates also consul service; use `--log-level debug` for debug info)
dapr run --components-path ../rentadrone-smarttracker-integration/dapr/components --app-id rentadrone-app-id --app-port 8181 --dapr-http-port 3081 --dapr-grpc-port 52081

cd ../rentadrone

# set defined dapr ports as environment variable (even in your IDE as mentioned above)
## Linux 
export DAPR_HTTP_PORT=3081
export DAPR_GRPC_PORT=52081
## Windows
setx DAPR_HTTP_PORT "3081"
setx DAPR_GRPC_PORT "52081"

# start service
mvn -q spring-boot:run

cd ../rentadrone

# add consul service mesh sidecar envoy
## Linux
consul connect envoy -sidecar-for rentadrone-app-id -admin-bind localhost:19001
## Windows
consul connect envoy -sidecar-for rentadrone-app-id -admin-bind localhost:19001 -bootstrap > ../rentadrone-smarttracker-integration/consul/envoy/rentadrone-bootstrap.json

## Following steps are only necessary on Windows: 
# replace "access_log_path" with "<PATH TO PROJECT DIR>/consul/envoy/rentadrone-proxy.log"

# start envoy with bootstrapped config
envoy -c ../rentadrone-smarttracker-integration/consul/envoy/rentadrone-bootstrap.json
```

- in _[dronesim](https://github.com/satspeedy/dronesim)_ project folder
```shell
# switch to dronesim project folder
cd ../dronesim

# register service in consul
consul services register -name=dronesim-app-id
# to deregister: consul services deregister -id=dronesim-app-id

# set defined dapr sentry environment variables (even in your IDE)
export DAPR_TRUST_ANCHORS=`cat $HOME/.dapr/certs/ca.crt`
export DAPR_CERT_CHAIN=`cat $HOME/.dapr/certs/issuer.crt`
export DAPR_CERT_KEY=`cat $HOME/.dapr/certs/issuer.key`
export NAMESPACE=default

# start dapr sidecar (updates also consul service; use `--log-level debug` for debug info)
dapr run --components-path ../rentadrone-smarttracker-integration/dapr/components --app-id dronesim-app-id --app-port 8282 --dapr-http-port 3082 --dapr-grpc-port 52082

cd ../dronesim

# set defined dapr ports as environment variable (even in your IDE as mentioned above)
## Linux 
export DAPR_HTTP_PORT=3082
export DAPR_GRPC_PORT=52082
## Windows
setx DAPR_HTTP_PORT "3082"
setx DAPR_GRPC_PORT "52082"

# start service
mvn -q spring-boot:run

cd ../dronesim

# add consul service mesh sidecar envoy
## Linux
consul connect envoy -sidecar-for dronesim-app-id -admin-bind localhost:19002
## Windows
consul connect envoy -sidecar-for dronesim-app-id -admin-bind localhost:19002 -bootstrap > ../rentadrone-smarttracker-integration/consul/envoy/dronesim-bootstrap.json

## Following steps are only necessary on Windows:  
# replace "access_log_path" with "<PATH TO PROJECT DIR>/consul/envoy/dronesim-proxy.log"

# start envoy with bootstrapped config
envoy -c ../rentadrone-smarttracker-integration/consul/envoy/dronesim-bootstrap.json
```

- in _[smarttracker](https://github.com/satspeedy/smarttracker)_ project folder
```shell
# switch to smarttracker project folder
cd ../smarttracker

# start infrastructure
docker-compose -f deploy-compose/docker-compose.infra.yml up -d

# Update sentry certs on guest machine
# Copy files from host machine `$HOME/.dapr/certs` to guest machine

# register service in consul
consul services register -name=smarttracker-app-id
# to deregister: consul services deregister -id=smarttracker-app-id

# set defined dapr sentry environment variables (even in your IDE)
export DAPR_TRUST_ANCHORS=`cat $HOME/.dapr/certs/ca.crt`
export DAPR_CERT_CHAIN=`cat $HOME/.dapr/certs/issuer.crt`
export DAPR_CERT_KEY=`cat $HOME/.dapr/certs/issuer.key`
export NAMESPACE=default

# start dapr sidecar (updates also consul service; use `--log-level debug` for debug info)
dapr run --components-path ../rentadrone-smarttracker-integration/dapr/components --app-id smarttracker-app-id --app-port 8383 --dapr-http-port 3083 --dapr-grpc-port 52083

cd ../smarttracker

# set defined dapr ports as environment variable (even in your IDE as mentioned above)
## Linux 
export DAPR_HTTP_PORT=3083
export DAPR_GRPC_PORT=52083
## Windows
setx DAPR_HTTP_PORT "3083"
setx DAPR_GRPC_PORT "52083"

# start service
mvn -q spring-boot:run

cd ../smarttracker 

# add consul service mesh sidecar envoy
## Linux
consul connect envoy -sidecar-for smarttracker-app-id -admin-bind localhost:19003
## Windows
consul connect envoy -sidecar-for smarttracker-app-id -admin-bind localhost:19003 -bootstrap > ../rentadrone-smarttracker-integration/consul/envoy/smarttracker-bootstrap.json

## Following steps are only necessary on Windows:
# replace "access_log_path" with "<PATH TO PROJECT DIR>/consul/envoy/smarttracker-proxy.log"

# start envoy with bootstrapped config
envoy -c ../rentadrone-smarttracker-integration/consul/envoy/smarttracker-bootstrap.json
```

### Test an example request via RentADrone Dapr Sidecar to SmartTracker with postman
- in _[rentadrone](https://github.com/satspeedy/rentadrone)_ project folder 
  - import `rent-a-drone.postman_collection.json` collection in postman and call _Create a new delivery_
- open respond `trackingUrl` in your browser and replace `YOUR_PIN` with your defined pin as query param
```shell
curl -X 'GET' 'http://localhost:3081/v1.0/invoke/smarttracker-app-id/method/api/trackings/...?pin=YOUR_PIN' -H 'accept: application/json' | jq
```
- Refresh the view after 2 minutes to see the currently attached coordinates in the payload

## Further links
- UI to explore, publish and subscribe kafka messages: https://www.getkadeck.com/#/
- UI to explore database: https://www.pgadmin.org/
- UI to explore redis: https://www.npmjs.com/package/redis-commander
- UI to publish and subscribe MQTT messages: https://chrome.google.com/webstore/detail/mqttlens/hemojaaeigabkbcookmlgmdigohjobjm
