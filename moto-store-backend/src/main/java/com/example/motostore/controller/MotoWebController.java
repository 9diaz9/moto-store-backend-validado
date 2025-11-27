package com.example.motostore.controller;

import com.example.motostore.model.Moto;
import com.example.motostore.service.MotoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MotoWebController {

    private final MotoService motoService;

    public MotoWebController(MotoService motoService) {
        this.motoService = motoService;
    }

    /**
     * Catálogo para el usuario logueado.
     */
    @GetMapping("/catalog")
    public String showCatalog(Model model) {
        List<Moto> motos = motoService.findAll(); // o findAllAvailable()
        Map<Long, Integer> availableMap = new HashMap<>();
        for (Moto m : motos) {
            availableMap.put(m.getId(), motoService.availableStockForMoto(m));
        }

        model.addAttribute("motos", motos);
        model.addAttribute("availableMap", availableMap);
        return "catalog"; // -> templates/catalog.html
    }
}
