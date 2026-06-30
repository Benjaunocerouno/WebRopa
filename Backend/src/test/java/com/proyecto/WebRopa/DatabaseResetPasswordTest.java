package com.proyecto.WebRopa;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DatabaseResetPasswordTest {

    @Test
    public void testResetPassword() {
        String url = "jdbc:mysql://shop.spring.informaticapp.com:3306/shop_tienda_db";
        String user = "shop_ADMINS";
        String pass = "ADMINS146567696768NOSEQUEMASPONER";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("admin123");

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE usuarios SET password = ? WHERE correo = 'superadmin@gmail.com'")) {

            pstmt.setString(1, hashedPassword);
            int rows = pstmt.executeUpdate();
            System.out.println("=== ACTUALIZACIÓN DE CONTRASEÑA DE SUPERADMIN ===");
            System.out.println("Filas afectadas: " + rows);
            System.out.println("Nueva contraseña establecida: admin123");
            System.out.println("=================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
