package hekireki.sanjijiksong.domain.price.Repository;

import hekireki.sanjijiksong.domain.price.entity.TrendingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrendingKeywordRepository extends JpaRepository<TrendingKeyword, Long> {
    List<TrendingKeyword> findByCreateDate(LocalDate createDate);
}
