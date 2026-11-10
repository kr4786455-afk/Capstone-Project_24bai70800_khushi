package com.uniloop.repository;

import com.uniloop.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        select i from Item i
        where (lower(i.title) like lower(concat('%', :q, '%'))
               or lower(coalesce(i.description, '')) like lower(concat('%', :q, '%')))
          and (:onlyAvailable = false or i.available = true)
        order by i.createdAt desc
        """)
    List<Item> search(@Param("q") String q, @Param("onlyAvailable") boolean onlyAvailable);

    List<Item> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
