package controllers;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.util.*;

public class menu_contoller {

    @FXML private VBox sidebarModulos;
    @FXML private AnchorPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Button btnCerrarSesion;

    private static final Map<String, Modulo> MODULOS = new LinkedHashMap<>();
    private static menu_contoller instancia;
    private static String ultimoModuloNombre;
    private Modulo moduloActivo = null;

    private static class SubItem {
        String id, etiqueta, descripcion, fxml;
        SubItem(String id, String etiqueta, String descripcion, String fxml) {
            this.id = id; this.etiqueta = etiqueta;
            this.descripcion = descripcion; this.fxml = fxml;
        }
    }

    private static class Modulo {
        String nombre, icono;
        List<SubItem> items;
        VBox sidebarSubBox;
        HBox sidebarHeader;
        boolean expandido;
        Modulo(String nombre, String icono, List<SubItem> items) {
            this.nombre = nombre; this.icono = icono;
            this.items = items; this.expandido = false;
        }
    }

    static {
        MODULOS.put("VENTAS", new Modulo("VENTAS", "V",
            List.of(
                new SubItem("btnDashboardVentas", "Dashboard", "Reportes y estadísticas de ventas", "/view/Dashboard_ventas.fxml"),
                new SubItem("btnNuevaOrden", "Nueva Orden", "Crear una orden de venta para un cliente", "/view/Crear_ordenventa.fxml"),
                new SubItem("btnHistorialVenta", "Historial de Ventas", "Consultar ventas realizadas", "/view/Historial_ventas.fxml"),
                new SubItem("btnSeguimientroEnvio", "Seguimiento Envíos", "Rastrear estado de entregas", "/view/Seguimiento_envio.fxml"),
                new SubItem("btnGuardarDIrecciones", "Guardar Direcciones", "Administrar direcciones de entrega", "/view/Guardar_Direccion.fxml")
            )));
        MODULOS.put("INVENTARIO", new Modulo("INVENTARIO", "I",
            List.of(
                new SubItem("btnDashboardInventario", "Dashboard", "Resumen y estadísticas del inventario", "/view/Dashboard_inventario.fxml"),
                new SubItem("btnRegistroProducto", "Registrar Producto", "Añadir un nuevo producto al catálogo", "/view/Registrar_productos.fxml"),
                new SubItem("btnVerInventario", "Ver Inventario", "Consultar stock actual de productos", "/view/Inventario.fxml"),
                new SubItem("btnVerMovimientos", "Ver Movimientos", "Historial de entradas y salidas", "/view/Movimientos_inventario.fxml"),
                new SubItem("btnRegistroRecetas", "Recetas", "Gestionar recetas de productos terminados", "/view/Registro_recetas.fxml")
            )));
        MODULOS.put("PRODUCCION", new Modulo("PRODUCCIÓN", "P",
            List.of(
                new SubItem("btnDashboardProduccion", "Dashboard", "Resumen y estadísticas de producción", "/view/Dashboard_produccion.fxml"),
                new SubItem("btnOrdenProduccion", "Nueva Orden de Producción", "Crear orden de producción con uno o varios productos", "/view/Orden_produccion_simple.fxml"),
                new SubItem("btnVerOrdenesProduccion", "Ver Órdenes", "Consultar órdenes de producción", "/view/Ver_ordenes_produccion.fxml"),
                new SubItem("btnVerRecetas", "Ver Recetas", "Consultar recetas disponibles", "/view/Ver_recetas.fxml")
            )));
        MODULOS.put("COMPRAS", new Modulo("COMPRAS", "C",
            List.of(
                new SubItem("btnDashboardCompras", "Dashboard", "Resumen y estadísticas de compras", "/view/Dashboard_compras.fxml"),
                new SubItem("btnNuevaCompra", "Nueva Compra", "Realizar una compra a proveedor", "/view/Nueva_compra.fxml"),
                new SubItem("btnGestionCompras", "Gestionar Compras", "Administrar compras y entrada de stock", "/view/Gestion_compras.fxml"),
                new SubItem("btnRegistroProveedor", "Registrar Proveedor", "Añadir un nuevo proveedor", "/view/Registrar_proveedor.fxml"),
                new SubItem("btnAdminProveedores", "Administrar Proveedores", "Gestionar proveedores existentes", "/view/Gestion_proveedores.fxml")
            )));
        MODULOS.put("CLIENTES", new Modulo("CLIENTES", "C",
            List.of(
                new SubItem("btnRegistroCliente", "Registrar Cliente", "Añadir un nuevo cliente", "/view/Registro_empresa.fxml"),
                new SubItem("btnGestionClientes", "Gestionar Clientes", "Administrar clientes existentes", "/view/Gestion_clientes.fxml"),
                new SubItem("btnReclamaciones", "Reclamaciones", "Gestionar reclamaciones de clientes", "/view/Reclamaciones.fxml")
            )));
        MODULOS.put("SISTEMA", new Modulo("SISTEMA", "S",
            List.of(
                new SubItem("btnCrearUsuario", "Crear Usuario", "Crear un nuevo usuario del sistema", "/view/Creacio_usuario.fxml"),
                new SubItem("btnAdminUsuarios", "Administrar Usuarios", "Gestionar usuarios del sistema", "/view/Admin_usuarios.fxml"),
                new SubItem("btnCrearEmpleado", "Crear Empleado", "Registrar un nuevo empleado", "/view/Crear_empleado.fxml"),
                new SubItem("btnAdminEmpleados", "Administrar Empleados", "Gestionar empleados", "/view/Admin_empleados.fxml")
            )));
    }

