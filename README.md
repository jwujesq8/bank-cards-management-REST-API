# Bank Cards Management API

## Project Description

This is a Spring Boot-based REST API for managing bank cards. It features JWT authentication, role-based access control, and integrates with a PostgreSQL database. The application allows admins and users to manage cards, cards transactions, and authenticate via JWT tokens.

## Technologies Used

- **Java** (Spring Boot)
- **PostgreSQL** (Database)
- **Docker** (Containerization)
- **JWT** (Authentication)
- **Swagger/OpenAPI** (API Documentation)
- **Liquibase** (Database tracking)

## Prerequisites

- Docker and Docker Compose installed
- Java 21 installed
- Maven or Gradle for building the project

## Setup Instructions

1. Clone the repository (or download from link - https://drive.google.com/drive/folders/1OXVIgGmN0hsislr-YbaAlwqFU31ovBdM?usp=sharing):
    ```bash
    git clone https://github.com/jwujesq8/bank-cards-management-REST-API.git
    cd <project_directory>
    ```

2. Set up Docker:
    - Ensure Docker and Docker Compose are installed on your system.

3. Build the project:
    ```bash
    ./mvnw clean install
    ```

4. Start the application using Docker Compose:
    ```bash
    docker-compose up --build
    ```

   This will set up the API container and PostgreSQL container as defined in the `docker-compose.yml`.

5. Visit the API at:
    - [http://localhost:8080](http://localhost:8080)

## Configuration

### Application Properties (`application.properties`)

- **PostgreSQL Configuration**
    - `spring.datasource.url=jdbc:postgresql://localhost:5432/rest-api`
    - `spring.datasource.username=postgres`
    - `spring.datasource.password=12345`

- **JWT Configuration**
    - `jwt.access.path=api/src/main/resources/jwt/access.txt`
    - `jwt.refresh.path=api/src/main/resources/jwt/refresh.txt`

- **Encryption Util Configuration**
    - `secret.key.path=src/main/resources/encryption-util/secret-key.txt`

- **Swagger Configuration**
    - `springdoc.swagger-ui.url=/v1/bank-cards-management-api-docs`
    - `springdoc.api-docs.path=/v1/bank-cards-management-api-docs`

### Docker Compose Configuration (`docker-compose.yml`)

The `docker-compose.yml` file contains the services for the API and PostgreSQL. The API container is built from the Dockerfile and connects to a PostgreSQL database. The `depends_on` ensures that the database container is started before the API.

## Endpoints

### Auth Endpoints

- **POST** `/auth/login`: Log in and receive a JWT access token.
- **POST** `/auth/newAccessToken`: Get a new access token using a refresh token.
- **POST** `/auth/refreshToken`: Get a new access token and refresh token.
- **DELETE** `/auth/logout`: Log out by invalidating the refresh token.

### User Endpoints

- **POST** `/user`: Get user by ID (Admin only).
- **POST** `/user/new`: Create a new user (Admin only).
- **PUT** `/user`: Update an existing user (Admin only).
- **DELETE** `/user`: Delete a user by ID (Admin only).
- **GET** `/user/all`: Get all users with pagination (Admin only).

### Card Endpoints

- **POST** `/card`: Get a card by ID (Admin or Card Owner).
- **POST** `/card/new`: Add a new card (Admin only).
- **PUT** `/card`: Update an existing card (Admin only).
- **PUT** `/card/status`: Update the status of a card (Admin only). 
- **PUT** `/card/transactionLimitPerDay`: Update transaction limit per day of a card (Admin only). 
- **DELETE** `/card`: Delete a card by ID (Admin only).
- **GET** `/card/all`: Get all cards with pagination (Admin only).
- **GET** `/card/all/owner`: Get all cards by owner ID with pagination (Admin or Card Owner).

### Transaction Endpoints

- **POST** `/transaction`: Get transaction by ID (Admin and source or destination card owner only). 
- **POST** `/transaction/new`: Create a new transaction (Admin only).
- **PUT** `/transaction`: Update an existing transaction (Admin only).
- **DELETE** `/transaction`: Delete a transaction by ID (Admin only).
- **POST** `/transaction/make`: Make a transaction between cards (Source card owner only).
- **GET** `/transaction/all`: Get all transactions with pagination (Admin only).
- **POST** `/transaction/all/card`: Get all transactions by card ID with pagination (Admin and card owner only).

## Swagger

- http://localhost:8080/swagger-ui/index.html

## JavaDoc

- <project_directory>\target\apidocs\index.html

## Running the Application

To run the application, simply use the Docker Compose command:

```bash
docker-compose up --build
