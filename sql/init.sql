-- ============================================================
--  Forno di Pietra Ristorante — Schema do Banco de Dados
--  MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS fornodipietra
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE fornodipietra;

-- ============================================================
--  1. PRATOS
-- ============================================================
CREATE TABLE IF NOT EXISTS prato (
    id           INT          NOT NULL AUTO_INCREMENT,
    codigo       VARCHAR(10)  NOT NULL UNIQUE,
    nome         VARCHAR(100) NOT NULL,
    preco        DECIMAL(10,2) NOT NULL,
    ingredientes TEXT         NOT NULL,
    categoria    ENUM('ENTRADA','PRATO_PRINCIPAL','SOBREMESA','BEBIDA') NOT NULL,
    disponivel   BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_prato_categoria (categoria),
    INDEX idx_prato_disponivel (disponivel)
) ENGINE=InnoDB;

-- ============================================================
--  2. CLIENTES
-- ============================================================
CREATE TABLE IF NOT EXISTS cliente (
    id             INT          NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(100) NOT NULL,
    telefone       VARCHAR(20)  NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    total_pedidos  INT          NOT NULL DEFAULT 0,
    criado_em      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_cliente_email (email)
) ENGINE=InnoDB;

-- ============================================================
--  3. PEDIDOS
-- ============================================================
CREATE TABLE IF NOT EXISTS pedido (
    id           INT            NOT NULL AUTO_INCREMENT,
    cliente_id   INT            NOT NULL,
    data_hora    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    status       ENUM('ABERTO','FINALIZADO','CANCELADO') NOT NULL DEFAULT 'ABERTO',
    observacao   VARCHAR(500)   NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_pedido_cliente (cliente_id),
    INDEX idx_pedido_status (status),
    INDEX idx_pedido_data (data_hora)
) ENGINE=InnoDB;

