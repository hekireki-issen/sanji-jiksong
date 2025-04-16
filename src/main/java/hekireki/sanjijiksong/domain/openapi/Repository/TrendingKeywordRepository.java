package hekireki.sanjijiksong.domain.openapi.Repository;

import hekireki.sanjijiksong.domain.openapi.entity.TrendingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrendingKeywordRepository extends JpaRepository<TrendingKeyword, Long> {
    List<TrendingKeyword> findByCreateDate(LocalDate createDate);
}
