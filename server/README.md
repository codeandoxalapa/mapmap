*Esta herramienta digital está publicada en la página web de la iniciativa [Código para el Desarrollo](http://code.iadb.org/es/repositorio/45/mapmap)*
# Servidor MapMap
### Descripción
---
Servidor utilizado para administrar las rutas subidas desde la aplicación móvil <a href="https://github.com/codeandoxalapa/mapmap" target="_blank">MapMap</a>. 

<strong>MapMap</strong> permite crear trazos georeferenciados sin la necesidad de consumir datos del dispositivo móvil, la información que se recolecta a través de la aplicación es la siguiente:
- Conteo de pasajeros (subida y bajada de usuarios en las paradas).
- Puntos que marcan las paradas por donde se detiene el transporte público.
- Línea de trazo que identifica una ruta.
- Tiempo inicial y final del recorrido.
- Fotografía del transporte público que realiza la ruta.

La aplicación puede ser utilizada para realizar cualquier tipo de trazo/recorrido siempre y cuando se utilicen en dispositivos con sistema operativo Android. El equipo de Codeando Xalapa agradece cualquier comentario, observación o sugerencia con respecto a la información contenida en esta aplicación y sus funcionalidades.


### Tecnología
---
* Java 1.6 - [Descargar](http://www.oracle.com/technetwork/es/java/javase/downloads/index.html)
* PostgreSQL 9.4.X - [Descargar](https://www.postgresql.org/download/)
* PostGIS - [Descargar](https://postgis.net/install/)
> Habilitar extensiones e instalar las que aparecen en la sección Enabling PostGIS

* Play Framework en la versión 1.2.5.6  - [Descargar](https://www.playframework.com/download)


### Instalación
---
> El servidor ha sido probado e instalado en servidores con sistema operativo debian

1) Instalar Java con la versión 1.6 como requerida (exportar la variable global o de entorno).
<pre><code>$ export JAVA_HOME=$(readlink -f /opt/jdk/jdk1.7.0/bin/java | sed "s:bin/java::")</code></pre>
2) Instalar Postgres en la versión indicada.
3) Instalar PostGIS en la versión indicada.

Al tener la instalación hasta eeste punto debemos conectarnos a postgres y crear una base de datos para poder colocar las extensiones requeridas.
*** Se deberá contar con usuario y contraseña de postgres.
<pre><code>$ sudo -u postgres psql</code></pre>
Crear una base de datos:
<pre><code>$ CREATE DATABASE gisdb;</code></pre>
Conectarnos a la base de datos:
<pre><code>$ \connect gisdb;</code></pre>
Instalar extensiones:
<pre><code>$ CREATE EXTENSION postgis;</code></pre>
<pre><code>$ CREATE EXTENSION postgis_topology;</code></pre>
<pre><code>$ CREATE EXTENSION postgis_sfcgal;</code></pre>
<pre><code>$ CREATE EXTENSION fuzzystrmatch;</code></pre>
<pre><code>$ CREATE EXTENSION address_standardizer;</code></pre>
<pre><code>$ CREATE EXTENSION address_standardizer_data_us;</code></pre>
<pre><code>$ CREATE EXTENSION postgis_tiger_geocoder;</code></pre>
Salir de la conexión:
<pre><code>$ \q</code></pre>

4) Instalar Play framework en la versión indicada, al finalizar se deberá crear la variable global o de entorno.
<pre><code>$ export PATH=$PATH:/etc/play/</code></pre>
5) Colocar los archivos de esté proyecto en un directorio del servidor.
6) Editar la conexión con el usuario y contraseña de postgres en archivo <strong>conf/application.conf</strong>.
<pre><code>
# If you need a full JDBC configuration use the following :
db.url=jdbc:postgresql://127.0.0.1/transit_wand
db.driver=org.postgresql.Driver
db.user=postgres
db.pass=postgres
</code></pre>
7) Una vez modificado los archivos correr el comando start dentro del proyecto para activar el servicio.
pre><code>$ play start</code></pre>
*** Se podrá acceder al servidor desde la ip y/o url en el puerto 9000

> Es muy importante tener activo un servidor NGINX o APACHE con un dominio.

Una vez importado el todo así es como se visualiza el servidor:
<img src="https://mapaton.org/libs/images/server_mapmap.png" with="100%" title="Route" />


