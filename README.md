# Puesta en marcha

* Ejecutar WoTGate sin archivo de configuración (puerto HTTP 8080 por defecto) 

$java -jar build/libs/wotgate-*-fat.jar

* Ejecutar WoTGate con archivo de configuración

$java -jar build/libs/wotgate-*-fat.jar -conf src/main/configuration/WoTGate-configuration.json

Las configuraciones que pueden definirse son:
*  Puerto HTTP del WoTGate: http.port
* URL base del WoTGate : http.baseUri

# Consideraciones
Los nombres de usuario y password por defecto son:
* administrator 12345678
* privileged 12345678
* authenticated 12345678


