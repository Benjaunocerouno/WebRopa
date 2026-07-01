package com.proyecto.WebRopa.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Verificando existencia de la tabla 'sucursales'...");
            
            String createSucursalesSql = "CREATE TABLE IF NOT EXISTS sucursales (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(255) NOT NULL, " +
                    "direccion VARCHAR(255) NOT NULL, " +
                    "telefono VARCHAR(255), " +
                    "ciudad VARCHAR(255), " +
                    "estado VARCHAR(50) DEFAULT 'ACTIVO' NOT NULL, " +
                    "empresa_id BIGINT NOT NULL, " +
                    "CONSTRAINT fk_sucursales_empresas FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
                    
            jdbcTemplate.execute(createSucursalesSql);
            logger.info("Tabla 'sucursales' verificada/creada exitosamente.");

            logger.info("Verificando columnas en la tabla 'usuarios'...");
            
            // Añadir columna talla_uniforme
            try {
                jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN talla_uniforme VARCHAR(10);");
                logger.info("Columna 'talla_uniforme' añadida a 'usuarios'.");
            } catch (Exception e) {
                // Ignore if it already exists
            }
            
            // Añadir columna descuento_empleado
            try {
                jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN descuento_empleado DOUBLE;");
                logger.info("Columna 'descuento_empleado' añadida a 'usuarios'.");
            } catch (Exception e) {
            }
            
            // Añadir columna especialidad
            try {
                jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN especialidad VARCHAR(100);");
                logger.info("Columna 'especialidad' añadida a 'usuarios'.");
            } catch (Exception e) {
            }
            
            // Añadir columna sucursal_id
            try {
                jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN sucursal_id BIGINT;");
                jdbcTemplate.execute("ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_sucursales FOREIGN KEY (sucursal_id) REFERENCES sucursales(id) ON DELETE SET NULL;");
                logger.info("Columna 'sucursal_id' y FK añadida a 'usuarios'.");
            } catch (Exception e) {
            }

        } catch (Exception ex) {
            logger.error("Error al inicializar esquemas de base de datos personalizados: " + ex.getMessage());
        }
    }
}
