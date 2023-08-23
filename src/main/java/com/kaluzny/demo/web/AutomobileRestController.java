package com.kaluzny.demo.web;

import com.kaluzny.demo.domain.Automobile;
import com.kaluzny.demo.domain.AutomobileRepository;
import com.kaluzny.demo.dto.AutoRequestDto;
import com.kaluzny.demo.dto.AutoResponseDto;
import com.kaluzny.demo.service.AutomobileService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AutomobileRestController implements AutomobileOpenApi {

    private final AutomobileService automobileService;
    private final AutomobileRepository repository;

    @Transactional
    @PostConstruct
    public void init() {
        repository.save(new Automobile(UUID.randomUUID(), "Ford", "Green", LocalDateTime.now(), LocalDateTime.now(), true, false));
    }

    @PostMapping("/automobiles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSON')")
    public ResponseEntity<UUID> saveAutomobile(@Valid @RequestBody AutoRequestDto automobile) {
        return automobileService.saveAutomobileAndPushMessage(automobile);
    }

    @GetMapping("/automobiles")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public Collection<AutoResponseDto> getAllAutomobiles() {
        return automobileService.getAllAutomobiles();
    }

    @GetMapping("/automobiles/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public AutoResponseDto getAutomobileById(@PathVariable UUID id) {
        return automobileService.getAutomobileById(id);
    }

    @GetMapping(value = "/automobiles", params = {"name"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public Collection<AutoResponseDto> findAutomobileByName(@RequestParam(value = "name") String name) {
        return automobileService.findAutomobileByName(name);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PERSON')")
    @PutMapping("/automobiles/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AutoResponseDto refreshAutomobile(@PathVariable UUID id, @RequestBody AutoRequestDto automobile) {
        return automobileService.refreshAutomobile(id, automobile);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PERSON')")
    @DeleteMapping("/automobiles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAutomobileById(@PathVariable UUID id) {
        automobileService.removeAutomobileById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/automobiles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllAutomobiles() {
        automobileService.removeAllAutomobiles();
    }

    @GetMapping(value = "/automobiles", params = {"color"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Collection<AutoResponseDto>> findAutomobileByColor(@RequestParam(value = "color") String color) {
        return automobileService.findAutomobileByColor(color);
    }

    @GetMapping(value = "/automobiles", params = {"name", "color"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public Collection<AutoResponseDto> findAutomobileByNameAndColor(
            @RequestParam(value = "name") String name, @RequestParam(value = "color") String color) {
        return automobileService.findAutomobileByNameAndColor(name, color);
    }

    @GetMapping(value = "/automobiles", params = {"colorStartsWith"})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public Collection<AutoResponseDto> findAutomobileByColorStartsWith(
            @RequestParam(value = "colorStartsWith") String colorStartsWith,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size) {
        return automobileService.findAutomobileByColorStartsWith(colorStartsWith, page, size);
    }
}
