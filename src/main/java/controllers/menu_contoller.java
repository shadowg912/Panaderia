package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.util.*;

public class menu_contoller {
    @FXML private Button btnNuevaOrden, btnHistorialVenta, btnSeguimientroEnvio, btnGuardarDIrecciones;
    @FXML private Button btnRegistroProducto, btnVerInventario, btnRegistroProveedores, btnRegistroRecetas;
    @FXML private Button btnOrdenProduccion, btnVerOrdenesProduccion, btnVerRecetas;
    @FXML private Button btnRegistroCliente, btnGestionClientes, btnReclamaciones;
    @FXML private Button btnCrearUsuario, btnAdminUsuarios, btnAdminEmpleados, btnCrearEmpleado, btnCerrarSesion;
    @FXML private Label lblUsuario;
    @FXML private VBox cardVentas, cardInventario, cardProduccion, cardClientes, cardSistema;
    
    AppNavigator appNavigator = new AppNavigator();

    private static final Map<String, Set<String>> PERMISOS = new HashMap<>();
    static {
        PERMISOS.put("Administrador", Set.of(
            "btnNuevaOrden", "btnHistorialVenta", "btnSeguimientroEnvio", "btnGuardarDIrecciones",
            "btnRegistroProducto", "btnVerInventario", "btnRegistroProveedores", "btnRegistroRecetas",
            "btnOrdenProduccion", "btnVerOrdenesProduccion", "btnVerRecetas",
            "btnRegistroCliente", "btnGestionClientes", "btnReclamaciones",
            "btnCrearUsuario", "btnAdminUsuarios", "btnAdminEmpleados", "btnCrearEmpleado"
        ));

        PERMISOS.put("Encargado de Almacén", Set.of(
            "btnRegistroProducto", "btnVerInventario", "btnRegistroProveedores",
            "btnVerRecetas"
        ));

        PERMISOS.put("Encargado de Área", Set.of(
            "btnHistorialVenta", "btnSeguimientroEnvio",
            "btnVerInventario",
            "btnVerOrdenesProduccion", "btnVerRecetas",
            "btnGestionClientes", "btnReclamaciones"
        ));

        PERMISOS.put("Encargado de Producción", Set.of(
            "btnVerInventario",
            "btnOrdenProduccion", "btnVerOrdenesProduccion", "btnVerRecetas",
            "btnRegistroRecetas"
        ));

        PERMISOS.put("Encargado de Compras", Set.of(
            "btnVerInventario", "btnRegistroProveedores"
        ));

        PERMISOS.put("Repartidor", Set.of(
            "btnSeguimientroEnvio"
        ));
    }

    private static final Map<String, List<Button>> CARD_BOTONES = new LinkedHashMap<>();
    private static final Map<String, VBox> CARDS = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        String nombre = SesionUsuario.getNombreEmpleado() != null
                ? SesionUsuario.getNombreEmpleado()
                : SesionUsuario.getNombreUsuario();
        lblUsuario.setText(nombre + " — " + SesionUsuario.getNombreRol());

        Set<String> allowed = PERMISOS.getOrDefault(SesionUsuario.getNombreRol(), Collections.emptySet());

        List<Button> todos = Arrays.asList(
            btnNuevaOrden, btnHistorialVenta, btnSeguimientroEnvio, btnGuardarDIrecciones,
            btnRegistroProducto, btnVerInventario, btnRegistroProveedores, btnRegistroRecetas,
            btnOrdenProduccion, btnVerOrdenesProduccion, btnVerRecetas,
            btnRegistroCliente, btnGestionClientes, btnReclamaciones,
            btnCrearUsuario, btnAdminUsuarios, btnAdminEmpleados, btnCrearEmpleado
        );

        for (Button b : todos) {
            boolean vis = allowed.contains(b.getId());
            b.setVisible(vis);
            b.setManaged(vis);
        }

