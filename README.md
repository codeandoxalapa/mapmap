# MapMap
### Descripción
---
Aplicacíon móvil que te permite mapear rutas que recorren los vehiculos del Transporte Público de una ciudad, con el objetivo de generar datos en formato <a href="https://developers.google.com/transit/gtfs/" target="_blank">GTFS</a>. 

<strong>MapMap</strong> permite crear trazos georeferenciados sin la necesidad de consumir datos del dispositivo móvil, la información que se recolecta a través de la aplicación es la siguiente:
- Conteo de pasajeros (subida y bajada de usuarios en las paradas).
- Puntos que marcan las paradas por donde se detiene el transporte público.
- Línea de trazo que identifica una ruta.
- Tiempo inicial y final del recorrido.
- Fotografía del transporte público que realiza la ruta.

La aplicación puede ser utilizada para realizar cualquier tipo de trazo/recorrido siempre y cuando se utilicen en dispositivos con sistema operativo Android. El equipo de Codeando Xalapa agradece cualquier comentario, observación o sugerencia con respecto a la información contenida en esta aplicación y sus funcionalidades.


### Problemática
---
En un gran porcentaje de las ciudades latinoamericanas el transporte público se brinda a través de concesiones por medio del sector privado, esto implica un desconocimiento del número de concesiones que transitan por las ciudades, los itinerarios son irregulares así como las rutas por donde circulan los camiones. El no conocer las rutas genera un serio problema para la movilidad de las personas y la administración pública al no lograr gestionar adecuadamente el transporte público de una ciudad.


### Contexto
---
En la ciudad de Xalapa llevamos a cabo el ejercicio del <a href="https://mapaton.org" target="_blank">Mapatón Ciudadano</a> con la colaboración de la sociedad civil (Codeando Xalapa), 400 personas participantes, gobierno municipal y asociaciones como Open Street Map, WRI, entre otras. 

Éste ejercicio consistió en el uso de tecnologías de posicionamiento geográfico (GPS) a través del uso de <strong>MapMap</strong> para la recolección de datos que permitieron crear feeds de las rutas de transporte público liberando esta información en formato abierto (GTFS, JSON, GIS, Shapefiles y GeoJSON) permitiendo ser utilizados por sistemas de navegación y buscando que el uso de los datos liberados permitan mejorar la movilidad de una ciudad. 
> MapMap está diseñada para el mapeo de rutas de transporte público de la ciudad de Xalapa, pero puede ser ajustada, implementada y reutilizada para el trazado de rutas de transporte o cualquier otro tipo de trazado y en cualquier ciudad.


### Tecnología
---
* Java 1.6 en adelante - [Descargar](http://www.oracle.com/technetwork/es/java/javase/downloads/index.html)
* Protocol Buffer Java 2.4.1 - [Descargar](https://github.com/google/protobuf/releases/tag/v2.4.1) 
* Android SDK, versión mínima 2.2 (Api Level 8) - [Descargar](https://developer.android.com/studio/index.html)
* IDE Eclipse for Android Developers - [Descargar](http://www.eclipse.org/downloads/packages/eclipse-android-developers-includes-incubating-components/neon3) 


### Ambiente de desarrollo
---
Si lo que deseas es utilizar el proyecto como desarrollador lo primero que debes hacer el preparar tu ambiente de desarrollo para poder realiar pruebas, detectar posibles errores e incluso proponer mejoras.
```
1. Instalar Java 1.6 en adelante y agregar variables de entorno
  * https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/
2. Instalar Android SDK
  * http://www.androidcentral.com/installing-android-sdk-windows-mac-and-linux-tutorial
3. Instar IDE Eclipse (Android Developer)
  * http://www.eclipse.org/downloads/packages/eclipse-android-developers-includes-incubating-components/neon3
4. Agregar plugin de Android a IDE Eclipse
  * https://stuff.mit.edu/afs/sipb/project/android/docs/sdk/installing/installing-adt.html
  * http://www.theserverside.com/tutorial/Step-by-step-guide-to-Android-development-with-Eclipse
5. Importar proyecto Android a IDE Eclipse
  * http://programmerguru.com/android-tutorial/how-to-import-android-project-in-eclipse/
6. Crear AVD (Android Virtual Device)
  * https://www.embarcadero.com/starthere/xe5/mobdevsetup/android/en/creating_an_android_emulator.html
  * http://theopentutorials.com/tutorials/android/how-to-create-android-avd-emulator-in-eclipse/
```
> **En caso de tener un dispositivo móvil físico no es necesario realizar el paso número 6**


### Instalación
---
Teniendo los archivos descargados y listo el ambiente de desarrollo:<br>
1) En Eclipse, pulse <strong>Archivo</strong> > <strong>Importar</strong>.
2) En la página Seleccionar del recuadro de diálogo Importar, seleccione <strong>Proyectos al Espacio de trabajo</strong> en el recuadro de lista <strong>Seleccionar un origen de importación</strong> y pulse <strong>Siguiente</strong>.
3) En la página Importar proyectos del recuadro de diálogo Importar, verifique si la opción <strong>Seleccionar directorio raíz</strong> está seleccionada y, a continuación , pulse el botón <strong>Examinar</strong> de esta opción.
4) En el recuadro de diálogo <strong>Examinar</strong> para buscar carpeta, vaya a la ubicación donde guardo los archivos del proyecto descargados y selecciónelo.
5) Pulse <strong>Finalizar</strong>.

