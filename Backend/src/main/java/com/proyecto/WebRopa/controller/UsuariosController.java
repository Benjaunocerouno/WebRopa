package com.proyecto.WebRopa.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.proyecto.WebRopa.entity.Carritos;
import com.proyecto.WebRopa.entity.Usuarios;
import com.proyecto.WebRopa.entity.Roles;
import com.proyecto.WebRopa.security.JwtUtil;
import com.proyecto.WebRopa.service.ICarritosService;
import com.proyecto.WebRopa.service.IUsuariosService;
import com.proyecto.WebRopa.service.IRolesServices;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UsuariosController {

    private final IUsuariosService serviceUsuarios;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ICarritosService serviceCarritos;
    private final IRolesServices serviceRoles;
    private final JwtUtil jwtUtil;

    @Autowired
    private com.proyecto.WebRopa.service.seguridad.SeguridadEnVivoService seguridadEnVivoService;

    public UsuariosController(IUsuariosService serviceUsuarios, BCryptPasswordEncoder passwordEncoder, ICarritosService serviceCarritos, IRolesServices serviceRoles, JwtUtil jwtUtil) {
        this.serviceUsuarios = serviceUsuarios;
        this.passwordEncoder = passwordEncoder;
        this.serviceCarritos = serviceCarritos;
        this.serviceRoles = serviceRoles;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/usuarios/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales, HttpServletRequest request) {
        String correo = credenciales.get("correo");
        String password = credenciales.get("password");

        if (correo == null || password == null) {
            return ResponseEntity.badRequest().body("Correo y contraseña requeridos");
        }

        Optional<Usuarios> userOpt = serviceUsuarios.buscarPorCorreo(correo);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        Usuarios user = userOpt.get();
        String rolNombre = user.getRol().getNombre();
        Long empresaId = user.getEmpresa() != null ? user.getEmpresa().getId() : null;
        
        java.util.List<String> permisos = new java.util.ArrayList<>();
        if (user.getRol().getPermisos() != null) {
            for (com.proyecto.WebRopa.entity.Permisos p : user.getRol().getPermisos()) {
                permisos.add(p.getNombre());
            }
        }

        String token = jwtUtil.generarToken(user.getId().toString(), empresaId, permisos, rolNombre);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("nombre", user.getNombre());
        response.put("correo", user.getCorreo());
        response.put("rol", rolNombre);
        if (empresaId != null) {
            response.put("empresa_id", empresaId);
        }
        response.put("permisos", permisos);

        // Registrar la sesión exitosa en el monitor de seguridad en vivo
        seguridadEnVivoService.registrarLoginExitoso(token, user.getId(), rolNombre, request.getRemoteAddr(), user.getCorreo());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/usuarios/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            seguridadEnVivoService.cerrarSesionPorToken(token);
        }
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }

    @GetMapping("/usuarios/me")
    public ResponseEntity<?> obtenerMe(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        String token = header.substring(7);
        if (!jwtUtil.validarToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String userId = jwtUtil.extraerUsuarioId(token);
        Optional<Usuarios> userOpt;
        try {
            userOpt = serviceUsuarios.buscarId(Long.parseLong(userId));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuarios user = userOpt.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("nombre", user.getNombre());
        response.put("correo", user.getCorreo());
        response.put("telefono", user.getTelefono());
        response.put("rol", user.getRol().getNombre());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/usuarios/me")
    public ResponseEntity<?> modificarMiPerfil(HttpServletRequest request, @RequestBody Usuarios usuarioDatos) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        String token = header.substring(7);
        if (!jwtUtil.validarToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String userIdStr = jwtUtil.extraerUsuarioId(token);
        Optional<Usuarios> existenteOpt;
        try {
            existenteOpt = serviceUsuarios.buscarId(Long.parseLong(userIdStr));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }

        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuarios existente = existenteOpt.get();

        if (usuarioDatos.getNombre() != null && !usuarioDatos.getNombre().trim().isEmpty()) {
            existente.setNombre(usuarioDatos.getNombre().trim());
        }

        if (usuarioDatos.getTelefono() != null) {
            String telefono = usuarioDatos.getTelefono();
            if (!telefono.matches("\\d{9}")) {
                return ResponseEntity.badRequest().body("El teléfono debe contener exactamente 9 dígitos.");
            }
            existente.setTelefono(telefono);
        }

        serviceUsuarios.guardar(existente);
        return ResponseEntity.ok("Perfil actualizado correctamente");
    }

    // Registro de nuevo usuario
    @PostMapping("/usuarios/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuarios usuario) {
        // Verificar que el correo no exista ya
        boolean correoEnUso = serviceUsuarios.buscarTodos().stream()
                .anyMatch(u -> u.getCorreo().equals(usuario.getCorreo()));

        if (correoEnUso) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El correo ya está registrado");
        }

        // Validar teléfono: exactamente 9 dígitos numéricos, sin espacios ni letras,
        // y no permitir que todos los dígitos sean iguales (ej. 000000000 o 999999999)
        String telefono = usuario.getTelefono();
        if (!telefono.matches("^\\d{9}$") || telefono.matches("^(\\d)\\1{8}$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Teléfono inválido. Debe contener exactamente 9 dígitos numéricos.");
        }

        // Todo nuevo registro es CLIENTE buscando dinámicamente el rol
        Optional<Roles> rolCliente = serviceRoles.buscarPorNombre("CLIENTE");
        if (!rolCliente.isPresent()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno: No se encontró el rol CLIENTE en la base de datos.");
        }
        usuario.setRol(rolCliente.get()); 

        serviceUsuarios.guardar(usuario);
        
        // ── Crear carrito automáticamente ──────────────
        Carritos carrito = new Carritos();
        carrito.setUsuario(usuario);
        serviceCarritos.guardar(carrito);
        // ───────────────────────────────────────────────
        return ResponseEntity.ok(usuario);
    }

    // Registro directo de Administrador por el SuperAdmin
    @PostMapping("/usuarios/admin")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public ResponseEntity<?> registrarAdmin(@RequestBody Usuarios usuario) {
        boolean correoEnUso = serviceUsuarios.buscarTodos().stream()
                .anyMatch(u -> u.getCorreo().equals(usuario.getCorreo()));

        if (correoEnUso) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El correo ya está registrado");
        }

        String telefono = usuario.getTelefono();
        if (telefono == null || !telefono.matches("^\\d{9}$") || telefono.matches("^(\\d)\\1{8}$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Teléfono inválido. Debe contener exactamente 9 dígitos numéricos.");
        }

        if (usuario.getEmpresa() == null || usuario.getEmpresa().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Debe especificar la empresa para este administrador.");
        }

        Optional<Roles> rolAdmin = serviceRoles.buscarPorNombre("ADMIN");
        if (!rolAdmin.isPresent()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno: No se encontró el rol ADMIN en la base de datos.");
        }
        usuario.setRol(rolAdmin.get()); 

        serviceUsuarios.guardar(usuario);
        
        Carritos carrito = new Carritos();
        carrito.setUsuario(usuario);
        serviceCarritos.guardar(carrito);
        
        return ResponseEntity.ok(usuario);
    }

    // Listar todos (solo ADMIN debera acceder, eso se controla en SecurityConfig)
    @GetMapping("/usuarios")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public List<Usuarios> listarTodos() {
        return serviceUsuarios.buscarTodos();
    }

    @GetMapping("/usuarios/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public Optional<Usuarios> buscarPorId(@PathVariable Long id) {
        return serviceUsuarios.buscarId(id);
    }

    @PutMapping("/usuarios")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public ResponseEntity<?> modificar(@RequestBody Usuarios usuario) {
        if (usuario.getId() == null) {
            return ResponseEntity.badRequest().body("Debe especificar el id del usuario");
        }

        Optional<Usuarios> existenteOpt = serviceUsuarios.buscarId(usuario.getId());
        if (!existenteOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario no existe");
        }

        Usuarios existente = existenteOpt.get();

        // Actualización parcial (solo modifica lo que venga en el body)
        if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) {
            existente.setNombre(usuario.getNombre().trim());
        }

        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            String nuevoCorreo = usuario.getCorreo().trim();
            if (!nuevoCorreo.equals(existente.getCorreo())) {
                boolean correoEnUso = serviceUsuarios.buscarTodos().stream()
                        .anyMatch(u -> u.getCorreo().equals(nuevoCorreo));
                if (correoEnUso) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El correo ya está registrado por otro usuario");
                }
            }
            existente.setCorreo(nuevoCorreo);
        }

        if (usuario.getTelefono() != null) {
            String telefono = usuario.getTelefono();
            if (!telefono.matches("^\\d{9}$") || telefono.matches("^(\\d)\\1{8}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Teléfono inválido. Debe contener exactamente 9 dígitos numéricos.");
            }
            existente.setTelefono(telefono);
        }

        if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
            existente.setPassword(usuario.getPassword());
        }

        // Actualización del rol (para que un admin pueda ascender/cambiar roles)
        if (usuario.getRol() != null && usuario.getRol().getId() != null) {
            Optional<Roles> rolOpt = serviceRoles.buscarId(usuario.getRol().getId());
            if (!rolOpt.isPresent()) {
                return ResponseEntity.badRequest().body("El rol especificado no existe");
            }
            existente.setRol(rolOpt.get());
        }

        if (usuario.getTallaUniforme() != null) existente.setTallaUniforme(usuario.getTallaUniforme());
        if (usuario.getDescuentoEmpleado() != null) existente.setDescuentoEmpleado(usuario.getDescuentoEmpleado());
        if (usuario.getEspecialidad() != null) existente.setEspecialidad(usuario.getEspecialidad());
        if (usuario.getSucursal() != null) existente.setSucursal(usuario.getSucursal());

        serviceUsuarios.guardar(existente);
        return ResponseEntity.ok(existente);
    }

    @DeleteMapping("/usuarios/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public String eliminar(@PathVariable Long id) {
        serviceUsuarios.eliminar(id);
        return "Usuario eliminado";
    }
}