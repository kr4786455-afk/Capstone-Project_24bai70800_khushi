package com.uniloop.repository;

import com.uniloop.model.BorrowTransaction;
import com.uniloop.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    @Query("""
        select t from BorrowTransaction t
        where t.borrower.id = :userId or t.item.owner.id = :userId
        order by t.requestedAt desc
        """)
    List<BorrowTransaction> findForUser(@Param("userId") Long userId);

    List<BorrowTransaction> findByStatus(TransactionStatus status);

    boolean existsByItemId(Long itemId);

    boolean existsByItemIdAndStatusIn(Long itemId, Collection<TransactionStatus> statuses);

    boolean existsByItemIdAndBorrowerIdAndStatusIn(Long itemId, Long borrowerId, Collection<TransactionStatus> statuses);
}
