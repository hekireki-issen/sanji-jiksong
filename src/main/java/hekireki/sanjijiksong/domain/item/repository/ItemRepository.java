package hekireki.sanjijiksong.domain.item.repository;

import hekireki.sanjijiksong.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
