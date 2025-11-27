package com.example.motostore.service;

import com.example.motostore.model.Moto;
import com.example.motostore.repository.MotoRepository;
import com.example.motostore.repository.MotoStockHistoryRepository;
import com.example.motostore.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotoService {

    private final MotoRepository motoRepository;
    private final CartRepository cartRepository;
    private final MotoStockHistoryRepository historyRepository;

    public MotoService(MotoRepository motoRepository, CartRepository cartRepository, MotoStockHistoryRepository historyRepository) {
        this.motoRepository = motoRepository;
        this.cartRepository = cartRepository;
        this.historyRepository = historyRepository;
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
        return saveWithHistory(moto);
    }

    /**
     * Actualiza una moto existente.
     */
    public Moto update(Long id, Moto moto) {
        Moto existing = findById(id);   // lanza excepción si no existe
        // Nos aseguramos de mantener el mismo ID
        moto.setId(existing.getId());
        return saveWithHistory(moto);
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
        return saveWithHistory(moto);
    }

    private Moto saveWithHistory(Moto moto) {
        boolean isNew = moto.getId() == null;

        if (!isNew) {
            // update: detect stock change
            Moto existing = findById(moto.getId());
            Integer prev = existing.getStock() == null ? 0 : existing.getStock();
            Moto saved = motoRepository.save(moto);
            Integer now = saved.getStock() == null ? 0 : saved.getStock();
            if (!now.equals(prev)) {
                recordStockChange(saved, prev, now, now - prev, "Modificación manual de stock");
            }
            return saved;
        } else {
            // new moto: check if other motos of same brand+model exist
            Moto saved = motoRepository.save(moto);
            try {
                java.util.List<Moto> others = motoRepository.findByBrandAndModel(saved.getBrand(), saved.getModel());
                int totalOthers = 0;
                for (Moto m : others) {
                    if (m.getId().equals(saved.getId())) continue;
                    totalOthers += m.getStock() == null ? 0 : m.getStock();
                }
                int prevTotal = totalOthers;
                int newTotal = prevTotal + (saved.getStock() == null ? 0 : saved.getStock());
                recordStockChange(saved, prevTotal, newTotal, (saved.getStock() == null ? 0 : saved.getStock()),
                        prevTotal > 0 ? "Nueva entrada de stock para referencia existente" : "Creación de nueva referencia");
            } catch (Exception ex) {
                // si el repositorio falla por alguna razón, no bloqueamos la creación
            }
            return saved;
        }
    }

    private void recordStockChange(Moto moto, int previous, int now, int change, String note) {
        try {
            com.example.motostore.model.MotoStockHistory ev = new com.example.motostore.model.MotoStockHistory();
            ev.setMoto(moto);
            ev.setBrand(moto.getBrand());
            ev.setModel(moto.getModel());
            ev.setPreviousStock(previous);
            ev.setNewStock(now);
            ev.setChangeAmount(change);
            // intentar obtener usuario admin desde el contexto de seguridad
            try {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) ev.setAdminUser(auth.getName());
            } catch (Exception ignored) {}
            ev.setNote(note);
            historyRepository.save(ev);
        } catch (Exception ex) {
            // No interrumpir la operación principal por fallos en el guardado del histórico
        }
    }

    // =====================================================
    // 3. LÓGICA DE NEGOCIO (STOCK PARA VENTAS)
    // =====================================================

    /**
     * Reduce el stock de una moto al comprar.
     */
    public void decreaseStock(Long motoId, int quantity) {
        Moto moto = findById(motoId);
        int stock = moto.getStock() == null ? 0 : moto.getStock();
        if (stock < quantity) {
            throw new RuntimeException("Stock insuficiente para la moto con id: " + motoId);
        }

        moto.setStock(stock - quantity);
        motoRepository.save(moto);
    }

    /**
     * Devuelve el stock disponible para una moto teniendo en cuenta
     * las unidades actualmente reservadas en todos los carritos.
     */
    public int availableStockForMoto(Moto moto) {
        Long reserved = cartRepository.sumQuantityByMotoId(moto.getId());
        int reservedInt = reserved == null ? 0 : reserved.intValue();
        int stock = moto.getStock() == null ? 0 : moto.getStock();
        int avail = stock - reservedInt;
        return Math.max(avail, 0);
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
