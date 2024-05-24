# WebSockets
Proyecto de WebSockets en Spring
Estudiantes:
- Daniel Fernández Barrientos
- Ismael Manzanera López

Datos relevantes:
- En la clase "UserService" la variable "file" contiene la ruta del csv con los datos de: usuario,contraseña,nivel
- Si se intenta iniciar sesión con un usuario que no esté definido o una contraseña incorrecta, no se permite el acceso al chat.
- Se ha cambiado el puerto por defecto a 8090
- En la página de lógin se tiene que indicar si se quieren recibir mensajes de usuarios de niveles "inferiores" o no.
- Los mensajes en el chat siguen el siguiente esquema: "nivel - usuario fecha del mensaje"
- Los usuarios definidos son los siguientes:
	- usuario,contraseña,nivel
	- daniel,daniel123,3
	- ismael,ismael123,4
	- profesor,profesor123,3

Funcionamiento:
- Clonar el repositorio.
- Agregar el proyecto de Maven a Eclipse o inteliJ.
- Iniciar la aplicación de Spring.
- Acceder a http://localhost:8090
- Iniciar sesión.
- Empezar a chatear.
