# MiniJmix Framework - Proof of Concept

This is a reduced framework that demonstrates the core technical ideas extracted from the Jmix enterprise framework.

## Technical Ideas Extracted from Jmix

### 1. Metadata-Driven Entity System
The foundation of Jmix is its metadata system that provides introspection and manipulation of entities at runtime.

**Key Concepts:**
- `Metadata` - Central interface for accessing entity metadata
- `MetaClass` - Metadata about an entity class (properties, methods, annotations)
- `MetaProperty` - Metadata about entity properties (type, constraints, relationships)
- Dynamic entity creation and property access through reflection

**Benefits:**
- Framework can work generically with any entity type
- UI components can be auto-generated based on metadata
- Validation, security, and business logic can be applied uniformly

### 2. Data Access Layer Abstraction
Jmix provides a high-level data access API that abstracts away the underlying persistence technology.

**Key Concepts:**
- `DataManager` - High-level interface for CRUD operations with security
- `UnconstrainedDataManager` - Low-level data access without security checks
- `LoadContext` - Defines what and how to load from the database
- `FetchPlan` - Defines object graph loading strategies (like JPA Entity Graphs)
- Multiple data store support

**Benefits:**
- Clean separation between business logic and persistence
- Built-in security integration
- Optimized loading strategies
- Support for multiple databases

### 3. Security and Access Control
Comprehensive security system that integrates at multiple levels.

**Key Concepts:**
- Role-based access control (RBAC)
- Resource permissions (entity operations)
- Property-level access control
- Row-level security constraints
- Integration with Spring Security

**Benefits:**
- Fine-grained access control
- Declarative security configuration
- Automatic enforcement across UI and API layers

### 4. Component-Based UI Framework
UI system that automatically generates interfaces based on entity metadata.

**Key Concepts:**
- Metadata-driven UI generation
- Component factories and builders
- Data binding between UI and entities
- Automatic CRUD operations
- View navigation and lifecycle management

**Benefits:**
- Rapid application development
- Consistent UI patterns
- Automatic data binding and validation

### 5. Modular Architecture
Spring Boot-based modular system with auto-configuration.

**Key Concepts:**
- Module system with dependencies
- Auto-configuration classes
- Starter projects for easy integration
- Plugin architecture for extensions

**Benefits:**
- Easy to extend and customize
- Modular deployment
- Consistent configuration patterns

## MiniJmix Implementation

This proof-of-concept implements simplified versions of these concepts:

### Core Packages

- `io.minijmix.core.metadata` - Metadata system implementation
- `io.minijmix.core.data` - Data access layer
- `io.minijmix.core.security` - Security interfaces
- `io.minijmix.core.ui` - UI component interfaces
- `io.minijmix.autoconfigure` - Spring Boot auto-configuration

### Demo Application

The demo application (`io.minijmix.demo`) showcases:

1. **Entity Registration** - How entities are registered with the metadata system
2. **Metadata Introspection** - How to explore entity structure at runtime
3. **Dynamic Entity Creation** - Creating entities through metadata
4. **Property Access** - Setting and getting property values through metadata

### Key Features Demonstrated

1. **Reflection-based Metadata System**
   - Automatic scanning of entity fields
   - Type-safe property access
   - Annotation-based configuration

2. **Spring Boot Integration**
   - Auto-configuration
   - Component scanning
   - Dependency injection

3. **JPA Integration**
   - Entity mapping
   - Database configuration
   - Annotation support

## Running the Demo

```bash
cd minijmix-framework
./gradlew bootRun
```

The demo will output examples of:
- Entity metadata inspection
- Dynamic entity creation
- Property value manipulation

## Key Learnings from Jmix

1. **Metadata is King** - Having rich metadata about entities enables powerful framework features
2. **Abstraction Layers** - Multiple levels of abstraction (DataManager vs UnconstrainedDataManager) provide flexibility
3. **Security Integration** - Security should be built into the core, not added as an afterthought
4. **Convention over Configuration** - Smart defaults based on annotations and naming conventions
5. **Modular Design** - Proper module boundaries enable extensibility and customization

## Potential Extensions

This proof-of-concept could be extended with:

- Full data access implementation with JPA
- Complete security system with Spring Security
- Web-based UI components
- Validation framework
- Audit capabilities
- Multi-tenancy support
- REST API generation
- Search and filtering

## Architecture Comparison

| Aspect | Full Jmix | MiniJmix PoC |
|--------|-----------|-------------|
| Metadata System | ✅ Full featured | ✅ Basic implementation |
| Data Access | ✅ Multiple stores | ⚠️ Interface only |
| Security | ✅ Comprehensive RBAC | ⚠️ Interface only |
| UI Framework | ✅ Vaadin-based | ⚠️ Interface only |
| Modules | ✅ 30+ modules | ✅ Basic structure |
| Auto-config | ✅ Spring Boot | ✅ Basic implementation |

This proof-of-concept successfully demonstrates how the core technical ideas from Jmix can be implemented in a reduced framework, providing a foundation for understanding enterprise application development patterns.