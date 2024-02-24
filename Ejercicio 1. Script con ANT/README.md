# Ejercicio 1. Script con ANT

Este es un proyecto de ejemplo que utiliza ANT para automatizar la compilación, ejecución, generación de documentación y creación de un fichero JAR, de un programa Java.

## Requisitos previos

Asegúrate de tener instalado lo siguiente en tu sistema:

- Java Development Kit (JDK)
- Apache ANT

## Configuración

1. Clona este repositorio en tu máquina local.
2. Abre una terminal y navega hasta el directorio raíz del proyecto.

## Uso

Ejecuta el siguiente comando para compilar el programa:

    PS D:\Estudios\Eclipse\MiProyectoAnt> ant
   
    Buildfile: D:\Estudios\Eclipse\MiProyectoAnt\build.xml

    init:
        [mkdir] Created dir: D:\Estudios\Eclipse\MiProyectoAnt\bin
        [mkdir] Created dir: D:\Estudios\Eclipse\MiProyectoAnt\doc
        [mkdir] Created dir: D:\Estudios\Eclipse\MiProyectoAnt\dist

    compile:
        [javac] Compiling 1 source file to D:\Estudios\Eclipse\MiProyectoAnt\bin

    javadoc:
        [javadoc] Generating Javadoc
        [javadoc] Javadoc execution
        [javadoc] Loading source file D:\Estudios\Eclipse\MiProyectoAnt\src\MiProyectoAnt.java...
        [javadoc] Constructing Javadoc information...
        [javadoc] Standard Doclet version 1.8.0_401
        [javadoc] Building tree for all the packages and classes...
        [javadoc] Building index for all the packages and classes...
        [javadoc] Building index for all classes...
        [javadoc] Generating D:\Estudios\Eclipse\MiProyectoAnt\doc\help-doc.html...
        [javadoc] 1 warning
        [javadoc] D:\Estudios\Eclipse\MiProyectoAnt\src\MiProyectoAnt.java:7: warning: no description for @param
        [javadoc]      * @param args
        [javadoc]        ^

    jar:
        [jar] Building jar: D:\Estudios\Eclipse\MiProyectoAnt\dist\MiProyectoAnt.jar

    all:
        BUILD SUCCESSFUL
        Total time: 1 second

Ejecuta el siguiente comando para ejecutar el programa JAR

    PS D:\Estudios\Eclipse\MiProyectoAnt> java -jar .\dist\MiProyectoAnt.jar
   
    Hola mundo! Este es mi proyecto para la práctica ANT

## Información extra sobre ANT.

El Script automatizado de ANT se encuentra en "build.xml" que es el nombre por defecto (Por ese motivo funciona correctamente escribir el comando ant y no se require expecificar ningún documento). 
Cualquier cambio de ANT deberá verse reflejado en ese documento.
