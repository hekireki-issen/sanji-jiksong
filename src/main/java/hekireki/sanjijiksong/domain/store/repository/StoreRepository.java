package hekireki.sanjijiksong.domain.store.repository;

import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store,Long> {
    boolean existsByUser(User user);
    List<Store> findAllByActiveTrue();
    List<Store> findByNameContainingAndActiveTrue(String keyword);
    Store findByUserId(Long id);
}
