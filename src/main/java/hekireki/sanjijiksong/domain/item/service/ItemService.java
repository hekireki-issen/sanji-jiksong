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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public ItemResponse createItem(ItemCreateRequest itemCreateRequest, String email) {
        Store userStore = verifyAndGetStore( email);

        Item savedItem = itemRepository.save(itemCreateRequest.toEntity(userStore));
        return ItemResponse.from(savedItem);
    }

    public List<ItemResponse> getMyItems(String email) {
        Store userStore = verifyAndGetStore( email);

        List<Item> AllItem = itemRepository.findAllByStoreId(userStore.getId());
        return AllItem.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    public ItemResponse getItemDetail(Long storeId, Long itemId) {
        Item item = getVerifiedItem(storeId, itemId);
        return ItemResponse.from(item);
    }

    @Transactional
    public ItemResponse updateItem(Long itemId, String email, ItemUpdateRequest itemUpdateRequest) {
        Store userStore = verifyAndGetStore(email);

        Item item = getVerifiedItem(userStore.getId(), itemId);
        item.updateIfChanged(itemUpdateRequest);

        return ItemResponse.from(item);
    }

    @Transactional
    public void deactivateItem(Long itemId, String email) {

        Store userStore = verifyAndGetStore(email);

        Item item = getVerifiedItem(userStore.getId(), itemId);
        item.deactivate();
    }

    public ResponseEntity<Map<String, Object>> getSalesOverview(String email) {

        Store userStore = verifyAndGetStore(email);

        //판매량
        List<OrderList> orderLists = orderListRepository.findAllByStoreId(userStore.getId());

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("title", "품목 매출 현황");

        Map<String, ItemSalesSummary> itemSalesData = calculateItemSalesSummary(orderLists);

        List<String> itemNames = new ArrayList<>();
        List<Integer> revenues = new ArrayList<>();
        for (Map.Entry<String, ItemSalesSummary> entry : itemSalesData.entrySet()) {
            itemNames.add(entry.getKey());
            revenues.add(entry.getValue().totalRevenue());
        }

        chartData.put("labels", itemNames);
        chartData.put("series", List.of(revenues));

        return ResponseEntity.ok(chartData);
    }

    public ResponseEntity<?> getTop5BestSellingProducts(String email) {

        Store userStore = verifyAndGetStore(email);
        Map<String, Integer> itemSalesData = rankItemsBySalesAsc(userStore);

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

        Store userStore = verifyAndGetStore(email);
        List<OrderList> orderLists = orderListRepository.findAllByStoreId(userStore.getId());

        Map<String, Integer> itemSalesData = calculateSalesByDayLast7Days(orderLists);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("title", "주간 매출 추이");
        chartData.put("xAxis", new ArrayList<>(itemSalesData.keySet()));
        chartData.put("series", List.of(
                Map.of("name", "매출", "data", new ArrayList<>(itemSalesData.values()))
        ));

        return ResponseEntity.ok(chartData);
    }

    public ResponseEntity<?> getDailyHourlySales(String email, LocalDateTime localDateTime) {

        Store userStore = verifyAndGetStore(email);

        LocalDate targetDate = LocalDate.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth());
        LocalDateTime startOfDay = targetDate.atStartOfDay(); // 00:00:00
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        List<OrderList> orderLists = orderListRepository.findAllByStoreIdAndDate(userStore.getId(), startOfDay, endOfDay);
        Map<String, Integer> itemSalesData = getHourlyRevenueMap(orderLists);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("title", "시간별 매출 추이");
        chartData.put("xAxis", new ArrayList<>(itemSalesData.keySet()));
        chartData.put("series", List.of(
                Map.of("name", "매출", "data", new ArrayList<>(itemSalesData.values()))
        ));

        return ResponseEntity.ok(chartData);
    }

    public Page<ItemResponse> itemSearch(String keyword, Pageable pageable) {
        return itemRepository
                .findAllByNameContainingAndItemStatusAndActiveIsTrue(keyword, ItemStatus.ONSALE, pageable)
                .map(ItemResponse::from);
    }

    public Page<ItemResponse> categorySearch(String keyword, Pageable pageable) {
        return itemRepository
                .findAllByCategoryContainingAndItemStatusAndActiveIsTrue(keyword, ItemStatus.ONSALE, pageable)
                .map(ItemResponse::from);
    }

    private Store verifyAndGetStore(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store userStore = user.getStore();

        if (userStore == null) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        if (!userStore.getId().equals(user.getId())) {
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }
        return userStore;
    }

    private Item getVerifiedItem(Long storeId, Long itemId) {
        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        return item;
    }

    private static Map<String, ItemSalesSummary> calculateItemSalesSummary(List<OrderList> orderLists) {
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
        return itemSalesData;
    }

    private Map<String, Integer> rankItemsBySalesAsc(Store userStore) {
        List<OrderList> orderLists = orderListRepository.findAllByStoreId(userStore.getId());
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
        return itemSalesData;
    }

    private static Map<String, Integer> calculateSalesByDayLast7Days(List<OrderList> orderLists) {
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
        return itemSalesData;
    }

    private static Map<String, Integer> getHourlyRevenueMap(List<OrderList> orderLists) {
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
        return itemSalesData;
    }
}