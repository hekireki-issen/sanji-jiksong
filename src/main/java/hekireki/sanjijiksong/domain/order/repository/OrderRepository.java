package hekireki.sanjijiksong.domain.order.repository;

import hekireki.sanjijiksong.domain.order.entity.Order;
import hekireki.sanjijiksong.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUser(User user, Pageable pageable);

}
