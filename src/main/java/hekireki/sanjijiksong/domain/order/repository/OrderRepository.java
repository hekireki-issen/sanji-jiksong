package hekireki.sanjijiksong.domain.order.repository;

import hekireki.sanjijiksong.domain.order.entity.Order;
import hekireki.sanjijiksong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUser(User user);

}
