-- 创建数据库
CREATE DATABASE IF NOT EXISTS spring_jdbc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE spring_jdbc;

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '书名',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN号',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    publish_date DATE COMMENT '出版日期',
    category VARCHAR(50) COMMENT '分类',
    stock INT DEFAULT 0 COMMENT '库存数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- 插入测试数据
INSERT INTO books (title, author, isbn, price, publish_date, category, stock) VALUES
('Spring实战', '张三', '978-7-111-12345-6', 89.00, '2023-01-15', '技术', 50),
('Java核心技术', '李四', '978-7-111-23456-7', 128.00, '2023-03-20', '技术', 30),
('设计模式', '王五', '978-7-111-34567-8', 79.00, '2023-02-10', '技术', 25),
('算法导论', '赵六', '978-7-111-45678-9', 158.00, '2023-04-05', '算法', 15),
('数据库系统概念', '钱七', '978-7-111-56789-0', 118.00, '2023-01-30', '数据库', 20);