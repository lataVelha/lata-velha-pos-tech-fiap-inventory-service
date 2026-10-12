-- PECAS
INSERT INTO PECA (ID, NOME, DESCRICAO, VALOR) VALUES
(1, 'Filtro de óleo', 'Filtro para motor',                35.00),
(2, 'Óleo 5W30',      'Óleo sintético',                   59.90),
(3, 'Parafuso roda',  'Parafuso fixação roda',             5.00),
(4, 'Disco freio',    'Disco freio roda dianteira',       200.00),
(5, 'Pastilha freio', 'Pastilha de freio roda dianteira', 100.00),
(6, 'Pivor',          'Pivor roda dianteira',              70.00),
(7, 'Amortecedor',    'Amortecedor roda dianteira',       270.00);

-- ESTOQUE
INSERT INTO PECA_ESTOQUE (PECA_ID, QUANTIDADE_ARMAZENADA, QUANTIDADE_DISPONIVEL) VALUES
(1, 50, 50),
(2, 10, 10),
(3, 50, 50),
(4,  5,  5),
(6, 10, 10),
(7,  8,  8);

-- SEQUENCE SYNC (tabela seedada com IDs explícitos)
SELECT setval('peca_id_seq', COALESCE((SELECT MAX(ID) FROM PECA), 1), true);
