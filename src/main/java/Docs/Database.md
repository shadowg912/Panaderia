# Base de Datos - PANADERIA

## Tecnologías
- SQL Server
- JDBC
- JavaFX
- Arquitectura basada en escenas

---

# Tablas Principales

## USUARIO

Descripción:
Usuarios del sistema.

Columnas:
- id_usuario (PK)
- nombre_usuario
- password_hash
- id_rol (FK -> ROL.id_rol)
- estado
- fecha_creacion
- id_empleado (FK -> EMPLEADO.id_empleado)

Reglas:
- nombre_usuario único
- password_hash mínimo 8 caracteres
- estado indica si el usuario está activo

---

## ROL

Descripción:
Roles y permisos del sistema.

Columnas:
- id_rol (PK)
- nombre_rol
- descripcion

---

## EMPLEADO

Descripción:
Información de empleados.

Columnas:
- id_empleado (PK)
- nombre
- apellido1
- apellido2
- numero_telefono
- puesto

---

## PRODUCTO

Descripción:
Productos vendidos por la panadería.

Columnas:
- id_producto (PK)
- nombre
- id_categoria_producto (FK)
- precio_unitario
- id_unidad (FK)

Relaciones:
- PRODUCTO -> CATEGORIA_PRODUCTO
- PRODUCTO -> UNIDAD

---

## INVENTARIO

Descripción:
Control de stock actual de productos.

Columnas:
- id_producto (PK/FK -> PRODUCTO.id_producto)
- stock_actual
- fecha_actualizacion

Reglas:
- stock_actual >= 0

---

## MOVIMIENTO_INVENTARIO

Descripción:
Historial de entradas y salidas de inventario.

Columnas:
- id_movimiento (PK)
- id_producto (FK)
- tipo
- cantidad
- id_usuario (FK)
- fecha
- id_tipo_movimiento (FK)

Reglas:
- tipo = ENTRADA | SALIDA
- cantidad > 0

---

## TIPO_MOVIMIENTO

Descripción:
Tipos de movimientos de inventario.

Columnas:
- id_tipo (PK)
- nombre

---

## CATEGORIA_PRODUCTO

Descripción:
Categorías de productos.

Columnas:
- id_categoria_producto (PK)
- nombre
- descripcion

---

## INGREDIENTE

Descripción:
Ingredientes usados en recetas.

Columnas:
- id_ingrediente (PK)
- nombre
- unidad_medida
- id_categoria_ingrediente (FK)

---

## RECETA_PRODUCTO

Descripción:
Relación entre productos e ingredientes.

Columnas:
- id_producto (FK)
- id_ingrediente (FK)
- cantidad_ingrediente

Relación:
- PRODUCTO N:M INGREDIENTE

---

## ORDEN_VENTA

Descripción:
Órdenes de venta realizadas por clientes.

Columnas:
- id_orden_venta (PK)
- id_empresa_cliente (FK)
- estado
- fecha_orden
- id_forma_pago (FK)
- subtotal
- itbis
- monto_total
- fecha_entrega

Estados:
- PENDIENTE
- FACTURADA
- CANCELADA

---

## DETALLE_ORDEN_VENTA

Descripción:
Productos incluidos en una orden de venta.

Columnas:
- id_orden_venta (FK)
- id_producto (FK)
- cantidad
- precio_unitario
- subtotal

---

## FACTURA_VENTA

Descripción:
Facturas generadas para órdenes de venta.

Columnas:
- id_factura_venta (PK)
- id_empresa_cliente (FK)
- numero_factura
- fecha_emision
- estado
- subtotal
- itbis
- monto_total
- id_forma_pago (FK)
- id_orden_venta (FK)

Estados:
- EMITIDA
- PAGADA
- ANULADA

---

## EMPRESA_CLIENTE

Descripción:
Empresas clientes de la panadería.

Columnas:
- id_empresa_cliente (PK)
- razon_social
- rnc
- id_direccion (FK)
- telefono
- correo_electronico

---

## PROVEEDOR

Descripción:
Proveedores de ingredientes o materiales.

Columnas:
- id_proveedor (PK)
- nombre
- id_direccion (FK)
- correo_electronico
- numero_telefono
- id_categoria_proveedor (FK)

---

## COMPRA_MATERIAL

Descripción:
Compras realizadas a proveedores.

Columnas:
- id_compra_material (PK)
- fecha
- estado
- id_forma_pago (FK)
- id_proveedor (FK)

Estados:
- PENDIENTE
- PAGADA
- CANCELADA

---

## DETALLE_COMPRA

Descripción:
Ingredientes comprados en una compra.

Columnas:
- id_compra_material (FK)
- id_ingrediente (FK)
- cantidad
- costo_unitario
- monto_total

---

# Vistas Importantes

## vw_productos_con_categoria_y_unidad

Descripción:
Vista optimizada para mostrar productos con:
- categoría
- unidad
- precio

Ideal para:
- tablas de inventario
- pantallas de productos
- consultas rápidas

---

## vw_recetas_completas

Descripción:
Vista completa de recetas y sus ingredientes.

---

## vw_facturas_emitidas_detalle

Descripción:
Vista detallada de facturas emitidas.

---

## vw_empresas_clientes_con_direccion

Descripción:
Vista completa de clientes con información de dirección.

---

# Relaciones Principales

- ROL 1:N USUARIO
- EMPLEADO 1:1 USUARIO
- PRODUCTO 1:1 INVENTARIO
- PRODUCTO N:M INGREDIENTE
- ORDEN_VENTA 1:N DETALLE_ORDEN_VENTA
- PRODUCTO 1:N DETALLE_ORDEN_VENTA
- FACTURA_VENTA -> ORDEN_VENTA
- PROVEEDOR 1:N COMPRA_MATERIAL

---

# Reglas para OpenCode

- NO inventar tablas ni columnas.
- Reutilizar vistas existentes cuando sea posible.
- Usar JDBC y PreparedStatement.
- Mantener arquitectura JavaFX existente.
- Reutilizar CSS global actual.
- No modificar estructura SQL sin autorización.