package com.uniloop.controller;

import com.uniloop.dto.Dtos.*;
import com.uniloop.model.Category;
import com.uniloop.security.AuthUser;
import com.uniloop.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService items;
    public ItemController(ItemService items) { this.items = items; }

    @GetMapping
    public List<ItemDto> search(@RequestParam(required = false) String q,
                                @RequestParam(required = false) Category category,
                                @RequestParam(defaultValue = "false") boolean onlyAvailable) {
        return items.search(q, category, onlyAvailable);
    }

    @GetMapping("/mine")
    public List<ItemDto> mine(@AuthenticationPrincipal AuthUser me) { return items.mine(me.getUser()); }

    @GetMapping("/{id}")
    public ItemDto get(@PathVariable Long id) { return items.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ItemRequest r) {
        return items.create(me.getUser(), r);
    }

    @PutMapping("/{id}")
    public ItemDto update(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @Valid @RequestBody ItemRequest r) {
        return items.update(me.getUser(), id, r);
    }

    @PatchMapping("/{id}/availability")
    public ItemDto availability(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @RequestParam boolean value) {
        return items.setAvailability(me.getUser(), id, value);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) { items.delete(me.getUser(), id); }
}
