package com.example.motostore.service;

import com.example.motostore.model.Moto;
import com.example.motostore.repository.MotoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotoService {

    private final MotoRepository motoRepository;

    public MotoService(MotoRepository motoRepository) {
        this.motoRepository = motoRepository;
    }

    // =====================================================
    // 1. LISTADOS / CATÁLOGO
    // =====================================================

    /**
     * Devuelve todas las motos (para catálogo, panel admin, etc.)
     */
    public List<Moto> findAll() {
        return motoRepository.findAll();
    }

    /**
     * Devuelve las motos "activas" (usado por MotoRestController).
     * Si tu entidad Moto tiene un campo "active" y un método en el repositorio
     * como findByActiveTrue(), puedes reemplazarlo aquí.
     */
    public List<Moto> listActive() {
        // TODO: si tienes un campo 'active', usar:
        // return motoRepository.findByActiveTrue();
        return motoRepository.findAll();
    }

    /**
     * Busca una moto por ID.
     */
    public Moto findById(Long id) {
        return motoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moto no encontrada: " + id));
    }

    // =====================================================
    // 2. CRUD PARA ADMIN (USADO POR MotoRestController, AdminController)
    // =====================================================

    /**
     * Crea una nueva moto.
     */
    public Moto create(Moto moto) {
        return motoRepository.save(moto);
    }

    /**
     * Actualiza una moto existente.
     */
    public Moto update(Long id, Moto moto) {
        Moto existing = findById(id);   // lanza excepción si no existe
        // Nos aseguramos de mantener el mismo ID
        moto.setId(existing.getId());
        return motoRepository.save(moto);
    }

    /**
     * Elimina una moto por id.
     */
    public void delete(Long id) {
        motoRepository.deleteById(id);
    }

    /**
     * Guarda o actualiza una moto (uso genérico).
     */
    public Moto save(Moto moto) {
        return motoRepository.save(moto);
    }

    // =====================================================
    // 3. LÓGICA DE NEGOCIO (STOCK PARA VENTAS)
    // =====================================================

    /**
     * Reduce el stock de una moto al comprar.
     */
    public void decreaseStock(Long motoId, int quantity) {
        Moto moto = findById(motoId);

        if (moto.getStock() < quantity) {
            // IMPORTANTÍSIMO: no usar getName() (no existe en tu entidad),
            // así evitamos el error de compilación.
            throw new RuntimeException("Stock insuficiente para la moto con id: " + motoId);
        }

        moto.setStock(moto.getStock() - quantity);
        motoRepository.save(moto);
    }

    // =====================================================
    // 4. MÉTODOS DE APOYO PARA ADMINCONTROLLER
    // =====================================================

    /**
     * Devuelve el inventario agrupado por modelo.
     * De momento devolvemos simplemente todas las motos,
     * y la vista se encarga de mostrarlas.
     */
    public List<Moto> getInventoryByModel() {
        return motoRepository.findAll();
    }

    /**
     * Construye una moto vacía para el formulario de creación.
     */
    public Moto buildEmptyMoto() {
        return new Moto();
    }
}
