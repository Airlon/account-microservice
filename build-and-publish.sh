#!/bin/bash

mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true

# Apenas "descomente" o comando abaixo, se quiser enviar para o registry.
# Tenha cuidado para não sobreescrever uma versão utilizada por algum time

# docker push registry.repassa.coop.br:5000/accounts-jvm:0.0.1-SNAPSHOT