**Una vez importado el proyecto podrá correr la aplicación desde un dispositivo virtual ó bien desde un dispositivo móvil con sistema Android.**

> <strong>MapMap</strong> se encuentra disponible para su descarga en la tienda oficial de aplicaciones para sistema operativo Android siguiendo este <a href="https://play.google.com/store/apps/details?id=org.codeandoxalapa.mapmap&hl=es" target="_blank">enlace</a>.

### ¿Cómo se utiliza?
---
El equipo de Codeando Xalapaa creó un manual de usuario que explica paso a paso cómo utilizar MapMap para realizar el mapeo de una ruta de Transporte Público, puedes descargar el manual en formato PDF
[https://mapaton.org/docs/Manual_de_Usuario_MapMap.pdf](https://mapaton.org/docs/Manual_de_Usuario_MapMap.pdf) 


### Metodología
---
<strong>MapMap</strong> fue utilizada en el ejercicio del Mapatón Ciudadano, esté mapeo colaborativo realizado en el ciudad de Xalapa dió como resultado una propuesta metodológica para realizar el trazado de rutas de transporte público en cualquier ciudad, se divide en 3 apartados:<br><br>
1. <strong>Previo al Trazado</strong><br>
 1.1. Tener identificado brigadistas (personas que harán el trazado).<br>
 1.2. Tener un primer acercamiento con checadores y choferes, desde los puntos más importantes que concentran rutas.<br>
 1.3. Identificar los puntos de inicio y fin.<br>
 1.4. Mapear las rutas en papel (fieldpapers.org), en la medida de lo posible.<br>
 1.5. Verificar si en los periodos de tiempo en lo que se quiere realizar el trazado se van a presentar eventos extraordinarios.<br>
 1.6. Identificar las zonas geográficas.<br>
 1.7. Organizar brigadas de voluntarios por zonas y rutas.<br>
 1.8. Asignar líderes a la brigadas de voluntarios.<br>
 1.9. Probar MapMap en una ruta seleccionada antes de iniciar el proceso.<br>
 1.10. Capacitar a las brigadas de voluntarios (ver manual de usuario).<br>
 1.11. dentificar horarios y días en los que hay disponibilidad el servicio de transporte público.<br><br>
2. <strong>Durante el Trazado</strong><br>
 2.1. Organizar las brigadas de voluntarios por parejas.<br>
 2.2. Cubrir todas las rutas.<br>
 2.3. Realizar y repetir el trazado de ruta.<br>
 2.4 Los líderes de brigadas deben estar en contacto con sus brigadas de voluntarios.<br>
 2.5 .Reportar el progreso del trazado por parte de los brigadistas voluntarios a los líderes de brigada.<br><br>
3. <strong>Posterior al Trazado</strong><br>
 3.1. Recopilar la información resultado del trazado en el repositorio.<br>
 3.2. Las brigadas de voluntarios deben informar a sus líderes que concluyeron el proceso de trazado.<br>
 3.3. Los líderes deben llevar un registro de la zonas y rutas a su cargo que han sido trazadas por completo.<br>
 3.4. La información deben ser verificada y contrastada con la cartografía de la zona geográfica que fue trazada.<br><br>
> Existe una versión extendida de la metodología la cual se puede ver en: <a href="https://mapaton.org">mapaton.org</a>. Actualmente no está publicada porque se encuentra en un proceso de rediseño, se espera tenerla pública próximamente.


### Manejo de datos
---
1) Escoger servidor de almacenamiento:
   * TransitWand
   * Instalar servidor propio
2) Proceso de limpieza de datos.
   * Descargar datos
   * Usar (https://github.com/XalapaCode/xalapa_bus_data) para convertir ShapeFiles a GEOJSON 
   * Publicación en datos abiertos

### Análisis de calidad
---
De acuerdo al sistema de evaluación de software definido en la guía de ciclo de vida de desarrollo de software, el análisis de esta herramienta ha generado la siguiente evaluación:

Blocker issues: <br>
Duplicated lines: <br>
Critical issues: <br>
Technical debt: <br>
Test coverage: <br>

Más información en este <a href="https://el-bid.github.io/software-life-cycle-guide/delivery/evaluation-matrix/">enlace</a>.


### Autores:
---
[![](https://mapaton.org/images/small-logo-codeandoxalapa-readme.png)](https://github.com/codeandoxalapa) Codeando Xalapa
* Rolando Drouaillet Pumarino
* Juan Manuel Becerril del Toro
* Elizabeth Montenegro Ñeco
* Elías Martín Sánchez Jímenez


### Información adicional
---
Un ejemplo de los trazos y archivos que pueden ser realizados con <strong>MapMap</strong> son:<br><br>
Shapefiles - https://www.mapaton.org/resultados <br>
Geojson - https://github.com/codeandoxalapa/mapmap/tree/master/data <br>
<br><br>
o bien, un resultado visual: 
<img src="https://mapaton.org/images/ejemplo_route.png" with="100%" title="Route" />


### Licencia
---
Licencia MIT <br>
Copyright (c) 2017 Codeando Xalapa


### Agradecimiento:
---
Agradecemos a todos los miembros que pertenecen a la comunidad de Codeando Xalapa por contribuir en este proyecto. También agradecemos al proyecto [TransitWand](https://github.com/conveyal/transit-wand) por haber creado la aplicación móvil que sirvió como base para construir <strong>MapMap</strong>.

#### <i>Happy Coding!</i>
