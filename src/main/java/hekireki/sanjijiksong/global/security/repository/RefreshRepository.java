package hekireki.sanjijiksong.global.security.repository;

import hekireki.sanjijiksong.global.security.entity.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByRefreshToken(String refresh);

    @Transactional
    void deleteByRefreshToken(String refresh);
}
