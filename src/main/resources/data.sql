-- 테스트 유저
INSERT INTO users (id, email, password, nickname, address, role, created_at, active)
VALUES (1, 'test@test.com', '1234', '테스트유저', '서울시', 'BUYER', CURRENT_TIMESTAMP, true);

-- 테스트 스토어 (item이 store_id를 참조하니까 하나 넣어야 함)
INSERT INTO store (id, user_id, name, address, created_at, active)
VALUES (1, 1, '테스트스토어', '서울시 강남구', CURRENT_TIMESTAMP, true);

-- 테스트 아이템
INSERT INTO item (id, store_id, name, price, image, stock, active, item_status, created_at)
VALUES (1, 1, '테스트 상품', 10000, 'image.png', 10, true, 'ONSALE', CURRENT_TIMESTAMP);

-- 테스트 카트
INSERT INTO cart (id, user_id, created_at)
VALUES (1, 1, CURRENT_TIMESTAMP);

-- 테스트 카트 아이템
INSERT INTO cart_item (id, cart_id, item_id, quantity, created_at)
VALUES (1, 1, 1, 2, CURRENT_TIMESTAMP);
