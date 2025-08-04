# Jmix POC Framework

A reduced proof-of-concept framework demonstrating key technical ideas extracted from the Jmix enterprise framework.

## Key Technical Ideas Implemented

### 1. Entity-Driven Development
- **Entity Interface**: Core abstraction for domain objects with identifier support
- **EntityEntry System**: Tracks entity state and provides metadata access
- **Metadata-Driven Creation**: Create entities dynamically from metadata

### 2. Data Management Layer
- **DataManager**: Secure CRUD operations with authorization
- **UnconstrainedDataManager**: Raw data access without security constraints
- **Fluent API**: Chainable methods for intuitive data loading
- **Multi-Store Support**: Abstract data store concept for different backends

### 3. Security by Design
- **Access Constraints**: Pluggable security constraints system
- **Constraint Registry**: Central registry for security rules
- **Constrained vs Unconstrained**: Clear separation of secure and raw access

### 4. Fetch Plans
- **Controlled Loading**: Define what data to load and graph depth
- **Performance Optimization**: Avoid N+1 queries through explicit planning
- **Flexible Graph Loading**: Support for complex object relationships

### 5. Modular Architecture
- **Separation of Concerns**: Clear module boundaries
- **Auto-Configuration**: Spring Boot-style configuration
- **Extensible Design**: Plugin-ready architecture

## Architecture Overview

```
jmix-poc/
├── core/           # Entity, Metadata, Core interfaces
├── data/           # DataManager, Stores, CRUD operations  
├── security/       # Access constraints, Authorization
└── example/        # Usage examples and demos
```

## Usage Examples

```java
// Create and save entity
Customer customer = dataManager.create(Customer.class);
customer.setName("John Doe");
dataManager.save(customer);

// Fluent loading API
List<Customer> customers = dataManager.load(Customer.class)
    .query("select c from Customer c where c.name = :name")
    .parameter("name", "John")
    .fetchPlan("customer-with-orders")
    .list();

// Security-aware operations
Customer customer = dataManager.load(Customer.class)
    .id(customerId)
    .one(); // Applies access constraints

// Raw access (bypasses security)
Customer customer = dataManager.unconstrained()
    .load(Customer.class)
    .id(customerId)
    .one();
```

## Technical Concepts Demonstrated

1. **Domain-Driven Design**: Entities as first-class citizens
2. **Fluent Interfaces**: Intuitive API design
3. **Aspect-Oriented Security**: Cross-cutting security concerns
4. **Metadata-Driven Development**: Reflection and annotation-based configuration
5. **Layered Architecture**: Clear separation between data, security, and business logic
6. **Dependency Injection**: Spring-based configuration and wiring