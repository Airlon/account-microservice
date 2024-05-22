## Pré requisitos:

* JDK 11
* Maven 3.8.3 >
* lombok
* Docker
* Dcoker-compose


## Como rodar:

##### Subindo dependências docker-compose:

```bash
docker-compose up
```

##### Empacotando o projeto:

```bash
mvn package 
```
OBS: Necessário executar o **package** antes quando estiver usando injeção de dependência do [mapstruct](https://mapstruct.org/)

##### Subindo o projeto:

```bash
mvn compile quarkus:dev
```


### Informações importantes

* [Configuração lombok eclipse](https://projectlombok.org/setup/eclipse)
* Criação de tests unitario, integração e modo native
    * [tests-quarkus](https://quarkus.io/guides/getting-started-testing)
    * **@NativeImageTest** e **@QuarkusIntegrationTest** não suportam injeção de dependencias,portanto os tests nativos deveram ser tests de integração.
    * Os nomes das classes de tests nativos devem term com **IT**.
    * Executando tests.
      `mvn verify ` ou `mvn test`
    * Executando tests no modo native.
      `mvn verify -Pnative`


#### Dependencias

* Tolerance
    * [quarkus-smallrye-fault-tolerance](https://quarkus.io/guides/smallrye-fault-tolerance)
* Metrics
    * [quarkus-micrometer](https://quarkus.io/guides/micrometer)
* DB-Migration
    * [quarkus-flyway](https://quarkus.io/guides/flyway)
* Config Server
    * [quarkus-spring-cloud-config-client](https://quarkus.io/guides/spring-cloud-config-client)
* Rest Client
    * [quarkus-rest-client](https://quarkus.io/guides/rest-client)
* Messaging
    * [quarkus-smallrye-reactive-messaging](https://quarkus.io/guides/amqp)
* Health Check
    * [quarkus-smallrye-health](https://quarkus.io/guides/smallrye-health)
* Structured Log
    * [quarkus-logging-json](https://quarkus.io/guides/logging)
* Container Image
    * [quarkus-container-image-jib](https://quarkus.io/guides/container-image)
* Percistence
    * [quarkus-hibernate-orm-panache](https://quarkus.io/guides/hibernate-orm-panache)
* Teste
    * [testcontainers](https://www.testcontainers.org/)
* Mapper
    * [mapstruct](https://mapstruct.org/)


## Descrição do projeto

Aqui um apanhado dos objetivos e principais funções do serviço.



## Simulando o ambiente Kubernetes local

Essa aplicação contem os arquivos de deployments no Kubernetes na pasta 05-Deploy/helms/  para você
conseguir rodar local você precisará seguir os seguintes passos:

### Pré requisitos

Primeiramente você precisará configurar o seu ambiente local, assumindo que você
já tem o docker instalado os próximos passos serão:

- [Instalação do Minikube](https://minikube.sigs.k8s.io/docs/start/)
- [Instalar Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/#install-kubectl-binary-with-curl-on-linux)
- [Instalar Helm](https://helm.sh/docs/intro/install/)

### Iniciar o ambiente

O próximo passo é iniciar as dependencias da aplicação, todas elas estão no docker-compose.yaml.
Observação: rodando o comando com "-d" no final você não ficará com o terminal "preso".

```bash
docker-compose up -d
```

### Preparação a aplicação

Entre no arquivo src/main/resources/application.yml você irá perceber que tanto o Postgres quando o
AMQ estão apontando para localhost porém eles tem uma outra versão que está comentada.
Exemplo:

```
# descomente a propriedade para rodar a aplicação no MiniKube
# amqp-host: amq-external-service

amqp-host: localhost
```

Para realizar um deploy local usando o minikube você precisa descomentar a propriedade que apontando para "nome-do-service" e comentar o localhost.
Isso é necessario porque tanto o **amq-external-service** quanto o **postgres-external-service** são serviços
que estão configurados para possibilitar que o minikube acesse o AMP e o Postgres que estarão rodando via docker-compose, aqui tem um material que ajudará a entender melhor esse funcionamento [link](https://kubernetes.io/docs/concepts/services-networking/service/#services-without-selectors).

### Build da imagem

Para construir a imagem basta rodar o script abaixo, por padrão ele não está push da imagem para alterar esse comportamento basta descomentar a linha referente ao "docker push" desse arquivo.

```
 chmod +x build-and-publish.sh
./build-and-publish.sh
```

### Deploy da aplicação

Os arquivos de deploy da aplicação estão no diretório "05-Deploy/helms", o objetivo é fazer tanto o deploy quanto o upgrade de versão utilizando o [Helm](https://helm.sh/).

O comando baixo realiza a instalação da aplicação no Kubernetes:

```aidl
helm install minha-aplicacao ./05-Deploy/helms --set image.tag=0.0.1-SNAPSHOT
```

Caso você precisar mudar algo no deploy você pode rodar um comando para upgrade:
```aidl
helm upgrade minha-aplicacao ./05-Deploy/helms --set image.tag=0.0.2-SNAPSHOT
```

Perceba que no comando acima após a instrução "--set" você consegue sobrescrever valores de variaveis presente no arquivo
05-Deploy/helms/values.yaml.


Outra coisa importante, nesse arquivos values.yaml você precisar adicionar o ip de sua maquina local para que o
Kubernetes consiga se comunicar com os container que estaram rodando via docker-compose.


``` 
### Custom

application:
  port: 8090
  livenessProbeUrl: /${artifactId}/q/health/live
  readinessProbeUrl: /${artifactId}/q/health/ready

externalApplications:

  postgres:
    name: postgres-external-service
    port: 5433
    serviceType: NodePort
    ip: 192.168.0.8 <------------------------------------------ trocar

  amq:
    name: amq-external-service
    port: 5672
    serviceType: NodePort
    ip: 192.168.0.8  <------------------------------------------ trocar
```