        // Hide cards with no visible buttons
        Map<VBox, List<Button>> cards = Map.of(
            cardVentas, Arrays.asList(btnNuevaOrden, btnHistorialVenta, btnSeguimientroEnvio, btnGuardarDIrecciones),
            cardInventario, Arrays.asList(btnRegistroProducto, btnVerInventario, btnRegistroProveedores, btnRegistroRecetas),
            cardProduccion, Arrays.asList(btnOrdenProduccion, btnVerOrdenesProduccion, btnVerRecetas),
            cardClientes, Arrays.asList(btnRegistroCliente, btnGestionClientes, btnReclamaciones),
            cardSistema, Arrays.asList(btnCrearUsuario, btnAdminUsuarios, btnAdminEmpleados, btnCrearEmpleado)
        );

        for (Map.Entry<VBox, List<Button>> e : cards.entrySet()) {
            boolean anyVisible = e.getValue().stream().anyMatch(Button::isVisible);
            e.getKey().setVisible(anyVisible);
            e.getKey().setManaged(anyVisible);
        }

        // Sección title also hide if no cards visible
        boolean hayCards = cards.values().stream()
                .flatMap(Collection::stream).anyMatch(Button::isVisible);
    }



    public void fnIrSeguimientoEnvio(ActionEvent actionEvent) {
     try {
      appNavigator.load("/view/Seguimiento_envio.fxml");
     }catch (Exception e){
     }
    }

    public void fnIrHistorialVentas(ActionEvent actionEvent) {
     try {
      appNavigator.load("/view/Historial_ventas.fxml");
     }catch (Exception e){
     }
    }

    public void fnIrCrearOrden(ActionEvent actionEvent) {
     try {
      appNavigator.load("/view/Crear_ordenventa.fxml");
     }catch (Exception e){
     }
    }

    public void fnIrRegistroProveedores(ActionEvent actionEvent) {
     try {
      appNavigator.load("/view/Registrar_proveedor.fxml");
     }catch (Exception e){
     }
    }

public void fnIrRegistrarProducto(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Registrar_productos.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrVerInventario(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Inventario.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrRegistroRecetas(ActionEvent actionEvent) { try {
     appNavigator.load("/view/Registro_recetas.fxml");
    }catch (Exception e){
    }
    }

public void fnIrOrdenProduccion(ActionEvent actionEvent) { try {
      appNavigator.load("/view/Orden_produccion.fxml");
     }catch (Exception e){
     }
    }

    public void fnIrVerOrdenesProduccion(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Ver_ordenes_produccion.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrVerRecetas(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Ver_recetas.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrRegistroCliente(ActionEvent actionEvent) {
     try {
      appNavigator.load("/view/Registro_empresa.fxml");
     }catch (Exception e){
     }
    }

    public void fnIrReclamaciones(ActionEvent actionEvent) {
     try {
     appNavigator.load("/view/Reclamaciones.fxml");
    }catch (Exception e){
    }
    }

public void fnIrCrearUsuario(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Creacio_usuario.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrCrearEmpleado(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Crear_empleado.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrAdminUsuarios(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Admin_usuarios.fxml");
      }catch (Exception e){
      }
    }

    public void fnIrAdminEmpleados(ActionEvent actionEvent) {
      try {
       appNavigator.load("/view/Admin_empleados.fxml");
      }catch (Exception e){
      }
    }
  public void fnirconfirmarorden(ActionEvent actionEvent) {
     try {
      appNavigator.load("/view/Confirmar_orden.fxml");
     }catch (Exception e){}
  }
 public void fnIrGuardarDIrecciones(ActionEvent actionEvent) {
      appNavigator.load("/view/Guardar_Direccion.fxml");
  }

  public void fnIrGestionClientes(ActionEvent actionEvent) {
      appNavigator.load("/view/Gestion_clientes.fxml");
  }

  public void fnCerrarSesion(ActionEvent actionEvent) {
      SesionUsuario.cerrarSesion();
      appNavigator.load("/view/login_usuarios.fxml");
  }
}
