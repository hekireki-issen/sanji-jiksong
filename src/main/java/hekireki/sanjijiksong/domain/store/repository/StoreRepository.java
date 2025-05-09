package hekireki.sanjijiksong.domain.store.repository;

import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store,Long> {
    boolean existsByUser(User user);
    List<Store> findByNameContainingAndActiveTrue(String keyword);
    Page<Store> findByActiveTrue(Pageable pageable);
    Store findByUserId(Long id);
}
