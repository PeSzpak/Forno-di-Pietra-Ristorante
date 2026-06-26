# 🍕 Forno di Pietra — Sistema de Gerenciamento de Restaurante

> Sistema desktop em Java para gerenciamento completo de um restaurante italiano, incluindo CRUD de pratos e clientes, registro de pedidos, geração de faturas e o programa de fidelidade **Carta d'Oro**.

---

## 🏛️ Sobre a empresa

**Forno di Pietra Ristorante** é um restaurante italiano fictício especializado em pizzas artesanais assadas em forno a lenha de pedra. Fundado com o propósito de trazer a autenticidade da culinária italiana ao Brasil, o restaurante combina tradição e tecnologia para oferecer a melhor experiência aos seus clientes.

---

## 🛠️ Tecnologias utilizadas

| Tecnologia        | Versão  | Função                          |
|-------------------|---------|---------------------------------|
| Java              | 17      | Linguagem principal             |
| Java Swing        | —       | Interface gráfica desktop       |
| MySQL             | 8.0     | Banco de dados relacional       |
| Docker            | —       | Containerização do banco        |
| Maven             | 3.9+    | Gerenciamento de dependências   |
| phpMyAdmin        | 5.2     | Administração visual do banco   |

---

## 🚀 Como executar

### Pré-requisitos

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### 1. Clone o repositório

```bash
git clone https://github.com/SEU_USUARIO/forno-di-pietra.git
cd forno-di-pietra
```

### 2. Suba o banco de dados com Docker

```bash
docker-compose up -d
```

Aguarde ~15 segundos para o MySQL inicializar completamente.  
O banco é criado e populado automaticamente via `sql/init.sql`.

> **phpMyAdmin** disponível em: [http://localhost:8080](http://localhost:8080)  
> Usuário: `forno` | Senha: `forno123`

### 3. Compile e execute

```bash
mvn clean package -q
java -jar target/forno-di-pietra.jar
```

Ou diretamente pelo IntelliJ IDEA: execute a classe `Main.java`.

### 4. Encerrar o banco

```bash
docker-compose down
```

Para remover os dados também:

```bash
docker-compose down -v
```

---

## 🗂️ Estrutura do projeto

```
forno-di-pietra/
├── docker-compose.yml          # MySQL 8 + phpMyAdmin
├── sql/
│   └── init.sql                # Schema + dados de exemplo
├── src/
│   └── main/
│       ├── java/com/fornodipietra/
│       │   ├── Main.java               # Ponto de entrada
│       │   ├── config/
│       │   │   └── DatabaseConnection.java  # Singleton de conexão
│       │   ├── model/                  # Entidades do domínio
│       │   ├── dao/                    # Acesso ao banco (SQL puro)
│       │   ├── service/                # Regras de negócio
│       │   ├── ui/                     # Telas Swing
│       │   └── util/                   # Validadores e utilitários
│       └── resources/
│           └── db.properties           # Configurações de conexão
├── pom.xml
└── README.md
```

---

## 🥇 Programa de Fidelidade — Carta d'Oro

| Tier      | Pontos         | Benefício                                      |
|-----------|----------------|------------------------------------------------|
| 🥉 Bronze | 0 – 499 pts    | Acumula pontos (R$1 = 1 ponto)                 |
| 🥈 Prata  | 500 – 1499 pts | Resgate: 5% de desconto no próximo pedido      |
| 🥇 Ouro   | 1500+ pts      | Resgate: 10% de desconto **ou** prato grátis   |

---

## 📊 Funcionalidades

- ✅ CRUD completo de Pratos (com categorias e disponibilidade)
- ✅ CRUD completo de Clientes
- ✅ Registro de pedidos com múltiplos pratos
- ✅ Geração de fatura detalhada por pedido
- ✅ Controle de disponibilidade dos pratos
- ✅ Programa de fidelidade com tiers Bronze / Prata / Ouro
- ✅ Resgate de desconto ou prato grátis
- ✅ 8 relatórios gerenciais com filtros por período
- ✅ Validações completas (dados, conflitos, integridade referencial)

---

## 👥 Equipe

> Projeto desenvolvido para a disciplina de **Análise e Desenvolvimento de Sistemas**  
> Faculdade de Tecnologia Senac PR — 3º Período

---

## 📄 Licença

Projeto acadêmico — uso educacional.
