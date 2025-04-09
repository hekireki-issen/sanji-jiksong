package hekireki.sanjijiksong.domain.item.repository;

import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByStoreId(Long storeId);

    Optional<Item> findByStoreIdAndId(Long storeId, Long itemId);

    List<Item> findAllByNameContainingAndItemStatusAndActiveIsTrue(String keyword, ItemStatus itemStatus);

    List<Item> findAllByCategoryContainingAndItemStatusAndActiveIsTrue(String keyword, ItemStatus itemStatus);
}
