package io.minijmix.demo;

import io.minijmix.core.metadata.Metadata;
import io.minijmix.core.metadata.MetaClass;
import io.minijmix.core.metadata.MetaProperty;
import io.minijmix.demo.entity.Customer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Demo application showcasing MiniJmix framework capabilities.
 */
@SpringBootApplication
public class MiniJmixDemoApplication implements CommandLineRunner {
    
    @Autowired
    private Metadata metadata;
    
    public static void main(String[] args) {
        SpringApplication.run(MiniJmixDemoApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== MiniJmix Framework Demo ===");
        
        // Register the Customer entity
        metadata.registerClass(Customer.class);
        
        // Demonstrate metadata capabilities
        demonstrateMetadata();
        
        // Demonstrate entity creation
        demonstrateEntityCreation();
    }
    
    private void demonstrateMetadata() {
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
    
    private void demonstrateEntityCreation() {
        System.out.println("\n--- Entity Creation Demo ---");
        
        // Create entity using metadata
        Customer customer = metadata.create(Customer.class);
        System.out.println("Created customer instance: " + customer);
        
        // Set properties using metadata
        MetaClass metaClass = metadata.getClass(Customer.class);
        MetaProperty nameProperty = metaClass.getProperty("name");
        MetaProperty emailProperty = metaClass.getProperty("email");
        
        nameProperty.setValue(customer, "John Doe");
        emailProperty.setValue(customer, "john.doe@example.com");
        
        System.out.println("Customer after setting properties: " + customer);
        
        // Get properties using metadata
        String retrievedName = (String) nameProperty.getValue(customer);
        System.out.println("Retrieved name via metadata: " + retrievedName);
    }
}