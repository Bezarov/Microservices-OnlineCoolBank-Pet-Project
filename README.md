# 🏦 OnlineCoolBank Microservices Edition

## <center> Welcome! </center>
#### This project is a microservices adaptation and decomposition of my previous monolithic project — [OnlineCoolBank](https://github.com/Bezarov/OnlineCoolBank-Pet-Project) 🚀.

---

## 📚 Project Overview

In this project, each microservice represents a separate component:

- 🧩**ApiGateway-Component**
- 🧩**AppRegistry-Component**
- 🧩**Security-Component**
- 🧩**Users-Component**
- 🧩**Account-Component**
- 🧩**Card-Component**
- 🧩**Payment-Component**
- 🧩**Eureka-Server-Component**

The architecture follows the **Per-Service Database** pattern:  
Each microservice manages its own database **PostgreSQL**.

Communication patterns:

- **Kafka-broker** is used for handling all external (user) requests asynchronously.
- **REST API** (via Feign and Eureka) is used for internal service-to-service communication.
- Each user request has a dedicated Kafka topic, for example:
    - `create-user` topic
    - `create-user-response` topic

Docker is used to launch PostgreSQL and Kafka-broker containers.  
You can find the Docker scripts inside the `/docker` directory in the project root.

---

## 🧩 Microservices Overview

### 🛡️ ApiGateway-Component

- Acts as a **single entry point** into the microservices ecosystem.
- Accepts client requests.
- If authentication is required, extracts the JWT and sends it for verification to the **Security-Component** via **Kafka-broker**.
- Upon successful authentication, routes the request to the appropriate service (**Users-Component**, **Account-Component**, **Card-Component**, **Payment-Component**) via **Kafka-broker**.
- Listens to response topics and delivers responses back to clients.
- Handles errors by listening to special error topics and forwarding error messages and statuses (e.g., from the **Users-Component**) to the client.

### 🗂️ AppRegistry-Component

- **Registers and de-registers** all services and their instances in the internal database.
- Special **REST API** endpoints are provided for registration and deregistration.
- Registration includes **validating:**
    - `componentId`
    - `componentName`
    - `componentSecret`
- Validation occurs against the global config file on AppRegistry-Component:  
  `/resources/global-app-components.config.yml`
- Each microservice deserializes its own config from a local file:  
    `/resources/*-component.config.yml`.

#### ⛓️ Additionally:

- Works as a **Spring Cloud Config Server**.
- Components **configuration properties** are dynamically filling using parameters from files
  **templates** and **global-app-components.config.yml**
- Serves **configuration properties** for components from:  
  `/resources/OnlineCoolBank-config-repo/`.
- Config **templates** are located in:  
  `/resources/default-component-config/`.

### 🔒 Security-Component

- Generates and authenticates **JWT tokens**.
- Works with **external user requests** for token creation and validation via **Kafka-broker**.
- Works with **internal service requests** for token creation and validation via **HTTP/REST API**.
- For **User tokens validation**, it verifies user credentials by calling **Users-Component**.
- For **Component tokens**, it verifies service credentials by calling **AppRegistry-Component**.

### 👤 Users-Component

- Provides basic CRUD operations for the **Users entity**.
- Stores and manages Users data in its own **PostgreSQL** database.
- Receives requests from **ApiGateway-Component** by listening to specific Kafka topics.
- Communicates with other components using **REST API** attaching JWT tokens to each request.
- Validates each request by token validation on **Security-Component**.

### 💰 Account-Component

- Provides basic CRUD operations for the **Account entity**.
- Stores and manages Account data in its own **PostgreSQL** database.
- An **account is linked to a user** and cannot exist without a user.
- Receives requests from **ApiGateway-Component** by listening to specific Kafka topics.
- Communicates with other components using **REST API** attaching JWT tokens to each request.
- Validates each request by token validation on **Security-Component**.

### 💳 Card-Component

- Provides basic CRUD operations for the **Card entity**.
- Stores and manages Card data in its own **PostgreSQL** database.
- An **card is linked to a account** and cannot exist without an account.
- Receives requests from **ApiGateway-Component** by listening to specific Kafka topics.
- Communicates with other components using **REST API** attaching JWT tokens to each request.
- Validates each request by token validation on **Security-Component**.

### 💸 Payment-Component

