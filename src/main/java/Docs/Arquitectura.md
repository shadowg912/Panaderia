# Arquitectura del Sistema

## Arquitectura General

El sistema está desarrollado bajo una arquitectura monolítica utilizando el patrón MVC (Model - View - Controller).

Toda la aplicación se ejecuta como una sola unidad, pero organizada internamente en capas separadas para mantener el código modular, mantenible y escalable.

Tecnologías principales:

* Java
* JavaFX
* JDBC
* SQL Server
* CSS global reutilizable

---

# Patrón MVC

## Model

La capa Model representa la lógica de datos y entidades del sistema.

Responsabilidades:

* Representar entidades del negocio
* Manejar acceso a datos
* Ejecutar consultas SQL
* Validar estructuras básicas de datos

Incluye:

* Models
* Repositories / DAO
* Conexión JDBC
* Queries SQL

Ejemplo:

```txt
model/
Data_base/
```

---

## View

La capa View representa la interfaz gráfica del sistema usando JavaFX.

Responsabilidades:

* Mostrar información al usuario
* Capturar interacciones
* Mantener consistencia visual
* Aplicar estilos CSS globales

Incluye:

* FXML
* Componentes visuales JavaFX
* CSS global

Ejemplo:

```txt
resources/view
```

---

## Controller

La capa Controller conecta la interfaz con la lógica del sistema.

Responsabilidades:

* Manejar eventos de la UI
* Coordinar flujo entre View y Model
* Ejecutar validaciones
* Controlar navegación entre escenas

Incluye:

* Controllers JavaFX
* Lógica de interacción
* Manejo de sesiones
* Navegación

Ejemplo:

```txt
controllers/
```

---

# Arquitectura Monolítica

El sistema utiliza una arquitectura monolítica.

Características:

* Toda la aplicación se despliega como una sola unidad
* Base de datos centralizada
* Comunicación interna directa entre módulos
* Estructura simple y adecuada para aplicaciones de escritorio

Ventajas:

* Desarrollo más rápido
* Menor complejidad inicial
* Fácil integración entre módulos
* Ideal para sistemas académicos y empresariales medianos

---

# Organización del Proyecto

Estructura general esperada:

```txt
src/
├── controllers/
├── models/
├── database/
├── utils/
└── resources/
    ├── view

```

---

# Base de Datos

Motor utilizado:

* SQL Server

Acceso a datos:

* JDBC

Características:

* Uso de PreparedStatement
* Conexiones reutilizables
* Consultas SQL organizadas
* Integridad relacional mediante claves foráneas

---

# Navegación

La navegación del sistema se realiza mediante escenas JavaFX.

Características:

* Cambio dinámico de escenas
* Control centralizado de navegación
* Reutilización de layouts
* Protección de pantallas según sesión activa

---

# Estilos Visuales

El sistema utiliza un CSS global reutilizable.

Reglas:

* Evitar estilos inline innecesarios
* Mantener consistencia visual
* Reutilizar componentes gráficos
* Mantener diseño limpio y moderno

---

# Principios del Proyecto

Reglas generales de desarrollo:

* Mantener separación de responsabilidades
* Evitar duplicación de lógica
* Reutilizar componentes existentes
* Mantener código modular
* No romper arquitectura actual
* Mantener consistencia visual y estructural

---

# Objetivo Arquitectónico

La arquitectura busca:

* Facilidad de mantenimiento
* Escalabilidad moderada
* Organización clara del código
* Facilidad de integración con nuevas funcionalidades
* Desarrollo rápido y estable
