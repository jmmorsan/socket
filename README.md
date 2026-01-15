# Ejercicio: Sockets TCP - Conexión VM y Gestión de Buffers

Este proyecto implementa una comunicación cliente-servidor mediante Sockets TCP en Java. Se abordan dos objetivos principales: establecer conexión entre máquinas distintas (Host y Máquina Virtual) y corregir la lectura de buffers para evitar "basura" en los mensajes recibidos.

## 🚀 Instrucciones de Ejecución

El escenario simula una arquitectura real:
1.  **Servidor:** Se ejecuta en una Máquina Virtual (Linux).
2.  **Cliente:** Se ejecuta en la máquina anfitriona (Windows/macOS).

### Pasos:
1.  Iniciar el **Servidor** en la VM. Este quedará a la espera de conexiones en el puerto `5555`.
2.  Obtener la IP de la VM (ver sección inferior) y configurarla en el código del Cliente.
3.  Iniciar el **Cliente** en el Host.
4.  Verificar en los logs del servidor que el mensaje se ha recibido limpio y sin caracteres extraños.

---

## 📝 Ejercicio 1: Configuración de Red

### Obtención de la IP de la Máquina Virtual
Para que el cliente (Windows) pudiera encontrar al servidor (VM), fue necesario obtener la IP real de la interfaz de red de la máquina virtual.

* **Sistema:** Linux (Ubuntu/Debian)
* **Comando utilizado:** `hostname -I`
* **Resultado:** Se obtuvo la IP `192.168.165.6` (red local).

### Configuración del Bind
En el servidor, se modificó el `bind` para usar la dirección `0.0.0.0` en lugar de `localhost`. Esto permite que el servidor acepte peticiones de interfaces de red externas y no solo las locales.

---

## 🛠 Ejercicio 2: Lectura Correcta y Buffer "Basura"

Se ha corregido el problema común donde el servidor mostraba caracteres vacíos o extraños al final del mensaje. Esto ocurría porque se transformaba el array de bytes completo a String, sin tener en cuenta cuántos bytes reales se habían enviado.

### Tabla de Problemas y Soluciones

| Problema | Causa | Solución Implementada |
| :--- | :--- | :--- |
| **Basura al final del mensaje** | El buffer (ej. 25 bytes) es mayor que el mensaje recibido ("Hola"). Al convertir a String, se incluyen los bytes vacíos sobrantes. | Usar el valor de retorno de `read()`: `new String(buffer, 0, bytesLeidos)`. |
| **Mensaje cortado** | El buffer es más pequeño que el mensaje enviado por el cliente. | Implementar lectura en bucle (`while`) o aumentar el tamaño del buffer si el protocolo lo permite. |
| **Bloqueo indefinido** | El servidor espera leer más datos pero el cliente no indicó el fin de la transmisión. | Usar `shutdownOutput()` en el cliente o definir un carácter de fin de línea (`\n`). |

---

## 📋 Logs de Ejecución (Ejemplo)

**Servidor (VM):**
```text
[DEBUG] Servidor: escuchando en puerto 5555
[DEBUG] Servidor: conectado desde=/192.168.1.35:54321
[DEBUG] Recibido: 'Hola desde el host'
[DEBUG] Respuesta bytes=18
