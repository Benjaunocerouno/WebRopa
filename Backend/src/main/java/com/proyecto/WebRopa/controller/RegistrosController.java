package com.proyecto.WebRopa.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.WebRopa.entity.Registros;
import com.proyecto.WebRopa.security.JwtUtil;   
import com.proyecto.WebRopa.service.IRegistrosService;




@RestController
@RequestMapping("/api")
public class RegistrosController {

    private final IRegistrosService serviceRegistros;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegistrosController(IRegistrosService serviceRegistros, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.serviceRegistros = serviceRegistros;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registros") //Método GET en POSTMAN
    public List<Registros> buscartodos() {
        return serviceRegistros.buscarTodos();
    }

    @PostMapping("/registros")
     public Registros guardar(@RequestBody Registros registro) {
        try {
            String datos = registro.getNombres() + registro.getApellidos() + registro.getEmail();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(datos.getBytes());
            registro.setCliente_id(new BigInteger(1, md.digest()).toString(16).toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String ClaveOriginal = registro.getEmail()+registro.getNombres()+registro.getApellidos();
        // Encriptar la clave original antes de guardarla en BD
        registro.setLlave_secreta(passwordEncoder.encode(ClaveOriginal));

        serviceRegistros.guardar(registro);
        return registro;
        
    }

    @PutMapping("/registros")
     public Registros modificar(@RequestBody Registros registro) {
        serviceRegistros.modificar(registro);
        return registro;
        
    }
    
    @GetMapping("/registros/{id}")
        public Optional<Registros> buscarId(@PathVariable("id") Integer id) {
            return serviceRegistros.buscarId(id); 
        }


    @DeleteMapping("/registros/{id}")
        public String eliminar(@PathVariable("id") Integer id) {
            serviceRegistros.eliminar(id); 
            return "Registro eliminado";
        }
    @PostMapping("/token")
    public ResponseEntity<?> obtenerToken (
        @RequestBody Map <String, String> credenciales) 
    {
        // Usamos getOrDefault para soportar si lo envían como Cliente_id o cliente_id
        String ClienteId = credenciales.getOrDefault("Cliente_id", credenciales.get("cliente_id"));
        String llaveSecreta = credenciales.get("llave_secreta");
        
        Optional<Registros> user = serviceRegistros
                                    .buscarTodos().stream()
                                 .filter(r -> r.getCliente_id() != null && r.getCliente_id().equals(ClienteId))
                                    .findFirst();
        if (user.isPresent() && passwordEncoder.matches(llaveSecreta, user.get().getLlave_secreta())) {
            String token = jwtUtil.generarToken(ClienteId, null, java.util.Collections.emptyList(), "API");
            Registros registro = user.get();
            registro.setAccess_token(token);
            serviceRegistros.guardar(registro);

            return ResponseEntity.ok(Collections.singletonMap("token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }
}