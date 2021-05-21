# Servicio agente Java

## Compilacion

### Jar
Correr el comando __gradlew jar__ desde una terminal:
* Compila el codigo fuente
* Genera un Jar
* El jar sera depositado en  build/libs

### Jar de pruebas
Correr el comando __gradlew copyToVm__ desde una terminal.

Este comando facilita la generacion del jar para realizar pruebas sobre una virtual machine local.

Acciones del comando:
* Compila el codigo fuente
* Genera un Jar
* El jar sera copiado al directorio indicado en la variable de configuracion "VM_SHARED_DIR" en el archivo gradle.build

### Entregable (Jar + Depedendencias)
Correr el comando __gradlew buildFull__ desde una terminal:
* Compila el codigo fuente
* Genera un Jar
* Copia las dependencias del proyecto en build/dependencies/lib 

