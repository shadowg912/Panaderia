# Base de Datos - PANADERÍA

## Tecnologías
- SQL Server
- JDBC
- JavaFX
- Arquitectura basada en escenas

---

# Tablas Principales

## CATEGORIA_INGREDIENTE

Descripción:
Categorías para clasificar ingredientes.

Columnas:
- id_categoria_ingrediente (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- descripcion (text, NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## CATEGORIA_PRODUCTO

Descripción:
Categorías de productos vendidos por la panadería.

Columnas:
- id_categoria_producto (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- descripcion (text, NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## CATEGORIA_PROVEEDOR

Descripción:
Categorías para clasificar proveedores.

Columnas:
- id_categoria_proveedor (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- descripcion (text, NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## CIUDAD

Descripción:
Ciudades para el sistema de direcciones.

Columnas:
- id_ciudad (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- id_provincia (FK -&gt; PROVINCIA.id_provincia, int, NOT NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## CLIENTE

Descripción:
Clientes de la panadería (empresas).

Columnas:
- id_cliente (PK, int)
- razon_social (varchar, NOT NULL, len &gt; 0)
- rnc (char(11), NOT NULL)
- id_direccion (FK -&gt; DIRECCION.id_direccion, int, NOT NULL)
- telefono (varchar(30), NULL)
- correo_electronico (varchar(150), NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## COMPRA_MATERIAL

Descripción:
Compras realizadas a proveedores.

Columnas:
- id_compra_material (PK, int)
- fecha (date, NOT NULL)
- estado (varchar, NOT NULL) — PENDIENTE | PAGADA | CANCELADA
- id_forma_pago (FK -&gt; FORMA_PAGO.id_forma_pago, int, NOT NULL)
- id_proveedor (FK -&gt; PROVEEDOR.id_proveedor, int, NOT NULL)

---

## CONSUMO_PRODUCCION

Descripción:
Consumo de productos durante la producción.

Columnas:
- id_consumo (PK, int)
- id_produccion (FK -&gt; PRODUCCION.id_produccion, int, NOT NULL)
- id_producto (FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- cantidad_consumida (decimal, NOT NULL, &gt; 0)

---

## DETALLE_COMPRA

Descripción:
Ingredientes/productos comprados en una compra.

Columnas:
- id_compra_material (FK -&gt; COMPRA_MATERIAL.id_compra_material, PK, int, NOT NULL)
- id_ingrediente (FK -&gt; INGREDIENTE.id_ingrediente, PK, int, NOT NULL)
- cantidad (decimal, NOT NULL, &gt; 0)
- costo_unitario (decimal, NOT NULL, &gt;= 0)
- monto_total (decimal, NULL)
- id_producto (FK -&gt; PRODUCTO.id_producto, int, NOT NULL)

---

## DETALLE_ORDEN_VENTA

Descripción:
Productos incluidos en una orden de venta.

Columnas:
- id_orden_venta (FK -&gt; ORDEN_VENTA.id_orden_venta, PK, int, NOT NULL)
- id_producto (FK -&gt; PRODUCTO.id_producto, PK, int, NOT NULL)
- cantidad (decimal, NOT NULL)
- precio_unitario (money, NOT NULL)
- subtotal (money, NOT NULL)

---

## DETALLE_PRODUCCION

Descripción:
Productos resultantes de una producción.

Columnas:
- id_detalle_produccion (PK, int)
- id_produccion (FK -&gt; PRODUCCION.id_produccion, int, NOT NULL)
- id_producto (FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- cantidad_producida (decimal, NOT NULL, &gt; 0)

---

## DIRECCION

Descripción:
Direcciones físicas del sistema.

Columnas:
- id_direccion (PK, int)
- calle (varchar, NOT NULL, len &gt; 0)
- numero (int, NULL, &gt; 0 si no es NULL)
- referencia (text, NULL)
- id_sector (FK -&gt; SECTOR.id_sector, int, NOT NULL)

---

## EMPLEADO

Descripción:
Información de empleados.

Columnas:
- id_empleado (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- apellido1 (varchar, NOT NULL, len &gt; 0)
- apellido2 (varchar, NULL)
- numero_telefono (varchar(30), NULL)
- id_puesto (FK -&gt; PUESTO.id_puesto, int, NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## ESTADO_ENVIO

Descripción:
Catálogo de estados para el seguimiento de envíos.

Columnas:
- id_estado_envio (PK, int)
- nombre (varchar(50), NOT NULL, UNIQUE)
- descripcion (varchar(255), NULL)

Valores:
- PENDIENTE
- ASIGNADO
- EN_RUTA
- ENTREGADO
- DEVUELTO
- CANCELADO

---

## ENVIO

Descripción:
Seguimiento de entregas de órdenes de venta.

Columnas:
- id_envio (PK, int)
- id_orden_venta (FK -&gt; ORDEN_VENTA.id_orden_venta, int, NOT NULL, UNIQUE)
- id_empleado_transportista (FK -&gt; EMPLEADO.id_empleado, int, NULL)
- id_estado_envio (FK -&gt; ESTADO_ENVIO.id_estado_envio, int, NOT NULL)
- id_direccion_entrega (FK -&gt; DIRECCION.id_direccion, int, NULL)
- id_usuario_creacion (FK -&gt; USUARIO.id_usuario, int, NOT NULL)
- numero_seguimiento (varchar(50), NULL, UNIQUE)
- fecha_asignacion (datetime, NULL)
- fecha_salida (datetime, NULL)
- fecha_entrega_estimada (datetime, NULL)
- fecha_entrega_real (datetime, NULL)
- observaciones (text, NULL)

Reglas:
- fecha_asignacion &lt;= fecha_salida (si ambas no son NULL)
- fecha_salida &lt;= fecha_entrega_real (si ambas no son NULL)
- fecha_entrega_estimada &gt;= fecha_asignacion (si ambas no son NULL)
- Estados ASIGNADO/EN_RUTA/ENTREGADO requieren transportista y dirección (trigger)
- Transiciones de estado validadas por máquina de estados (trigger)
- Cambio de transportista se registra en HISTORICO_TRANSPORTISTA_ENVIO (trigger)

---

## FACTURA_VENTA

Descripción:
Facturas generadas para órdenes de venta.

Columnas:
- id_factura_venta (PK, int)
- id_empresa_cliente (FK -&gt; CLIENTE.id_cliente, int, NOT NULL)
- numero_factura (varchar, NOT NULL)
- fecha_emision (date, NOT NULL)
- estado (varchar, NOT NULL) — EMITIDA | PAGADA | ANULADA
- subtotal (money, NULL, &gt;= 0)
- itbis (money, NULL, &gt;= 0)
- monto_total (money, NULL, &gt;= 0)
- id_forma_pago (FK -&gt; FORMA_PAGO.id_forma_pago, int, NOT NULL)
- id_orden_venta (FK -&gt; ORDEN_VENTA.id_orden_venta, int, NOT NULL)

---

## FORMA_PAGO

Descripción:
Formas de pago disponibles.

Columnas:
- id_forma_pago (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- estado (bit, NOT NULL, DEFAULT 1)

---

## HISTORICO_ENVIO

Descripción:
Historial de cambios de estado en envíos.

Columnas:
- id_historico (PK, int)
- id_envio (FK -&gt; ENVIO.id_envio, int, NOT NULL)
- fecha_evento (datetime, NOT NULL)
- fecha_registro (datetime, NOT NULL, DEFAULT GETDATE())
- id_estado_envio (FK -&gt; ESTADO_ENVIO.id_estado_envio, int, NOT NULL)
- observaciones (text, NULL)
- id_usuario (FK -&gt; USUARIO.id_usuario, int, NULL)

Nota:
- fecha_evento = cuándo ocurrió el evento en la realidad
- fecha_registro = cuándo se registró en el sistema

---

## HISTORICO_RECLAMACIONES

Descripción:
Historial de cambios de estado de reclamaciones.

Columnas:
- id_historico_reclamacion (PK, int)
- id_reclamacion (FK -&gt; RECLAMACION_VENTA.id_reclamacion_venta, int, NOT NULL)
- fecha_evento (datetime, NOT NULL)
- estado (varchar, NOT NULL) — ABIERTA | EN_PROCESO | RESUELTA | CERRADA
- observaciones (text, NULL)
- id_empleado (FK -&gt; EMPLEADO.id_empleado, int, NULL)

---

## HISTORICO_TRANSPORTISTA_ENVIO

Descripción:
Trazabilidad de reasignaciones de transportista en envíos.

Columnas:
- id_historico_transportista (PK, int)
- id_envio (FK -&gt; ENVIO.id_envio, int, NOT NULL)
- id_empleado_transportista_anterior (FK -&gt; EMPLEADO.id_empleado, int, NULL)
- id_empleado_transportista_nuevo (FK -&gt; EMPLEADO.id_empleado, int, NOT NULL)
- fecha_cambio (datetime, NOT NULL, DEFAULT GETDATE())
- id_usuario (FK -&gt; USUARIO.id_usuario, int, NULL)
- motivo (text, NULL)

Notas:
- id_empleado_transportista_anterior = NULL indica primera asignación
- Se genera automáticamente por trigger al cambiar el transportista en ENVIO

---

## INGREDIENTE

Descripción:
Ingredientes usados en recetas.

Columnas:
- id_ingrediente (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- unidad_medida (varchar, NOT NULL)
- id_categoria_ingrediente (FK -&gt; CATEGORIA_INGREDIENTE.id_categoria_ingrediente, int, NOT NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## INVENTARIO

Descripción:
Control de stock actual de productos.

Columnas:
- id_producto (PK/FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- stock_actual (decimal, NOT NULL, &gt;= 0)
- fecha_actualizacion (datetime, NULL)

---

## MOVIMIENTO_INVENTARIO

Descripción:
Historial de entradas y salidas de inventario.

Columnas:
- id_movimiento (PK, int)
- id_producto (FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- cantidad (decimal, NOT NULL, &gt; 0)
- id_usuario (FK -&gt; USUARIO.id_usuario, int, NOT NULL)
- fecha (datetime, NULL)
- id_tipo_movimiento (FK -&gt; TIPO_MOVIMIENTO.id_tipo, int, NULL)

---

## ORDEN_PRODUCCION

Descripción:
Órdenes de producción planificadas.

Columnas:
- id_orden_produccion (int, NOT NULL)
- id_producto (int, NOT NULL)
- id_empleado (int, NOT NULL)
- cantidad_planificada (decimal, NOT NULL)
- fecha_produccion (date, NOT NULL)
- estado (varchar(50), NOT NULL)
- fecha_registro (datetime, NULL)

---

## ORDEN_VENTA

Descripción:
Órdenes de venta realizadas por clientes.

Columnas:
- id_orden_venta (PK, int)
- id_cliente (FK -&gt; CLIENTE.id_cliente, int, NOT NULL)
- estado (varchar, NOT NULL) — PENDIENTE | FACTURADA | CANCELADA
- fecha_orden (date, NOT NULL)
- id_forma_pago (FK -&gt; FORMA_PAGO.id_forma_pago, int, NULL)
- subtotal (money, NULL)
- itbis (money, NULL)
- monto_total (money, NULL)
- fecha_entrega (date, NULL)
- id_empleado (FK -> EMPLEADO.id_empleado, int, NULL)
- motivo_cancelacion (text, NULL)

---

## PAGO

Descripción:
Pagos realizados para órdenes de venta.

Columnas:
- id_pago (PK, int)
- id_orden_venta (FK -&gt; ORDEN_VENTA.id_orden_venta, int, NOT NULL)
- id_forma_pago (FK -&gt; FORMA_PAGO.id_forma_pago, int, NOT NULL)
- monto (decimal, NOT NULL)
- fecha (datetime, NULL)

---

## PAIS

Descripción:
Países para el sistema de direcciones.

Columnas:
- id_pais (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- estado (bit, NOT NULL, DEFAULT 1)

---

## PRODUCCION

Descripción:
Registro de procesos de producción.

Columnas:
- id_produccion (PK, int)
- fecha_inicio (datetime, NOT NULL)
- fecha_fin (datetime, NULL)
- estado (varchar(20), NOT NULL) — PENDIENTE | EN_PROCESO | FINALIZADA | CANCELADA
- id_usuario (FK -&gt; USUARIO.id_usuario, int, NOT NULL)
- id_orden_venta (FK -&gt; ORDEN_VENTA.id_orden_venta, int, NULL)
- observacion (varchar(500), NULL)

Reglas:
- fecha_fin solo puede tener valor si estado es FINALIZADA o CANCELADA

---

## PRODUCTO

Descripción:
Productos del sistema (terminados, materia prima, material de empaque).

Columnas:
- id_producto (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- id_categoria_producto (FK -&gt; CATEGORIA_PRODUCTO.id_categoria_producto, int, NOT NULL)
- precio_unitario (money, NOT NULL, &gt;= 0)
- id_unidad (FK -&gt; UNIDAD.id_unidad, int, NOT NULL)
- tipo_producto (varchar(30), NOT NULL) — PRODUCTO_TERMINADO | MATERIA_PRIMA | MATERIAL_EMPAQUE
- estado (bit, NOT NULL, DEFAULT 1)

---

## PROVEEDOR

Descripción:
Proveedores de ingredientes o materiales.

Columnas:
- id_proveedor (PK, int)
- nombre (nvarchar, NULL)
- id_direccion (FK -&gt; DIRECCION.id_direccion, int, NULL)
- correo_electronico (varchar(150), NULL)
- numero_telefono (varchar(30), NULL)
- id_categoria_proveedor (FK -&gt; CATEGORIA_PROVEEDOR.id_categoria_proveedor, int, NOT NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## PROVINCIA

Descripción:
Provincias para el sistema de direcciones.

Columnas:
- id_provincia (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- id_pais (FK -&gt; PAIS.id_pais, int, NOT NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## PUESTO

Descripción:
Catálogo de puestos de empleados.

Columnas:
- id_puesto (PK, int)
- nombre (varchar(50), NOT NULL, UNIQUE, len &gt; 0)
- descripcion (varchar(255), NULL)
- area (varchar(30), NOT NULL) — PRODUCCION | VENTAS | LOGISTICA | ADMINISTRACION | MANTENIMIENTO
- estado (bit, NOT NULL, DEFAULT 1)

Puestos registrados:

| Área | Puestos |
|---|---|
| PRODUCCION | Panadero, Pastelero, Ayudante de panaderia, Encargado de produccion, Mezclador |
| VENTAS | Cajero, Vendedor, Atencion al cliente, Encargado de tienda |
| LOGISTICA | Repartidor, Auxiliar de almacen, Encargado de inventario |
| ADMINISTRACION | Gerente, Administrador, Contador, Recursos humanos, Encargado de compras |
| MANTENIMIENTO | Limpieza, Mantenimiento |

---

## RECETA_DETALLE

Descripción:
Relación entre productos finales e ingredientes (productos).

Columnas:
- id_receta_detalle (PK, int)
- id_producto_final (FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- id_producto_ingrediente (FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- cantidad (decimal, NOT NULL, &gt; 0)
- unidad_medida (varchar(20), NULL)

Reglas:
- id_producto_final &lt;&gt; id_producto_ingrediente (no circular)

---

## RECLAMACION_VENTA

Descripción:
Reclamaciones asociadas a órdenes de venta.

Columnas:
- id_reclamacion_venta (PK, int)
- fecha (datetime, NOT NULL)
- motivo (text, NOT NULL)
- estado_actual (varchar, NOT NULL) — ABIERTA | EN_PROCESO | RESUELTA | CERRADA
- tipo_reclamacion (varchar, NOT NULL)
- prioridad (varchar(20), NOT NULL) — ALTA | MEDIA | BAJA
- id_factura_venta (FK -&gt; FACTURA_VENTA.id_factura_venta, int, NOT NULL)
- id_empresa_cliente (FK -&gt; CLIENTE.id_cliente, int, NOT NULL)
- id_empleado (FK -&gt; EMPLEADO.id_empleado, int, NULL)
- id_orden_venta (FK -&gt; ORDEN_VENTA.id_orden_venta, int, NOT NULL)

---

## ROL

Descripción:
Roles y permisos del sistema.

Columnas:
- id_rol (PK, int)
- nombre_rol (varchar, NOT NULL)
- descripcion (varchar(255), NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## SECTOR

Descripción:
Sectores/barrios para el sistema de direcciones.

Columnas:
- id_sector (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- id_ciudad (FK -&gt; CIUDAD.id_ciudad, int, NOT NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## TIPO_MOVIMIENTO

Descripción:
Tipos de movimientos de inventario.

Columnas:
- id_tipo (PK, int)
- nombre (varchar, NOT NULL)
- naturaleza (varchar(10), NULL) — ENTRADA | SALIDA

---

## UNIDAD

Descripción:
Unidades de medida.

Columnas:
- id_unidad (PK, int)
- nombre (varchar, NOT NULL, len &gt; 0)
- descripcion (text, NULL)
- categoria (varchar(50), NULL)
- estado (bit, NOT NULL, DEFAULT 1)

---

## USUARIO

Descripción:
Usuarios del sistema.

Columnas:
- id_usuario (PK, int)
- nombre_usuario (varchar, NOT NULL)
- password_hash (varchar, NOT NULL, len &gt;= 8)
- id_rol (FK -&gt; ROL.id_rol, int, NOT NULL)
- estado (bit, NOT NULL)
- fecha_creacion (datetime, NULL)
- id_empleado (FK -&gt; EMPLEADO.id_empleado, int, NULL)

Reglas:
- nombre_usuario único
- password_hash mínimo 8 caracteres
- estado indica si el usuario está activo

---

## VALOR_NUTRICIONAL

Descripción:
Información nutricional de productos.

Columnas:
- id_producto (PK/FK -&gt; PRODUCTO.id_producto, int, NOT NULL)
- calorias (varchar(50), NULL)
- grasas_totales (varchar(50), NULL)
- grasas_saturadas (varchar(50), NULL)
- grasas_trans (varchar(50), NULL)
- carbohidratos (varchar(50), NULL)
- azucares (varchar(50), NULL)
- proteinas (varchar(50), NULL)
- fibra_dietetica (varchar(50), NULL)
- sodio (varchar(50), NULL)

---

# Vistas Importantes

## vw_productos_con_categoria_y_unidad

Descripción:
Vista optimizada para mostrar productos con categoría, unidad y precio.
Ideal para tablas de inventario, pantallas de productos y consultas rápidas.

---

## vw_productos_valor_nutricional

Descripción:
Vista de productos con su información nutricional asociada.

---

## vw_empresas_clientes_con_direccion

Descripción:
Vista completa de clientes con información de dirección.

---

## vw_recetas_completas

Descripción:
Vista completa de recetas y sus ingredientes.

---

## vw_facturas_emitidas_detalle

Descripción:
Vista detallada de facturas emitidas.

---

## vw_produccion_detalle

Descripción:
Vista detallada de producciones.

---

# Relaciones Principales

- PAIS 1:N PROVINCIA
- PROVINCIA 1:N CIUDAD
- CIUDAD 1:N SECTOR
- SECTOR 1:N DIRECCION
- DIRECCION 1:1 CLIENTE
- DIRECCION 1:1 PROVEEDOR
- DIRECCION 1:N ENVIO (como dirección de entrega alternativa)
- ROL 1:N USUARIO
- EMPLEADO 1:1 USUARIO
- EMPLEADO 1:N HISTORICO_RECLAMACIONES
- EMPLEADO 1:N RECLAMACION_VENTA
- EMPLEADO 1:N ENVIO (como transportista)
- EMPLEADO 1:N HISTORICO_TRANSPORTISTA_ENVIO (como anterior)
- EMPLEADO 1:N HISTORICO_TRANSPORTISTA_ENVIO (como nuevo)
- PUESTO 1:N EMPLEADO
- CATEGORIA_PRODUCTO 1:N PRODUCTO
- UNIDAD 1:N PRODUCTO
- PRODUCTO 1:1 INVENTARIO
- PRODUCTO 1:N RECETA_DETALLE (como final)
- PRODUCTO 1:N RECETA_DETALLE (como ingrediente)
- PRODUCTO 1:N DETALLE_ORDEN_VENTA
- PRODUCTO 1:N DETALLE_PRODUCCION
- PRODUCTO 1:N CONSUMO_PRODUCCION
- PRODUCTO 1:1 VALOR_NUTRICIONAL
- CATEGORIA_INGREDIENTE 1:N INGREDIENTE
- INGREDIENTE 1:N DETALLE_COMPRA
- ORDEN_VENTA 1:N DETALLE_ORDEN_VENTA
- ORDEN_VENTA 1:N FACTURA_VENTA
- ORDEN_VENTA 1:N PAGO
- ORDEN_VENTA 1:1 PRODUCCION
- ORDEN_VENTA 1:N RECLAMACION_VENTA
- ORDEN_VENTA 1:1 ENVIO
- CLIENTE 1:N ORDEN_VENTA
- CLIENTE 1:N FACTURA_VENTA
- CLIENTE 1:N RECLAMACION_VENTA
- FACTURA_VENTA 1:N RECLAMACION_VENTA
- RECLAMACION_VENTA 1:N HISTORICO_RECLAMACIONES
- FORMA_PAGO 1:N ORDEN_VENTA
- FORMA_PAGO 1:N FACTURA_VENTA
- FORMA_PAGO 1:N COMPRA_MATERIAL
- FORMA_PAGO 1:N PAGO
- PROVEEDOR 1:N COMPRA_MATERIAL
- CATEGORIA_PROVEEDOR 1:N PROVEEDOR
- COMPRA_MATERIAL 1:N DETALLE_COMPRA
- PRODUCCION 1:N DETALLE_PRODUCCION
- PRODUCCION 1:N CONSUMO_PRODUCCION
- USUARIO 1:N MOVIMIENTO_INVENTARIO
- USUARIO 1:N PRODUCCION
- USUARIO 1:N HISTORICO_ENVIO
- USUARIO 1:N ENVIO (como creador)
- USUARIO 1:N HISTORICO_TRANSPORTISTA_ENVIO
- TIPO_MOVIMIENTO 1:N MOVIMIENTO_INVENTARIO
- ESTADO_ENVIO 1:N ENVIO
- ESTADO_ENVIO 1:N HISTORICO_ENVIO
- ENVIO 1:N HISTORICO_ENVIO
- ENVIO 1:N HISTORICO_TRANSPORTISTA_ENVIO

---

# Reglas para OpenCode

- NO inventar tablas ni columnas.
- Reutilizar vistas existentes cuando sea posible.
- Usar JDBC y PreparedStatement.
- Mantener arquitectura JavaFX existente.
- Reutilizar CSS global actual.
- No modificar estructura SQL sin autorización.