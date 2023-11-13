INSERT INTO organisations_schema.orders (
	merchant_id,
	merchant_order_id,
	buyer_id,
	status,
	order_created_at
)
VALUES
	('111', '11', '10', 'SHIPPING', '2023-01-01 10:00:00'),
	('222', '22', '20', 'SHIPPING', '2023-01-02 12:30:00'),
	('333', '33', '30', 'PAID', '2023-01-03 15:45:00');

INSERT INTO organisations_schema.items (
	transaction_id,
	item_id,
	price,
	status
)
VALUES
	((select transaction_id from organisations_schema.orders where merchant_id='111' and merchant_order_id='11'), '1', 1000, 'ORDERED'),
	((select transaction_id from organisations_schema.orders where merchant_id='111' and merchant_order_id='11'), '2', 100, 'ORDERED'),
	((select transaction_id from organisations_schema.orders where merchant_id='111' and merchant_order_id='11'), '3', 120, 'ORDERED'),
	((select transaction_id from organisations_schema.orders where merchant_id='222' and merchant_order_id='22'), '1', 1000, 'PAID_TO_MERCHANT'),
	((select transaction_id from organisations_schema.orders where merchant_id='222' and merchant_order_id='22'), '2', 100, 'ORDERED'),
	((select transaction_id from organisations_schema.orders where merchant_id='222' and merchant_order_id='22'), '3', 120, 'ORDERED'),
	((select transaction_id from organisations_schema.orders where merchant_id='333' and merchant_order_id='33'), '1', 1000, 'PAID_TO_MERCHANT'),
	((select transaction_id from organisations_schema.orders where merchant_id='333' and merchant_order_id='33'), '2', 100, 'PAID_TO_MERCHANT'),
	((select transaction_id from organisations_schema.orders where merchant_id='333' and merchant_order_id='33'), '3', 120, 'PAID_TO_MERCHANT');