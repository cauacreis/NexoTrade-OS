# NexoTrade OS

NexoTrade OS é um simulador de paper trading (arbitragem) de criptomoedas focado em alta performance. Ele se conecta à stream pública da Binance (em tempo real via WebSocket) e armazena o histórico dos trades de Bitcoin.

## 🚀 Tecnologias

A stack tecnológica do NexoTrade OS foi escolhida para máxima performance, simultaneidade e simplicidade:

- **Java 21**: Aproveitando as mais novas features da linguagem, incluindo as **Virtual Threads**, para garantir que as gravações de banco de dados e os processos paralelos rodem sem travar a thread de leitura do WebSocket.
- **Spring Boot 3**: Fornece toda a fundação web, configuração automática e a facilidade do ecossistema Spring.
- **WebSocket (Spring)**: O radar embutido do sistema que mantém a conexão sempre viva (com auto-reconnect) ouvindo o stream da Binance.
- **Spring Data JPA & Hibernate**: Camada de persistência das transações.
- **H2 Database**: Banco de dados relacional em memória de alta velocidade utilizado como o "Cofre de Persistência".
- **Lombok** & **Jackson**: Para redução de boilerplate e parse de JSON ultra-rápido.

## ⚙️ Funcionalidades

- **O Olho do Radar (WebSocket):** Conecta-se no endpoint `wss://stream.binance.com:9443/ws/btcusdt@trade`, faz o parse do JSON dos trades de BTC/USDT em tempo real e imprime no terminal de maneira super estilizada (ANSI colors).
- **O Cofre de Simulação (Persistência Async):** Utiliza Virtual Threads (ou thread pool paralelo) para gravar os registros na entidade `TradeRecord` assincronamente (através do `@EnableAsync`), sem bloquear o "Radar".
- **Resiliência:** A aplicação detecta a queda de conexão do WebSocket e tenta reconectar em 5 segundos, além de ser tolerante a falhas na leitura dos pacotes JSON (sem NullPointerExceptions).

## 🛠️ Como Rodar a Aplicação

Siga as instruções abaixo para rodar o radar de simulação na sua própria máquina.

### Pré-requisitos
- JDK 21+ instalado na máquina.
- Git (opcional, se quiser clonar o repositório).

### Passos:

1. Clone o projeto e entre no diretório:
   ```bash
   git clone https://github.com/cauacreis/NexoTrade-OS.git
   cd "NexoTrade-OS"
   ```

2. Para rodar diretamente usando o Maven Wrapper (incluído no projeto):
   No Linux/Mac:
   ```bash
   ./mvnw spring-boot:run
   ```
   No Windows (PowerShell/CMD):
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```

3. Você verá os logs do radar pipocando no seu terminal em tempo real!
   ```
   [NEXOTRADE RADAR] BTC/USDT Trade Executado -> $ 65,432.10
   ```

## 🗄️ Acessando o Banco de Dados (H2)

Para visualizar os trades que estão sendo salvos pelo Radar, você pode usar o console web do H2.

1. Com a aplicação rodando, abra o seu navegador e acesse: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
2. Insira as credenciais de acesso padrão:
   - **Driver Class:** `org.h2.Driver`
   - **JDBC URL:** `jdbc:h2:mem:nexotradedb`
   - **User Name:** `sa`
   - **Password:** *(deixe em branco)*
3. Clique em **Connect** e rode `SELECT * FROM trade_records;` para ver o histórico populando.

## 💼 A API de Operações (Paper Trading)

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

*Obs: Você pode utilizar ferramentas visuais como **Postman** ou **Insomnia** para disparar as requisições acima configurando o JSON no body (`raw` > `JSON`).*

## 🧪 Como Rodar os Testes

Os testes garantem que o radar não sofra Memory Leaks ou Exception Leaks caso a Binance envie um JSON sujo, incompleto ou quebrado.

Para executar a suíte de testes unitários (JUnit 5 + Mockito):

No Linux/Mac:
```bash
./mvnw clean test
```
No Windows:
```cmd
.\mvnw.cmd clean test
```
