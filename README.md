
# 💳 E-Wallet Microservices Project

A scalable **E-Wallet application** built with **Spring Boot Microservices**, **Kafka**, **Redis**, and **Eureka**.  
The system provides core wallet functionalities (account creation, wallet top-up, internal transfers, transactions, etc.) and is being extended with advanced features like external payments, rewards, notifications, and dashboards.

---

## 🚀 Current Features (Implemented)
✅ **User Service**  
- User registration & authentication (JWT-based login).  
- Role-based access (USER/ADMIN).  

✅ **Wallet Service**  
- Create wallet accounts for users.  
- Fetch wallet balance.  
- Redis caching for performance.  

✅ **Transaction Service**  
- Internal money transfer between wallets.  
- Transaction persistence with proper entity modeling.  
- Kafka integration for asynchronous transaction processing.  

✅ **Payment Service (Basic)**  
- Handles top-ups into wallet.  
- Interfaces with transaction-service to record transactions.  

✅ **Eureka Service Discovery**  
- All microservices are registered with **Eureka Server**.  
- Enables dynamic service discovery and communication.  

✅ **API Gateway (Spring Cloud Gateway)**  
- Single entry point for all APIs.  
- CORS configured for frontend (React app on port `5173`).  

---

## 📌 Upcoming Features (In Progress / Planned)
🛠️ **Payment Gateway Service**  
- Integration with real external payment providers (e.g., Razorpay/Stripe/PayPal).  
- Support for external top-up and withdrawal.  

📊 **Dashboard Service**  
- User-friendly dashboard to view balances, analytics, and transaction summaries.  

📜 **Transaction History Service**  
- Dedicated service for fetching and managing transaction logs.  
- Filter by date, type, and amount.  

🎁 **Reward Service**  
- Reward points for transactions.  
- Redemption options for offers and discounts.  

🔔 **Notification Service**  
- Kafka-driven event notifications.  
- Email/SMS/Push notification support.  

---

## 🏗️ Tech Stack
- **Backend:** Spring Boot (Java 21), Spring Data JPA, Spring Security, Hibernate.  
- **Service Discovery:** Netflix Eureka.  
- **API Gateway:** Spring Cloud Gateway.  
- **Event Streaming:** Apache Kafka.  
- **Caching:** Redis.  
- **Database:** MySQL.  
- **Frontend:** React (planned, currently in development).  
- **Containerization:** Docker (multi-service setup).  

---

## 📂 Project Structure
```

├── eureka-server
├── api-gateway
├── user-service
├── wallet-service
├── transaction-service
├── payment-service
├── reward-service        (planned)
├── notification-service  (planned)
├── dashboard-service     (planned)
└── history-service       (planned)

````

---

## ⚡ Running the Project

### Prerequisites
- JDK 21+
- Docker (for MySQL, Redis, Kafka, Zookeeper)
- Maven

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/MrPal28/e-wallet.git
   
````

2. Start required infra with Docker: (Still not dockerized)

   ```bash
   docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=ewallet -p 3306:3306 -d mysql:8
   docker run --name redis -p 6379:6379 -d redis
   docker run -d --name zookeeper -p 2181:2181 zookeeper:3.9
   docker run -d --name kafka -p 9092:9092 --link zookeeper wurstmeister/kafka
   ```

3. Start **Eureka Server**:

   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```

4. Start **API Gateway**:

   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

5. Start other services (user-service, wallet-service, transaction-service, payment-service):

   ```bash
   mvn spring-boot:run
   ```

6. Access Swagger UI for APIs: #Not Added till now (In future it will be added)

   ```
   http://localhost:8080/swagger-ui.html   # centralized via API Gateway 
   ```

---

## 🔮 Future Roadmap

* [ ] Integrate **external payment gateways**.
* [ ] Build **React-based frontend** (user dashboard & admin panel).
* [ ] Add **real-time notifications** with Kafka + WebSockets.
* [ ] Implement **reward points system**.
* [ ] Deploy on Kubernetes with CI/CD pipelines.

---

## 🧑‍💻 Author

👤 **Arindam Pal**
*Learning microservices, Kafka, Redis, and Docker by building real-world scalable projects.*

---

⚠️ **Note:** This project is still in development. Some features (dashboard, external payments, rewards, history, notifications) are not yet implemented but are planned in upcoming releases.
