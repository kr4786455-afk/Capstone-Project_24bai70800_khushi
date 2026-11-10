package com.uniloop.service;

import com.uniloop.config.ApiException;
import com.uniloop.dto.Dtos.*;
import com.uniloop.model.*;
import com.uniloop.repository.BorrowTransactionRepository;
import com.uniloop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumSet;
import java.util.List;

@Service
public class ItemService {
    private static final EnumSet<TransactionStatus> ACTIVE =
            EnumSet.of(TransactionStatus.REQUESTED, TransactionStatus.APPROVED, TransactionStatus.HANDED_OVER);

    private final ItemRepository items;
    private final BorrowTransactionRepository txs;

    public ItemService(ItemRepository items, BorrowTransactionRepository txs) {
        this.items = items;
        this.txs = txs;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> search(String q, Category category, boolean onlyAvailable) {
        String query = (q == null) ? "" : q.trim();
        return items.search(query, onlyAvailable).stream()
                .filter(i -> category == null || i.getCategory() == category)
                .map(ItemDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto get(Long id) { return ItemDto.from(find(id)); }

    @Transactional(readOnly = true)
    public List<ItemDto> mine(User owner) {
        return items.findByOwnerIdOrderByCreatedAtDesc(owner.getId()).stream().map(ItemDto::from).toList();
    }

    @Transactional
    public ItemDto create(User owner, ItemRequest r) {
        Item i = new Item();
        i.setOwner(owner);
        apply(i, r);
        return ItemDto.from(items.save(i));
    }

    @Transactional
    public ItemDto update(User user, Long id, ItemRequest r) {
        Item i = ownedBy(user, id);
        apply(i, r);
        return ItemDto.from(i);
    }

    @Transactional
    public ItemDto setAvailability(User user, Long id, boolean available) {
        Item i = ownedBy(user, id);
        if (available && txs.existsByItemIdAndStatusIn(id, List.of(TransactionStatus.APPROVED, TransactionStatus.HANDED_OVER)))
            throw ApiException.conflict("Item is currently reserved or on loan");
        i.setAvailable(available);
        return ItemDto.from(i);
    }

    @Transactional
    public void delete(User user, Long id) {
        Item i = ownedBy(user, id);
        if (txs.existsByItemId(id))
            throw ApiException.conflict("This item has borrowing history, so it can't be deleted. Mark it unavailable instead.");
        items.delete(i);
    }

    private Item find(Long id) {
        return items.findById(id).orElseThrow(() -> ApiException.notFound("Item not found"));
    }

    private Item ownedBy(User user, Long id) {
        Item i = find(id);
        if (!i.getOwner().getId().equals(user.getId()))
            throw ApiException.forbidden("You can only change your own items");
        return i;
    }

    private void apply(Item i, ItemRequest r) {
        i.setTitle(r.title().trim());
        i.setDescription(r.description());
        i.setCategory(r.category());
        i.setCondition(r.condition());
        i.setImageUrl(r.imageUrl() == null || r.imageUrl().isBlank() ? null : r.imageUrl().trim());
        i.setEstimatedValue(r.estimatedValue() == null ? 0 : r.estimatedValue());
        i.setMaxBorrowDays(r.maxBorrowDays() == null ? 7 : r.maxBorrowDays());
    }
}
