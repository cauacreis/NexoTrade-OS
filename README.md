# NexoTrade OS

Esse é um projeto de arquitetura de alta performance utilizando **Java 21**, **Spring Boot 3**, e **Virtual Threads**, acoplado a um front-end brutalista em **React + Vite**. O objetivo é simular transações financeiras na velocidade da luz consumindo o stream de cotações em tempo real da Binance via WebSocket.

## 🚀 Arquitetura do Sistema

1. **O Radar (WebSocket):** Um canal de escuta contínua na porta WSS da Binance, decodificando dezenas de mensagens JSON por segundo.
2. **O Cofre de Persistência:** Sistema de gravação das cotações em tempo real no banco de dados, offloading o peso para threads virtuais isoladas.
3. **O Motor Financeiro (Paper Trading):** Transações em banco de dados H2 (in-memory) rodando de forma assíncrona para não engargalar o Radar.
4. **O Bot de Arbitragem (Média Móvel):** Robô autônomo acoplado à engine, tomando decisões de compra/venda baseadas na volatilidade instantânea da Média Móvel de 50 períodos (SMA).
5. **A API REST e Web Terminal:** O motor financeiro está totalmente exposto via JSON, controlado por um Dashboard React com estética Neo-Brutalista.

## 🛠️ Tecnologias Utilizadas
- **Backend:** Java 21, Spring Boot 3, Maven, Spring WebSockets, Spring Data JPA, H2 Database.
- **Frontend:** React, TypeScript, Vite, TailwindCSS (Neo-Brutalism).
- **Design Patterns:** Event-driven architecture, Injeção de Dependências, Asynchronous Processing, Strategy.

## 🗄️ Acessando o Banco de Dados (H2)

Para visualizar os trades que estão sendo salvos pelo Radar, você pode usar o console web do H2.

1. Com a aplicação rodando, abra o seu navegador e acesse: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
2. Insira as credenciais de acesso padrão:
   - **Driver Class:** `org.h2.Driver`
   - **JDBC URL:** `jdbc:h2:mem:nexotradedb`
   - **User Name:** `sa`
   - **Password:** *(deixe em branco)*
3. Clique em **Connect** e rode `SELECT * FROM trade_records;` para ver o histórico populando.

## 🤖 O Motor de Análise (Bot de Arbitragem)

O NexoTrade OS agora conta com um Bot de Auto-Trading acoplado diretamente à stream de preços!
Ele utiliza a estratégia de **Média Móvel Simples (SMA - Simple Moving Average)** dos últimos 50 trades do Bitcoin para identificar oportunidades matemáticas no mercado.

**Regras de Execução Autônoma:**
- Se o preço do BTC **cair 2%** abaixo da Média Móvel (Sinal de Queda/Oportunidade), o Bot dispara uma **COMPRA** automática usando 10% do seu saldo de Dólar disponível.
- Se o preço do BTC **subir 2%** acima da Média Móvel (Sinal de Alta/Lucro), o Bot dispara uma **VENDA** automática liquidando todo o seu saldo de BTC para realizar lucro imediato.

*(Para ativar o robô, utilize a API REST de Controle descrita abaixo).*

## 💼 A API de Operações (REST)

O NexoTrade OS disponibiliza endpoints REST para você executar simulações de compra e venda usando o preço do Bitcoin em tempo real!

A aplicação já inicializa uma **Carteira Virtual** com `$ 10.000,00` (Dez mil dólares) prontos para operar.

### 1. Checar o Saldo da Carteira (GET)
Retorna o saldo de USD e BTC disponíveis.

**cURL:**
```bash
curl -X GET http://localhost:8080/api/wallet
```

### 2. Comprar Bitcoin (POST)
Deduz o valor em Dólar (`amountUsd`) da carteira e adiciona a fração de Bitcoin baseada no preço exato do radar no instante da execução.

**cURL:**
```bash
curl -X POST http://localhost:8080/api/trade/execute \
     -H "Content-Type: application/json" \
     -d "{\"action\": \"BUY\", \"amountUsd\": 1500.00}"
```

### 3. Vender Bitcoin (POST)
Vende a fração de Bitcoin (`amountBtc`), adicionando o Dólar correspondente à carteira.

**cURL:**
```bash
curl -X POST http://localhost:8080/api/trade/execute \
     -H "Content-Type: application/json" \
     -d "{\"action\": \"SELL\", \"amountBtc\": 0.05}"
```

### 4. Ligar/Desligar o Bot de Média Móvel (POST)
Ativa ou desativa o Auto-Trading em tempo real. O Bot inicia desligado por padrão de segurança.

**cURL:**
```bash
curl -X POST http://localhost:8080/api/bot/toggle
```

## 🖥️ O Terminal Web (Frontend React)

O NexoTrade OS agora possui um painel de controle Neo-Brutalista!
Através dele você acompanha o saldo em tempo real, controla o Bot Autônomo e pode disparar ordens de override manuais (Comprar/Vender).

### Como Rodar o Dashboard:
1. Abra um terminal separado e acesse a pasta do frontend:
```bash
cd nexo-trade-web
```
2. Instale as dependências (se for a primeira vez):
```bash
npm install
```
3. Inicie o servidor Vite:
```bash
npm run dev
```
4. Acesse **http://localhost:5173** no seu navegador!

*(Certifique-se de que o backend Java esteja rodando na porta `8080` ao mesmo tempo).*

## 🧪 Como Rodar os Testes

Os testes garantem que o radar não sofra Memory Leaks ou Exception Leaks caso a Binance envie um JSON sujo, incompleto ou quebrado.

```bash
./mvnw clean test
```
