package com.proyecto.WebRopa.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.WebRopa.entity.PedidosItems;
import com.proyecto.WebRopa.repository.PedidosItemsRepository;
import com.proyecto.WebRopa.service.IPedidosItemsService;

@Service
public class PedidosItemsService implements IPedidosItemsService {

    private final PedidosItemsRepository repoPedidosItems;
    public PedidosItemsService(PedidosItemsRepository repoPedidosItems) {
        this.repoPedidosItems = repoPedidosItems;
    }

    public List<PedidosItems> buscarPorPedidoId(Long pedidoId) { 
        return repoPedidosItems.findByPedidoId(pedidoId); 
    }
    public Optional<PedidosItems> buscarId(Long id) { 
        return repoPedidosItems.findById(id); 
    }
    public void guardar(PedidosItems item) { 
        repoPedidosItems.save(item); 
    }
    public void eliminar(Long id) {
        repoPedidosItems.deleteById(id);
    }
}