-- ============================================================
--  4. ITENS DO PEDIDO
-- ============================================================
CREATE TABLE IF NOT EXISTS item_pedido (
    id               INT           NOT NULL AUTO_INCREMENT,
    pedido_id        INT           NOT NULL,
    prato_id         INT           NOT NULL,
    quantidade       INT           NOT NULL,
    preco_unitario   DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_prato
        FOREIGN KEY (prato_id) REFERENCES prato(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_item_pedido (pedido_id),
    INDEX idx_item_prato (prato_id)
) ENGINE=InnoDB;

-- ============================================================
--  5. FIDELIDADE (Carta d'Oro)
-- ============================================================
CREATE TABLE IF NOT EXISTS fidelidade (
    id                 INT      NOT NULL AUTO_INCREMENT,
    cliente_id         INT      NOT NULL UNIQUE,
    pontos_acumulados  INT      NOT NULL DEFAULT 0,
    tier               ENUM('BRONZE','PRATA','OURO') NOT NULL DEFAULT 'BRONZE',
    atualizado_em      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_fidelidade_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_fidelidade_tier (tier)
) ENGINE=InnoDB;

-- ============================================================
--  6. RESGATES DE FIDELIDADE
-- ============================================================
CREATE TABLE IF NOT EXISTS resgate_fidelidade (
    id                 INT           NOT NULL AUTO_INCREMENT,
    cliente_id         INT           NOT NULL,
    tipo_resgate       ENUM('DESCONTO','PRATO_GRATIS') NOT NULL,
    prato_id           INT           NULL,
    desconto_aplicado  DECIMAL(5,2)  NULL COMMENT 'Percentual de desconto (ex: 5.00, 10.00)',
    pontos_usados      INT           NOT NULL,
    data_resgate       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_resgate_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_resgate_prato
        FOREIGN KEY (prato_id) REFERENCES prato(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_resgate_cliente (cliente_id),
    INDEX idx_resgate_data (data_resgate)
) ENGINE=InnoDB;

-- ============================================================
--  DADOS DE EXEMPLO — Pratos
-- ============================================================
INSERT INTO prato (codigo, nome, preco, ingredientes, categoria, disponivel) VALUES
-- Entradas
('ENT001', 'Bruschetta al Pomodoro',    18.90, 'Pão ciabatta, tomate, alho, manjericão fresco, azeite extra virgem',                          'ENTRADA', TRUE),
('ENT002', 'Antipasto della Casa',      34.90, 'Presunto cru, salame, mozzarella de búfala, azeitonas, pimentões grelhados',                 'ENTRADA', TRUE),
('ENT003', 'Carpaccio di Manzo',        42.90, 'Carne bovina fatiada finamente, rúcula, parmesão, alcaparras, limão siciliano, azeite',       'ENTRADA', TRUE),
('ENT004', 'Crostini con Funghi',       22.90, 'Pão tostado, mix de cogumelos salteados, alho, tomilho, creme de queijo',                    'ENTRADA', TRUE),

-- Pratos Principais — Pizzas
('PIZ001', 'Pizza Margherita',          48.90, 'Molho de tomate San Marzano, mozzarella fior di latte, manjericão fresco, azeite',           'PRATO_PRINCIPAL', TRUE),
('PIZ002', 'Pizza Quattro Formaggi',    62.90, 'Molho branco, mozzarella, gorgonzola, parmesão, provolone',                                  'PRATO_PRINCIPAL', TRUE),
('PIZ003', 'Pizza Diavola',             58.90, 'Molho de tomate, mozzarella, salame piccante, pimenta calabresa, azeite picante',            'PRATO_PRINCIPAL', TRUE),
('PIZ004', 'Pizza Prosciutto e Funghi', 64.90, 'Molho de tomate, mozzarella, presunto cozido, cogumelos Paris, orégano',                    'PRATO_PRINCIPAL', TRUE),
('PIZ005', 'Pizza Tartufo',             89.90, 'Creme de trufa negra, mozzarella, cogumelos selvagens, rúcula, parmesão ralado',             'PRATO_PRINCIPAL', TRUE),

-- Pratos Principais — Massas
('MAS001', 'Spaghetti alla Carbonara',  52.90, 'Spaghetti, guanciale, ovo caipira, pecorino romano, pimenta-do-reino',                      'PRATO_PRINCIPAL', TRUE),
('MAS002', 'Penne all''Arrabbiata',     44.90, 'Penne rigate, molho de tomate, alho, pimenta-dedo-de-moça, azeite, salsinha',               'PRATO_PRINCIPAL', TRUE),
('MAS003', 'Risotto ai Funghi Porcini', 72.90, 'Arroz arbóreo, cogumelos porcini secos e frescos, manteiga, parmesão, vinho branco seco',   'PRATO_PRINCIPAL', TRUE),
('MAS004', 'Lasagna della Nonna',       58.90, 'Massa fresca, ragù bolognese, bechamel, mozzarella, parmesão',                              'PRATO_PRINCIPAL', TRUE),

-- Sobremesas
('SOB001', 'Tiramisù Classico',         28.90, 'Biscoito savoiardi, mascarpone, ovos, café espresso, cacau em pó, Marsala',                 'SOBREMESA', TRUE),
('SOB002', 'Panna Cotta ai Frutti',     24.90, 'Creme de leite, baunilha, gelatina, calda de frutas vermelhas',                             'SOBREMESA', TRUE),
('SOB003', 'Cannolo Siciliano',         22.90, 'Massa frita crocante, ricota adocicada, pistache, gotas de chocolate',                      'SOBREMESA', TRUE),
('SOB004', 'Gelato Artigianale',        19.90, 'Sorvete artesanal sabores: pistache, baunilha, stracciatella ou limone (escolha 2)',         'SOBREMESA', TRUE),

-- Bebidas
('BEB001', 'Água Mineral San Pellegrino', 12.90, 'Água mineral com ou sem gás (500ml)',                                                     'BEBIDA', TRUE),
('BEB002', 'Vinho Tinto Casa Valduga',    68.90, 'Vinho tinto seco, safra 2021, notas de frutas vermelhas e especiarias (garrafa 750ml)',   'BEBIDA', TRUE),
('BEB003', 'Limonata Italiana',           16.90, 'Suco de limão siciliano, água com gás, hortelã, açúcar de cana',                         'BEBIDA', TRUE),
('BEB004', 'Espresso Illy',               10.90, 'Café espresso extraído de blend especial Illy (dose simples ou dupla)',                   'BEBIDA', TRUE),
('BEB005', 'Suco de Laranja Natural',     14.90, 'Suco de laranja espremido na hora (300ml)',                                               'BEBIDA', TRUE);

-- ============================================================
--  DADOS DE EXEMPLO — Clientes
-- ============================================================
INSERT INTO cliente (nome, telefone, email, total_pedidos) VALUES
('Marco Aurelio Rossi',      '(41) 99801-2233', 'marco.rossi@email.com',     0),
('Giulia Fontana',           '(41) 98712-4455', 'giulia.fontana@email.com',  0),
('Lorenzo Bianchi',          '(41) 99523-6677', 'lorenzo.bianchi@email.com', 0),
('Sofia De Luca',            '(41) 98234-8899', 'sofia.deluca@email.com',    0),
('Alessandro Ferrari',       '(41) 99345-1122', 'ale.ferrari@email.com',     0),
('Valentina Esposito',       '(41) 98456-3344', 'vale.esposito@email.com',   0),
('Giovanni Conti',           '(41) 99567-5566', 'giovanni.conti@email.com',  0),
('Isabella Greco',           '(41) 98678-7788', 'isabella.greco@email.com',  0);

-- ============================================================
--  DADOS DE EXEMPLO — Fidelidade (criada para cada cliente)
-- ============================================================
INSERT INTO fidelidade (cliente_id, pontos_acumulados, tier) VALUES
(1, 1820, 'OURO'),
(2,  740, 'PRATA'),
(3,  310, 'BRONZE'),
(4, 2150, 'OURO'),
(5,  480, 'BRONZE'),
(6,  950, 'PRATA'),
(7,   80, 'BRONZE'),
(8, 1500, 'OURO');

-- ============================================================
--  DADOS DE EXEMPLO — Pedidos e Itens
-- ============================================================
INSERT INTO pedido (cliente_id, data_hora, valor_total, status) VALUES
(1, '2025-05-10 19:30:00', 131.70, 'FINALIZADO'),
(2, '2025-05-12 20:15:00',  77.80, 'FINALIZADO'),
(3, '2025-05-15 21:00:00',  48.90, 'FINALIZADO'),
(4, '2025-05-20 19:45:00', 161.80, 'FINALIZADO'),
(1, '2025-06-01 20:00:00',  62.90, 'FINALIZADO'),
(6, '2025-06-03 19:15:00',  95.80, 'FINALIZADO'),
(2, '2025-06-05 21:30:00', 121.80, 'CANCELADO'),
(5, '2025-06-10 20:45:00',  87.80, 'FINALIZADO');

INSERT INTO item_pedido (pedido_id, prato_id, quantidade, preco_unitario) VALUES
-- Pedido 1: Marco — bruschetta(1) + margherita(5) + tiramisù(14) + espresso(21)
(1,  1, 1, 18.90),
(1,  5, 1, 48.90),
(1, 14, 1, 28.90),
(1, 21, 1, 10.90),
-- Pedido 2: Giulia — crostini(4) + arrabbiata(11) + limonata(20)
(2,  4, 1, 22.90),
(2, 11, 1, 44.90),
(2, 20, 1, 16.90),
-- Pedido 3: Lorenzo — margherita(5)
(3,  5, 1, 48.90),
-- Pedido 4: Sofia — antipasto(2) + carbonara(10) + tiramisù(14) + vinho(19)
(4,  2, 1, 34.90),
(4, 10, 1, 52.90),
(4, 14, 1, 28.90),
(4, 19, 1, 68.90),
-- Pedido 5: Marco — quattro formaggi(6)
(5,  6, 1, 62.90),
-- Pedido 6: Valentina — carpaccio(3) + diavola(7) + espresso(21)
(6,  3, 1, 42.90),
(6,  7, 1, 48.90),
(6, 21, 1, 10.90),
-- Pedido 7: Giulia — cancelado — lasagna(13) + gelato(17)
(7, 13, 1, 58.90),
(7, 17, 1, 19.90),
-- Pedido 8: Alessandro — prosciutto e funghi(8) + panna cotta(15) + suco(22)
(8,  8, 1, 64.90),
(8, 15, 1, 24.90),
(8, 22, 1, 14.90);

-- ============================================================
--  RESGATES DE EXEMPLO
-- ============================================================
INSERT INTO resgate_fidelidade (cliente_id, tipo_resgate, prato_id, desconto_aplicado, pontos_usados, data_resgate) VALUES
(1, 'DESCONTO',     NULL, 10.00, 500, '2025-05-20 19:50:00'),
(4, 'PRATO_GRATIS',    5, NULL,  500, '2025-05-22 20:10:00'),
(8, 'DESCONTO',     NULL, 10.00, 500, '2025-06-08 21:00:00');