    private static final Map<String, Set<String>> PERMISOS = new HashMap<>();
    static {
        PERMISOS.put("Administrador", Set.of(
            "btnDashboardVentas","btnNuevaOrden","btnHistorialVenta","btnSeguimientroEnvio","btnGuardarDIrecciones",
            "btnDashboardInventario","btnRegistroProducto","btnVerInventario","btnVerMovimientos","btnRegistroRecetas",
            "btnDashboardProduccion","btnOrdenProduccion","btnVerOrdenesProduccion","btnVerRecetas",
            "btnDashboardCompras","btnNuevaCompra","btnGestionCompras","btnRegistroProveedor","btnAdminProveedores",
            "btnRegistroCliente","btnGestionClientes","btnReclamaciones",
            "btnCrearUsuario","btnAdminUsuarios","btnAdminEmpleados","btnCrearEmpleado"
        ));
        PERMISOS.put("Encargado de Almacén", Set.of(
            "btnDashboardInventario","btnRegistroProducto","btnVerInventario","btnVerMovimientos","btnVerRecetas",
            "btnGestionCompras","btnRegistroProveedor","btnAdminProveedores"
        ));
        PERMISOS.put("Encargado de Área", Set.of(
            "btnDashboardVentas","btnHistorialVenta","btnSeguimientroEnvio","btnVerInventario","btnVerMovimientos",
            "btnDashboardProduccion","btnVerOrdenesProduccion","btnVerRecetas",
            "btnGestionClientes","btnReclamaciones"
        ));
        PERMISOS.put("Encargado de Producción", Set.of(
            "btnDashboardProduccion","btnVerInventario","btnVerMovimientos","btnOrdenProduccion","btnVerOrdenesProduccion",
            "btnVerRecetas","btnRegistroRecetas"
        ));
        PERMISOS.put("Encargado de Compras", Set.of(
            "btnDashboardCompras","btnVerInventario","btnVerMovimientos","btnNuevaCompra","btnGestionCompras","btnRegistroProveedor","btnAdminProveedores"
        ));
        PERMISOS.put("Repartidor", Set.of("btnSeguimientroEnvio"));
    }

    @FXML
    public void initialize() {
        instancia = this;

        String nombre = SesionUsuario.getNombreEmpleado() != null
                ? SesionUsuario.getNombreEmpleado()
                : SesionUsuario.getNombreUsuario();
        lblUserName.setText(nombre);
        lblUserRole.setText(SesionUsuario.getNombreRol());

        AppNavigator.setContentPane(contentArea);

        construirSidebar();
        mostrarPrimerModuloDisponible();
    }

    private void construirSidebar() {
        Set<String> allowed = PERMISOS.getOrDefault(SesionUsuario.getNombreRol(), Collections.emptySet());
        sidebarModulos.getChildren().clear();

        for (Modulo m : MODULOS.values()) {
            boolean tieneAcceso = m.items.stream().anyMatch(s -> allowed.contains(s.id));
            if (!tieneAcceso) continue;

            VBox contenedor = new VBox();
            contenedor.getStyleClass().add("sidebar-modulo");

            HBox header = new HBox();
            header.getStyleClass().add("sidebar-item");
            header.setAlignment(Pos.CENTER_LEFT);
            header.setSpacing(12);
            header.setPadding(new Insets(14, 20, 14, 20));

            Label lblIcono = new Label(m.icono);
            lblIcono.getStyleClass().add("sidebar-item-icon");

            Label lblNombre = new Label(m.nombre);
            lblNombre.getStyleClass().add("sidebar-item-label");
            HBox.setHgrow(lblNombre, Priority.ALWAYS);

            Label lblArrow = new Label("▾");
            lblArrow.getStyleClass().add("sidebar-item-arrow");

            header.getChildren().addAll(lblIcono, lblNombre, lblArrow);

            VBox subBox = new VBox();
            subBox.getStyleClass().add("sidebar-subitems");
            subBox.setMaxHeight(0);
            subBox.setOpacity(0);
            subBox.setVisible(false);
            subBox.setManaged(false);

            for (SubItem si : m.items) {
                if (!allowed.contains(si.id)) continue;
                HBox subItem = new HBox();
                subItem.getStyleClass().add("sidebar-subitem");
                Label lblSub = new Label(si.etiqueta);
                subItem.getChildren().add(lblSub);
                subItem.setOnMouseClicked(e -> navegarA(si));
                subBox.getChildren().add(subItem);
            }

            m.sidebarSubBox = subBox;
            m.sidebarHeader = header;

            header.setOnMouseClicked(e -> toggleModulo(m, header, lblArrow));
            contenedor.getChildren().addAll(header, subBox);
            sidebarModulos.getChildren().add(contenedor);
        }
    }

