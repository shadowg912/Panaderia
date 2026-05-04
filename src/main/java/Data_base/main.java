package Data_base;
import Data_base.CONEXION;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class main {

    public static void main(String[] args) {
        CONEXION con = new CONEXION();

        try (Connection conn = con.establecerconexio()) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");

            if (rs.next()) {
                System.out.println("✅ Conexión funcionando correctamente");
            }

        } catch (Exception e) {
            System.out.println("❌ Falló la conexión");
            e.printStackTrace();
        }
    }
}

