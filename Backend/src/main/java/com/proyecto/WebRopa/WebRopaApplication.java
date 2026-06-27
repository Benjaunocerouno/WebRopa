package com.proyecto.WebRopa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebRopaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebRopaApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedDatabase(JdbcTemplate jdbcTemplate) {
		return args -> {
			System.out.println("=== VERIFICANDO BASE DE DATOS ===");
			try {
				// ── 1. Seed de permisos ──────────────────────────────────────
				Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles_permisos", Integer.class);
				if (count == null || count == 0) {
					System.out.println("[DB SEED] roles_permisos vacía. Asignando permisos...");
					Long superadminId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE nombre = 'SUPERADMIN'", Long.class);
					Long adminId      = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE nombre = 'ADMIN'", Long.class);
					Long empleadoId   = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE nombre = 'EMPLEADO'", Long.class);
					List<Long> todosLosPermisos = jdbcTemplate.query("SELECT id FROM permisos", (rs, n) -> rs.getLong("id"));
					for (Long permId : todosLosPermisos) {
						if (superadminId != null) jdbcTemplate.update("INSERT INTO roles_permisos (rol_id, permiso_id) VALUES (?, ?)", superadminId, permId);
						if (adminId != null)      jdbcTemplate.update("INSERT INTO roles_permisos (rol_id, permiso_id) VALUES (?, ?)", adminId, permId);
					}
					if (empleadoId != null) {
						List<Long> empPerms = jdbcTemplate.query(
							"SELECT id FROM permisos WHERE nombre IN ('INVENTARIO_AJUSTAR', 'PEDIDOS_GESTIONAR')",
							(rs, n) -> rs.getLong("id")
						);
						for (Long permId : empPerms) jdbcTemplate.update("INSERT INTO roles_permisos (rol_id, permiso_id) VALUES (?, ?)", empleadoId, permId);
					}
					System.out.println("[DB SEED] Permisos asignados correctamente.");
				} else {
					System.out.println("[DB SEED] roles_permisos OK (" + count + " registros).");
				}

				// ── 2. Schema fix: añadir empresa_id si falta ───────────────
				String[] tablesToCheck = {"variantes", "cupones", "pedidos"};
				for (String table : tablesToCheck) {
					try {
						Integer colCount = jdbcTemplate.queryForObject(
							"SELECT COUNT(*) FROM information_schema.COLUMNS " +
							"WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'empresa_id'",
							Integer.class, table
						);
						if (colCount == null || colCount == 0) {
							System.out.println("[DB SEED] Añadiendo empresa_id a '" + table + "'...");
							jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN empresa_id BIGINT DEFAULT NULL");
							try {
								jdbcTemplate.execute(
									"ALTER TABLE " + table +
									" ADD CONSTRAINT fk_" + table + "_empresa " +
									"FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL"
								);
								System.out.println("[DB SEED] empresa_id + FK creados en '" + table + "'.");
							} catch (Exception ex) {
								System.out.println("[DB SEED] Advertencia FK '" + table + "': " + ex.getMessage());
							}
						}
					} catch (Exception e) {
						System.err.println("[DB SEED] Error en tabla '" + table + "': " + e.getMessage());
					}
				}
				System.out.println("=== INICIALIZACIÓN COMPLETADA ===");
			} catch (Exception e) {
				System.err.println("[DB SEED] Error general: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}
}
