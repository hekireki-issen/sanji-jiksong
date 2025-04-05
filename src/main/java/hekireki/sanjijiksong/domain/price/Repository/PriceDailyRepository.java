package hekireki.sanjijiksong.domain.price.Repository;

import hekireki.sanjijiksong.domain.price.entity.PriceDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PriceDailyRepository extends JpaRepository<PriceDaily, Long> {
    // JpaRepository를 상속받아 PriceDaily 엔티티에 대한 CRUD 및 쿼리 메서드를 사용할 수 있습니다.
    // 추가적인 쿼리 메서드가 필요하다면 여기에 정의할 수 있습니다.
    Optional<PriceDaily> findTopByItemCodeAndRankCodeAndSnapshotDateLessThanOrderBySnapshotDateDesc(
            String itemCode, String rankCode, LocalDate snapshotDate);
}