### Metodología
---
<strong>MapMap</strong> fue utilizada en el ejercicio del Mapatón Ciudadano, esté mapeo colaborativo realizado en el ciudad de Xalapa dió como resultado una propuesta metodológica para realizar el trazado de rutas de transporte público en <strong>cualquier ciudad que quiera implementarlo</strong>, se divide en 3 apartados:<br><br>
1. <strong>Previo al Trazado</strong><br>
 <code>1.1.</code> Tener identificado brigadistas (personas que harán el trazado).<br>
 <code>1.2.</code> Tener un primer acercamiento con checadores y choferes, desde los puntos más importantes que concentran rutas.<br>
 <code>1.3.</code> Identificar los puntos de inicio y fin.<br>
 <code>1.4.</code> Mapear las rutas en papel (fieldpapers.org), en la medida de lo posible.<br>
 <code>1.5.</code> Verificar si en los periodos de tiempo en lo que se quiere realizar el trazado se van a presentar eventos extraordinarios.<br>
 <code>1.6.</code> Identificar las zonas geográficas.<br>
 <code>1.7.</code> Organizar brigadas de voluntarios por zonas y rutas.<br>
 <code>1.8.</code> Asignar líderes a la brigadas de voluntarios.<br>
 <code>1.9.</code> Probar MapMap en una ruta seleccionada antes de iniciar el proceso.<br>
 <code>1.10.</code> Capacitar a las brigadas de voluntarios (ver manual de usuario).<br>
 <code>1.11.</code> dentificar horarios y días en los que hay disponibilidad el servicio de transporte público.<br><br>
2. <strong>Durante el Trazado</strong><br>
 <code>2.1.</code> Organizar las brigadas de voluntarios por parejas.<br>
 <code>2.2.</code> Cubrir todas las rutas.<br>
 <code>2.3.</code> Realizar y repetir el trazado de ruta.<br>
 <code>2.4.</code> Los líderes de brigadas deben estar en contacto con sus brigadas de voluntarios.<br>
 <code>2.5.</code> Reportar el progreso del trazado por parte de los brigadistas voluntarios a los líderes de brigada.<br><br>
3. <strong>Posterior al Trazado</strong><br>
 <code>3.1.</code> Recopilar la información resultado del trazado en el repositorio.<br>
 <code>3.2.</code> Las brigadas de voluntarios deben informar a sus líderes que concluyeron el proceso de trazado.<br>
 <code>3.3.</code> Los líderes deben llevar un registro de la zonas y rutas a su cargo que han sido trazadas por completo.<br>
 <code>3.4.</code> La información deben ser verificada y contrastada con la cartografía de la zona geográfica que fue trazada.<br><br>
> Existe una versión extendida de la metodología la cual se puede ver en: <a href="https://mapaton.org">mapaton.org</a>.


