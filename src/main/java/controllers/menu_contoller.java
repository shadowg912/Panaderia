package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utils.AppNavigator;

import java.awt.*;

public class menu_contoller {
   @FXML
    private Button btnRegistroProveedores;
    @FXML
    private Button btnVerInventario;
    @FXML
    private Button btnCrearEmpleado;
    @FXML
    private Button btnAdminUsuarios;
    @FXML
    private Button btnVerOrdenesProduccion;
    @FXML
    private Button btnGestionClientes;
    @FXML
    private Button btnAdminEmpleados;
    @FXML
    private Button btnVerRecetas;
    AppNavigator appNavigator = new AppNavigator();



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
}
