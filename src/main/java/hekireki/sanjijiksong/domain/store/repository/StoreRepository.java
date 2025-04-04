package hekireki.sanjijiksong.domain.store.repository;

import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store,Long> {
    boolean existsByUser(User user);
    boolean existsByUserId(Long userId);
}
