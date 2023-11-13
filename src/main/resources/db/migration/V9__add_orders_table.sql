CREATE TABLE IF NOT EXISTS organisations_schema.orders
(
	transaction_id                  UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
	merchant_id                     VARCHAR(30) NOT NULL,
	merchant_order_id               VARCHAR(30) NOT NULL,
	buyer_id                        VARCHAR(30) NOT NULL,
	status                          VARCHAR(30) NOT NULL,
	order_created_at                TIMESTAMP NOT NULL
);