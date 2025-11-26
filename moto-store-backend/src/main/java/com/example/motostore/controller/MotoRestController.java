package com.example.motostore.controller;

import com.example.motostore.model.Moto;
import com.example.motostore.service.MotoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motos")
@CrossOrigin
public class MotoRestController {

    private final MotoService motoService;

    public MotoRestController(MotoService motoService) {
        this.motoService = motoService;
    }

    @GetMapping
    public List<Moto> list() {
        return motoService.listActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    
    public Moto create(@RequestBody Moto moto) {
        return motoService.create(moto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Moto update(@PathVariable Long id, @RequestBody Moto moto) {
        return motoService.update(id, moto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        motoService.delete(id);
    }
}
