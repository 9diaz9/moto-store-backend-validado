package com.example.motostore.controller;

import com.example.motostore.model.Moto;
import com.example.motostore.service.MotoService;
import com.example.motostore.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Year;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MotoService motoService;
    private final SaleService saleService;

    public AdminController(MotoService motoService, SaleService saleService) {
        this.motoService = motoService;
        this.saleService = saleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("inventoryByModel", motoService.getInventoryByModel());
        model.addAttribute("monthlySales", saleService.getMonthlySalesSummary());
        model.addAttribute("paymentSummary",
                saleService.getPaymentMethodSummaryForCurrentMonth());
        return "admin/dashboard";
    }

    @GetMapping("/motos/new")
    public String newMotoForm(Model model) {

        int currentYear = Year.now().getValue();

        // Moto base con marca fija Suzuki
        Moto moto = motoService.buildEmptyMoto();
        moto.setBrand("Suzuki");

        // límites de año: mínimo = currentYear - 2, máximo = currentYear + 1
        int minYear = currentYear - 2;
        int maxYear = currentYear + 1;

        model.addAttribute("moto", moto);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("minYear", minYear);
        model.addAttribute("maxYear", maxYear);

        return "admin/moto-form";
    }

    @PostMapping("/motos")
    public String saveMoto(@ModelAttribute("moto") @Valid Moto moto,
                           BindingResult bindingResult,
                           Model model) {

        int currentYear = Year.now().getValue();
        int minYear = currentYear - 2;
        int maxYear = currentYear + 1;

        // Validación de rango para el modelo (año)
        if (moto.getYear() != null) {
            if (moto.getYear() < minYear) {
                bindingResult.rejectValue(
                        "year",
                        "moto.year.tooOld",
                        "El modelo no puede ser menor a " + minYear
                );
            }
            if (moto.getYear() > maxYear) {
                bindingResult.rejectValue(
                        "year",
                        "moto.year.tooNew",
                        "El modelo no puede ser mayor a " + maxYear
                );
            }
        }

        // Si hay errores, volvemos al formulario con los mismos límites
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("minYear", minYear);
            model.addAttribute("maxYear", maxYear);
            return "admin/moto-form";
        }

        // Aseguramos que siempre se guarde con marca Suzuki
        moto.setBrand("Suzuki");

        motoService.save(moto);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/sales")
    public String salesHistory(Model model) {
        model.addAttribute("sales", saleService.getAllSales());
        return "admin/sales";
    }
}

