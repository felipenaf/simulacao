# Simulation Service

## Overview
This API manages customer records and credit/financial simulations. It provides full CRUD operations for customer profiles (including address details and custom CPF validation) and calculates loan/financing simulations based on customer data. The application also supports exporting simulation results in multiple formats (CSV, TXT) via the Factory Design Pattern.

## Architecture
- **Controller**: Exposes RESTful endpoints via `CustomerController` and `SimulationController`.
- **Service**: Encapsulates business logic in `CustomerServiceImpl` and `SimulationServiceImpl`, with custom exporter logic (`SimulationExporterFactory`, `SimulationExporterCsv`, `SimulationExporterTxt`).
- **Repository**: Handles data persistence using Spring Data JPA (`CustomerRepository`, `SimulationRepository`).
- **Entity**: Represents the application's domain model, including `Customer`, `Address`, `Simulation`, and `State`.
- **DTO**: Data transfer objects for API payload validation and response mapping (`CustomerRequest`, `CustomerPatchRequest`, `CustomerResponse`, `SimulationRequest`, `SimulationResponse`, etc.).
- **Validation & Exception Handling**: Includes custom Brazilian CPF validation (`@ValidCPF`) and a centralized `@ControllerAdvice` global exception handler (`GlobalExceptionHandler`).

## Tech Stack
Java 21, Spring Boot 3.x, Spring Data JPA, Spring Validation, PostgreSQL, Lombok, Jackson (CSV)

## Getting Started

### Prerequisites
- Java 21 / JDK 21
- Maven
- Docker & Docker Compose

### Environment Setup

1. Start the PostgreSQL database container:
```bash
docker compose up -d
```

2. Run the application:
```bash
./mvnw spring-boot:run
```
*(Or `mvn spring-boot:run`)*

The application will start on `http://localhost:8080` and automatically connect to PostgreSQL on port `5433`.

## Testing
```
./mvnw clean test
```
*(Or `mvn clean test`)*

## Design Decisions
- **Factory Pattern for Exporters**: Applied `SimulationExporterFactory` paired with format-specific exporters (`SimulationExporterCsv`, `SimulationExporterTxt`) to allow clean and extensible export formatting (e.g., CSV, TXT) adhering to the Open/Closed Principle.
- **Layered Architecture & DTO Pattern**: Segregated API request/response objects from domain entities to maintain clean domain boundaries and prevent mass-assignment risks.
