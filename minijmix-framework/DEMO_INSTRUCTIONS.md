# How to Run MiniJmix Demo

The MiniJmix framework demonstrates the core technical ideas extracted from the Jmix enterprise framework.

## Prerequisites

- Java 17 or higher
- Gradle (included via wrapper)

## Running the Demo

1. **Navigate to the framework directory**:
   ```bash
   cd minijmix-framework
   ```

2. **Run the simple demo**:
   ```bash
   ./gradlew runSimpleDemo
   ```

3. **Run the tests**:
   ```bash
   ./gradlew test
   ```

4. **Build the framework**:
   ```bash
   ./gradlew build
   ```

## Demo Output

The demo will show:

1. **Entity Metadata Discovery**: How the framework automatically discovers entity structure
2. **Dynamic Entity Creation**: Creating entities through the metadata system
3. **Property Access**: Setting and getting property values via reflection
4. **Constraint Discovery**: Identifying ID fields, required fields, etc.

Expected output:
```
=== MiniJmix Framework Demo ===

--- Metadata System Demo ---
Entity: Customer
Java Class: io.minijmix.demo.entity.Customer

Properties:
  - id (Long) [ID]
  - name (String) [REQUIRED]
  - email (String)
  - phone (String)
  - address (String)

--- Entity Creation Demo ---
Created customer instance: Customer{id=null, name='null', email='null'}
Customer after setting properties: Customer{id=null, name='John Doe', email='john.doe@example.com'}
Retrieved name via metadata: John Doe
Retrieved email via metadata: john.doe@example.com

--- Property Introspection ---
Name property is ID: false
Name property is mandatory: true
ID property is ID: true
ID property type: Long

=== Demo Complete ===
```

## Framework Architecture

The MiniJmix framework consists of:

- **Core Metadata System**: Runtime entity introspection
- **Data Access Layer**: High-level data operations interfaces
- **Security Framework**: Role-based access control interfaces
- **UI Framework**: Component generation interfaces
- **Auto-configuration**: Spring Boot integration

## Technical Concepts Demonstrated

1. **Metadata-Driven Development**: Framework behavior driven by entity metadata
2. **Reflection-Based Property Access**: Type-safe property manipulation
3. **Annotation-Based Configuration**: Using JPA annotations for metadata
4. **Modular Architecture**: Clean separation of concerns
5. **Spring Boot Integration**: Auto-configuration and dependency injection

For a complete analysis of the technical ideas, see `TECHNICAL_ANALYSIS.md` in the root directory.