    private void toggleModulo(Modulo m, HBox header, Label arrow) {
        if (moduloActivo != null && moduloActivo != m) {
            colapsarModulo(moduloActivo);
        }

        m.expandido = !m.expandido;
        if (m.expandido) {
            m.sidebarSubBox.setVisible(true);
            m.sidebarSubBox.setManaged(true);
            header.getStyleClass().add("active");
            arrow.setText("▴");
            TranslateTransition t = new TranslateTransition(Duration.millis(200), m.sidebarSubBox);
            t.setFromY(-20);
            t.setToY(0);
            t.play();
            m.sidebarSubBox.setOpacity(1);
            m.sidebarSubBox.setMaxHeight(m.sidebarSubBox.prefHeight(-1) > 0 ? Region.USE_COMPUTED_SIZE : 200);
            moduloActivo = m;
        } else {
            colapsarModulo(m);
        }

        mostrarSubmenu(m);
    }

    private void colapsarModulo(Modulo m) {
        m.expandido = false;
        m.sidebarSubBox.setVisible(false);
        m.sidebarSubBox.setManaged(false);
        m.sidebarSubBox.setOpacity(0);
        m.sidebarSubBox.setMaxHeight(0);
        if (m.sidebarHeader != null) {
            m.sidebarHeader.getStyleClass().remove("active");
            Label arrow = (Label) m.sidebarHeader.getChildren().get(2);
            arrow.setText("▾");
        }
        if (moduloActivo == m) moduloActivo = null;
    }

    private void mostrarSubmenu(Modulo m) {
        ultimoModuloNombre = m.nombre;
        contentArea.getChildren().clear();

        VBox contenedor = new VBox(4);
        AnchorPane.setTopAnchor(contenedor, 0.0);
        AnchorPane.setBottomAnchor(contenedor, 0.0);
        AnchorPane.setLeftAnchor(contenedor, 0.0);
        AnchorPane.setRightAnchor(contenedor, 0.0);

        Label header = new Label(m.nombre);
        header.getStyleClass().add("modulo-header");

        Label subheader = new Label("Seleccione una opción para continuar");
        subheader.getStyleClass().add("modulo-subheader");

        FlowPane botones = new FlowPane();
        botones.getStyleClass().add("modulo-botones");

        Set<String> allowed = PERMISOS.getOrDefault(SesionUsuario.getNombreRol(), Collections.emptySet());

        for (SubItem si : m.items) {
            if (!allowed.contains(si.id)) continue;

            VBox card = new VBox(8);
            card.getStyleClass().add("modulo-card");
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new Insets(24));

            Label icono = new Label(si.etiqueta.substring(0, 1));
            icono.getStyleClass().add("modulo-card-icon");

            Label titulo = new Label(si.etiqueta);
            titulo.getStyleClass().add("modulo-card-title");

            Label desc = new Label(si.descripcion);
            desc.getStyleClass().add("modulo-card-desc");
            desc.setWrapText(true);

            card.getChildren().addAll(icono, titulo, desc);
            card.setOnMouseClicked(e -> navegarA(si));

            botones.getChildren().add(card);
        }

        contenedor.getChildren().addAll(header, subheader, botones);
        contentArea.getChildren().add(contenedor);
    }

    private void navegarA(SubItem si) {
        if (si.fxml != null) {
            AppNavigator.navigateTo(si.fxml);
        }
    }

    private void mostrarPrimerModuloDisponible() {
        for (Modulo m : MODULOS.values()) {
            if (m.sidebarSubBox != null) {
                m.expandido = true;
                m.sidebarHeader.getStyleClass().add("active");
                Label arrow = (Label) m.sidebarHeader.getChildren().get(2);
                arrow.setText("▴");
                mostrarSubmenu(m);
                break;
            }
        }
    }

    public static void volverAlMenu() {
        if (instancia == null || instancia.contentArea == null) {
            AppNavigator.load("/view/Menu.fxml");
            return;
        }
        Modulo modulo = MODULOS.get(ultimoModuloNombre);
        if (modulo == null) {
            for (Modulo m : MODULOS.values()) {
                if (m.sidebarHeader != null) { modulo = m; break; }
            }
        }
        if (modulo != null) {
            instancia.mostrarSubmenu(modulo);
        }
    }

    public void fnCerrarSesion(ActionEvent actionEvent) {
        SesionUsuario.cerrarSesion();
        AppNavigator.load("/view/login_usuarios.fxml");
    }
}
