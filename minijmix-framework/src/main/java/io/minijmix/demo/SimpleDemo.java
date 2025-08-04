package io.minijmix.demo;

import io.minijmix.core.metadata.Metadata;
import io.minijmix.core.metadata.MetaClass;
import io.minijmix.core.metadata.MetaProperty;
import io.minijmix.core.metadata.impl.MetadataImpl;
import io.minijmix.demo.entity.Customer;

/**
 * Simple standalone demo without Spring Boot dependencies.
 */
public class SimpleDemo {
    
    public static void main(String[] args) {
        System.out.println("\n=== MiniJmix Framework Demo ===");
        
        // Create metadata instance
        Metadata metadata = new MetadataImpl();
        
        // Register the Customer entity
        metadata.registerClass(Customer.class);
        
        // Demonstrate metadata capabilities
        demonstrateMetadata(metadata);
        
        // Demonstrate entity creation
        demonstrateEntityCreation(metadata);
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateMetadata(Metadata metadata) {
        System.out.println("\n--- Metadata System Demo ---");
        
        MetaClass customerMetaClass = metadata.getClass(Customer.class);
        System.out.println("Entity: " + customerMetaClass.getName());
        System.out.println("Java Class: " + customerMetaClass.getJavaClass().getName());
        
        System.out.println("\nProperties:");
        for (MetaProperty property : customerMetaClass.getProperties()) {
            System.out.println("  - " + property.getName() + 
                              " (" + property.getType().getSimpleName() + ")" +
                              (property.isId() ? " [ID]" : "") +
                              (property.isMandatory() ? " [REQUIRED]" : ""));
        }
    }
    
    private static void demonstrateEntityCreation(Metadata metadata) {
        System.out.println("\n--- Entity Creation Demo ---");
        
        // Create entity using metadata
        Customer customer = metadata.create(Customer.class);
        System.out.println("Created customer instance: " + customer);
        
        // Set properties using metadata
        MetaClass metaClass = metadata.getClass(Customer.class);
        MetaProperty nameProperty = metaClass.getProperty("name");
        MetaProperty emailProperty = metaClass.getProperty("email");
        MetaProperty phoneProperty = metaClass.getProperty("phone");
        MetaProperty addressProperty = metaClass.getProperty("address");
        
        nameProperty.setValue(customer, "John Doe");
        emailProperty.setValue(customer, "john.doe@example.com");
        phoneProperty.setValue(customer, "+1-555-123-4567");
        addressProperty.setValue(customer, "123 Main St, Anytown, USA");
        
        System.out.println("Customer after setting properties: " + customer);
        
        // Get properties using metadata
        String retrievedName = (String) nameProperty.getValue(customer);
        String retrievedEmail = (String) emailProperty.getValue(customer);
        System.out.println("Retrieved name via metadata: " + retrievedName);
        System.out.println("Retrieved email via metadata: " + retrievedEmail);
        
        // Demonstrate property introspection
        System.out.println("\n--- Property Introspection ---");
        System.out.println("Name property is ID: " + nameProperty.isId());
        System.out.println("Name property is mandatory: " + nameProperty.isMandatory());
        
        MetaProperty idProperty = metaClass.getProperty("id");
        System.out.println("ID property is ID: " + idProperty.isId());
        System.out.println("ID property type: " + idProperty.getType().getSimpleName());
    }
}