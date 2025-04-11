package hekireki.sanjijiksong.domain.item.service;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemSalesSummary;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import hekireki.sanjijiksong.domain.order.entity.OrderList;
import hekireki.sanjijiksong.domain.order.repository.OrderListRepository;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.ItemException;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import hekireki.sanjijiksong.global.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderListRepository orderListRepository;

    public ItemResponse createItem(Long storeId, ItemCreateRequest itemCreateRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store userStore = user.getStore();

        if (userStore == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        if (!userStore.getId().equals(storeId)) {
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        Item savedItem = itemRepository.save(itemCreateRequest.toEntity(userStore));
        return ItemResponse.from(savedItem);
    }

    public List<ItemResponse> getMyItems(Long storeId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store userStore = user.getStore();

        if (userStore == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        if (!userStore.getId().equals(storeId)) {
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        List<Item> AllItem = itemRepository.findAllByStoreId(storeId);
        return AllItem.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    public ItemResponse getItemDetail(Long storeId, Long itemId) {
        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        return ItemResponse.from(item);
    }

    @Transactional
    public ItemResponse updateItem(Long storeId, Long itemId, String email, ItemUpdateRequest itemUpdateRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if (!store.getId().equals(storeId)) {
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        item.updateIfChanged(itemUpdateRequest);

        return ItemResponse.from(item);
    }

    @Transactional
    public void deactivateItem(Long storeId, Long itemId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if (!store.getId().equals(storeId)) {
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        item.deactivate();
    }

    public ResponseEntity<Map<String, Object>> getSalesOverview(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if (store == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        //판매량
        List<OrderList> orderLists = orderListRepository.findAllByStoreId(store.getId());

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("title", "품목 매출 현황");

        Map<String, ItemSalesSummary> itemSalesData = new HashMap<>();
        for (OrderList order : orderLists) {
            String itemName = order.getItem().getName();
            int price = order.getCountPrice();
            int count = order.getCount();

            itemSalesData.compute(itemName, (key, summary) -> {
                if (summary == null) {
                    return new ItemSalesSummary(price, count);
                } else {
                    return new ItemSalesSummary(
                            summary.totalRevenue() + price,
                            summary.totalCount() + count
                    );
                }
            });
        }

        List<String> itemNames = new ArrayList<>();
        List<Integer> revenues = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (Map.Entry<String, ItemSalesSummary> entry : itemSalesData.entrySet()) {
            itemNames.add(entry.getKey());
            revenues.add(entry.getValue().totalRevenue());
            counts.add(entry.getValue().totalCount());
        }

        chartData.put("labels", itemNames);
        chartData.put("series", List.of(revenues));

        return ResponseEntity.ok(chartData);
    }


    public ResponseEntity<?> getTop5BestSellingProducts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if (store == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }
        List<OrderList> orderLists = orderListRepository.findAllByStoreId(store.getId());
        Map<String, Integer> itemSalesData = new HashMap<>();

        for (OrderList order : orderLists) {
            String itemName = order.getItem().getName();
            int price = order.getCountPrice();

            itemSalesData.put(itemName, itemSalesData.getOrDefault(itemName, 0) + price);
        }

        List<String> keySet = new ArrayList<>(itemSalesData.keySet());

        keySet.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return itemSalesData.get(o1).compareTo(itemSalesData.get(o2));
            }
        });

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(itemSalesData.entrySet());
        sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        List<String> top5ItemNames = new ArrayList<>();
        List<Integer> top5Revenues = new ArrayList<>();

        for (int i = 0; i < Math.min(5, sortedEntries.size()); i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            top5ItemNames.add(entry.getKey());
            top5Revenues.add(entry.getValue());
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("xAxis", top5ItemNames);
        chartData.put("series", List.of(
                Map.of("name", "판매금액", "data", top5Revenues)
        ));

        // ResponseEntity로 반환
        return ResponseEntity.ok(chartData);
    }

    public ResponseEntity<?> getWeeklySalesTrend(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if (store == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }
        List<OrderList> orderLists = orderListRepository.findAllByStoreId(store.getId());
        Map<String, Integer> itemSalesData = new HashMap<>();

        for (OrderList order : orderLists) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String saleDate;

            if (order.getModifiedAt() == null && order.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                saleDate = order.getCreatedAt().format(formatter);
            } else if (order.getModifiedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                saleDate = order.getModifiedAt().format(formatter);
            } else {
                continue;
            }

            int price = order.getCountPrice();

            itemSalesData.put(saleDate, itemSalesData.getOrDefault(saleDate, 0) + price);
        }
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("title", "주간 매출 추이");
        chartData.put("xAxis", new ArrayList<>(itemSalesData.keySet()));
        chartData.put("series", List.of(
                Map.of("name", "매출", "data", new ArrayList<>(itemSalesData.values()))
        ));

        return ResponseEntity.ok(chartData);
    }

    public ResponseEntity<?> getDailyHourlySales(String email, LocalDateTime localDateTime) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if (store == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        LocalDate targetDate = LocalDate.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth());

        LocalDateTime startOfDay = targetDate.atStartOfDay(); // 00:00:00
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        List<OrderList> orderLists = orderListRepository.findAllByStoreIdAndDate(store.getId(), startOfDay, endOfDay);
        Map<String, Integer> itemSalesData = new HashMap<>();

        for (OrderList order : orderLists) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
            String saleDate;

            if (order.getModifiedAt() == null) {
                saleDate = order.getCreatedAt().format(formatter);
            } else {
                saleDate = order.getModifiedAt().format(formatter);
            }

            int price = order.getCountPrice();

            itemSalesData.put(saleDate, itemSalesData.getOrDefault(saleDate, 0) + price);
        }
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("title", "시간별 매출 추이");
        chartData.put("xAxis", new ArrayList<>(itemSalesData.keySet()));
        chartData.put("series", List.of(
                Map.of("name", "매출", "data", new ArrayList<>(itemSalesData.values()))
        ));
        return ResponseEntity.ok(chartData);


    public List<ItemResponse> itemSearch(String keyword) {
        List<Item> items = itemRepository.findAllByNameContainingAndItemStatusAndActiveIsTrue(keyword, ItemStatus.ONSALE);

        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    public Object categorySearch(String keyword) {
        List<Item> items = itemRepository.findAllByCategoryContainingAndItemStatusAndActiveIsTrue(keyword, ItemStatus.ONSALE);

        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }
}