### Manejo de datos
---
Una vez que utilizaste MapMap para realizar el mapeo de una ruta de transporte público la información de la(s) ruta(s) se encuentra(n) dentro de la aplicación MapMap y por tal motivo no es posible utilizar esos datos en un formato estandarizado e interpretable por sistemas de geo referencia como QGIS, Open StreetMap , Google Maps entre otros. Para poder disponer y manejar los datos es necesario contar con la infraestructura de almacenamiento así como de conversión de datos y realizar un proceso de limpieza a los datos almacenados.
<br><br>
Escoger servidor de almacenamiento:<br>
   * Servidor Público dentro de la web del Mapatón Ciudadano (http://mapaton.org/mapmap):  Esté servidor permite almacenar y concentrar las rutas mapeadas para visualizarlas sobre un tile de Open Street Map (OSM) y conversión en formato Shapefile (.shp) y comma-separated values (.csv).
   * Instalar tu propio servidor: Utilizando el código abierto de TransitWand puedes controlar y modificar el proceso de almacenamiento, visualización y conversión de datos sin depender de un servicio que a pesar de ser público y gratuito podría dejar de funcionar en cualquier momento. Ver como instalar tu propio servidor desde esté <a href="https://github.com/conveyal/transit-wand">enlace</a>.


### Limpieza de datos
---
Una vez que los datos se encuentran concentrados en la infraestructura de almacenamiento es posible descargarlos en dos formatos (.shp, .csv) para su uso.
#### Descargar datos
Lo único que se requiere es ingresar el código de seis dígitos que la aplicación proporciona para identificar el dispositivo que fue utilizado para realizar el mapeo. Teniendo esté código se deberá entrar al servidor de la infraestructura (https://mapaton.org/mapmap) e ingresarlo:<br><br>
<img src="https://mapaton.org/libs/images/seis.png" with="300px" align="middle" title="Route" />
<br><br>
Una vez que ingresaste tu código es posible descargar los datos, seleccionando la ruta que deseas descargar en el formato que tú indiques.<br><br>
<img src="https://mapaton.org/libs/images/mapmap_server.png" with="100%" align="middle" title="Route" />
#### Convertir ShapeFiles a GEOJSON
Al contar con la información descargada en formato Shapefile (.shp) es posible realizar un proceso de conversión a otros formatos como por ejemplo Geojson (http://geojson.org/) que nos permite utilizar y manipular los datos con lenguajes de programación como Javascript y realizar interfaces de consulta para visualizar las rutas. <br><br>
Para realizar ese proceso adicional de conversión que se encuentra fuera de Transitwand se puede hacer uso de otras herramientas Open Source, como ejemplo tenemos el siguiente repositorio en donde se encuentran las rutas en formato Geojson (https://github.com/XalapaJS/xalapa_bus_data).
Ejemplo de un geojson obtenido de <strong>MapMap</strong>:<br><br>
<img src="https://mapaton.org/libs/images/example_geojson.png" with="100%" align="middle" title="Route" />
#### Publicación en datos abiertos
Una vez que se dispone de los datos en formato estandarizado y abierto se puede realizar una publicación de la información recolectada. En el caso de Mapatón Ciudadano las rutas de transporte público se publicaron en dos plataformas mexicanas para la publicación de datos abiertos, estas son:
  * http://datamx.io/dataset/rutas-de-transporte-publico-en-xalapa
  * https://datos.gob.mx/busca/dataset/rutas-tranporte-publico

Es importante mencionar:<br>
```
En ambas plataformas los datos pueden ser descargados y utilizados de manera libre y gratuita.
```
```
El Ayuntamiento de la Ciudad de Xalapa realizó la publicación en https://datos.gob.mx.
```
De esa forma la información puede ser utilizada y compartida para futuros proyectos, investigaciones académicas, análisis y planeación de movilidad urbana, entre otros.
> Existe una versión extendida del proceso de limpieza de datos la cual se puede ver en: <a href="https://mapaton.org">mapaton.org</a>. Actualmente no está publicada porque se encuentra en un proceso de rediseño, se espera tenerla pública próximamente.


### Autores:
---
[![](https://mapaton.org/libs/images/small-logo-codeandoxalapa-readme.png)](https://github.com/codeandoxalapa)
* Rolando Drouaillet Pumarino
* Juan Manuel Becerril del Toro
* Elizabeth Montenegro Ñeco
* Elías Martín Sánchez Jímenez


### Información adicional
---
Un ejemplo de los trazos y archivos que pueden ser realizados con <strong>MapMap</strong> son:<br><br>
* Visor de rutas - https://mapaton.org/rutas/
* Servidor para administrar rutas y generar archivos Shapefiles|CSV - http://mapaton.org/mapmap
* Geojson - https://github.com/codeandoxalapa/mapmap/tree/master/data
* Datos en <a href="https://www.mapillary.com/app/user/xalapa?lat=19.533702607228946&lng=-96.84397948833018&z=11.389325293212847&username%5B%5D=xalapa">Mapillary</a>
<br><br>
o bien, un resultado visual (ejemplo de una ruta): 
<img src="https://mapaton.org/libs/images/ejemplo_route.png" with="100%" title="Route" />


### Licencia
---
Licencia MIT [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


### Agradecimiento:
---
Agradecemos a todos los miembros que pertenecen a la comunidad de <a href="http://codeandoxalapa.org/">Codeando Xalapa</a> por contribuir en este proyecto. También agradecemos al proyecto [TransitWand](https://github.com/conveyal/transit-wand) por haber creado la aplicación móvil que sirvió como base para construir <strong>MapMap</strong>.

#### <i> Happy Coding!</i>
<img src="https://mapaton.org/libs/images/back.png" with="100%" title="Route" />
