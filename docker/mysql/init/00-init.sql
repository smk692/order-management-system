-- Grant privileges to oms user
GRANT ALL PRIVILEGES ON oms.* TO 'oms'@'%';
FLUSH PRIVILEGES;

-- Set character set
ALTER DATABASE oms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