- Provides basic CRUD operations for the **Payment entity**.
- Stores and manages Payment data in its own **PostgreSQL** database.
- An **payment is linked to a accounts** and cannot exist without an accounts.
- Receives requests from **ApiGateway-Component** by listening to specific Kafka topics.
- Communicates with other components using **REST API** attaching JWT tokens to each request.
- Validates each request by token validation on **Security-Component**.

### 🌍 Eureka-Server-Component
- Acts as a **Service Discovery Server**.
- Allows microservices to **register** themselves and **discover** each other via Eureka.
- Central piece enabling **service-to-service communication** through dynamic service lookup.

---

## 📌 Core Technologies
- **Java 17 version**
- **Spring Boot 3.3.2:**
  - **Web**
  - **Data JPA**
  - **Security**
- **Spring Cloud:** 
  - **Eureka**
  - **Config**
  - **OpenFeign**
  - **LoadBalancer**
  - **Resilience4j**
- **Spring Kafka**
- **Docker**
- **PostgreSQL**
- **JJWT (Java JSON Web Token)**
- **SLF4J**

---

## ⚙️ Automated Setting-up Process
- ### 🔧 Local Deserialization:
  - **Each microservice deserializes its own local configuration file: /resources/*-component.config.yml**
- ### 🔧 Registration with AppRegistry-Component:
  -	**Components register themselves via special REST API endpoints on the AppRegistry-Component.**
- ### 🔧 Dynamic Configuration Enrichment:
  - **AppRegistry-Component enriches the received configuration:**
  - **Using templates from: /resources/default-component-config/**
  - **And global parameters from: /resources/global-app-components.config.yml**
- ### 🔧 Serialization of the Enriched Configuration:
  -	**The final configuration is saved into the repository: /resources/OnlineCoolBank-config-repo/**
- ### 🔧 Serving Configuration via Spring Cloud Config Server:
  - **AppRegistry-Component acts as a Spring Cloud Config Server and provides the finalized configurations to the components.**
- ### 🛡️ Authentication via Security-Component:
  - **After configuration is fetched, each microservice authenticates itself by communicating with the Security-Component.**
  - **Upon successful authentication, a JWT token is issued and used for secured communication between microservices.**


## 🔢 Mini-Scheme of the Process

```
Microservice  ↓ (Local deserialization: `*-component.config.yml`)  
Registration → AppRegistry-Component  ↓ (Enrichment: default templates + global config)  
Serialization → `/resources/OnlineCoolBank-config-repo/` ↓  
Spring Cloud Config Server ↓  
Microservice ↓ (fetching finalized config) 
Authentication → Security-Component (receiving JWT)    
Microservice ↓ (secured communication using JWT)
```

---

## 📂 Repository structure
**🧩 /ApiGateway-Component**  
**🧩 /AppRegistry-Component**  
**🧩 /Security-Component**  
**🧩 /Users-Component**  
**🧩 /Account-Component**  
**🧩 /Card-Component**  
**🧩 /Payment-Component**  
**🧩 /Eureka-Server-Component**  
**🐳 docker**  
**📓 README**  

---


## 🐳 Docker Usage

The project uses Docker to manage essential infrastructure:

- **PostgreSQL** (Database)
- **Kafka-Broker** (Message Broker)

Docker scripts and configurations are located at:  
`docker/`

---

## 🎯 TODO List for Future Improvements

📌 Improve microservice-to-microservice communication and integration with Eureka.  
📌 Add support for gRPC communication between microservices.  
📌 Enable monitoring using Grafana + metrics (running inside Docker).  
📌 Implement logging aggregation in Grafana using Loki.  
📌 Add request tracing using Spring Cloud Sleuth and Zipkin.  
📌 Containerize each microservice with Docker for deployment in Spring Cloud Kubernetes.

---

## 🚀 Quick Start

```
# Clone the repository
git clone https://github.com/YourGithubUsername/OnlineCoolBank-Microservices.git

# Navigate into the project
cd OnlineCoolBank-Microservices

# Start Docker containers
docker-compose up -d

# Launch options
Option 1: Run each service directly from your IDE (e.g., IntelliJ IDEA → Run Application class).
Option 2: Use Gradle/Maven: 
      ./gradlew bootRun --args='--spring.profiles.active=dev' 
      or
      mvn spring-boot:run -Dspring-boot.run.profiles=dev
Option 3: Build and run JAR manually: 
      ./gradlew build
      java -jar build/libs/your-service-name.jar --spring.profiles.active=dev
```

---