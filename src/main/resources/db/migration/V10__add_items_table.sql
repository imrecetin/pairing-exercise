CREATE TABLE IF NOT EXISTS organisations_schema.items
(
	id                          UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
	transaction_id              UUID,
	item_id                     VARCHAR(30) NOT NULL,
	price                       BIGINT NOT NULL,
	status                      VARCHAR(30) NOT NULL
);