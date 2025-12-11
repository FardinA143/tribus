# Uso / compilación / ejecución de la capa de presentación

En este directorio se encuentran las clases necesarias para ejecutar la capa de presentación de la aplicación.

El frontend esta desarollado con ReactJS, y empaquetado con Electron, que a la vez se comunica con el backend desarrollado en Java mediante el driver ElectronDriver.java.

## Requisitos previos
Para compilar y ejecutar la capa de presentación, es necesario tener instalado:
- Node.js 
- npm
  - Electron
  - Vite
- JDK

## Instalación de dependencias
1. Navegar al directorio `FONTS/presentation`.
2. Ejecutar el siguiente comando para instalar las dependencias necesarias:
   ```bash
   npm install
   ```

## Compilación y ejecución
1. Asegurarse de que el backend esté compilado y listo para ejecutarse.
   - Navegar al directorio `FONTS`.
   - Ejecutar el siguiente comando para compilar el backend:
     ```bash
     make jar
     ```
2. Navegar al directorio `FONTS/presentation`.
3. Compilar el frontend ejecutando el siguiente comando:
   ```bash
   npm run build
   ```
4. Ejecutar el siguiente comando para iniciar la aplicación:
   ```bash
   npm run electron
   ```
Esto iniciará la aplicación Electron, que cargará la interfaz de usuario y se comunicará con el backend Java.