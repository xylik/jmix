package io.minijmix.core.metadata;

import io.minijmix.core.metadata.impl.MetadataImpl;
import io.minijmix.demo.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the metadata system.
 */
public class MetadataTest {
    
    private Metadata metadata;
    
    @BeforeEach
    void setUp() {
        metadata = new MetadataImpl();
    }
    
    @Test
    void testEntityRegistration() {
        metadata.registerClass(Customer.class);
        
        MetaClass metaClass = metadata.getClass(Customer.class);
        assertNotNull(metaClass);
        assertEquals("Customer", metaClass.getName());
        assertEquals(Customer.class, metaClass.getJavaClass());
    }
    
    @Test
    void testEntityCreation() {
        metadata.registerClass(Customer.class);
        
        Customer customer = metadata.create(Customer.class);
        assertNotNull(customer);
        assertInstanceOf(Customer.class, customer);
    }
    
    @Test
    void testPropertyAccess() {
        metadata.registerClass(Customer.class);
        
        MetaClass metaClass = metadata.getClass(Customer.class);
        MetaProperty nameProperty = metaClass.getProperty("name");
        assertNotNull(nameProperty);
        assertEquals("name", nameProperty.getName());
        assertEquals(String.class, nameProperty.getType());
        assertTrue(nameProperty.isMandatory());
        
        MetaProperty idProperty = metaClass.getProperty("id");
        assertNotNull(idProperty);
        assertTrue(idProperty.isId());
    }
    
    @Test
    void testPropertyValueAccess() {
        metadata.registerClass(Customer.class);
        
        Customer customer = new Customer();
        MetaClass metaClass = metadata.getClass(Customer.class);
        MetaProperty nameProperty = metaClass.getProperty("name");
        
        nameProperty.setValue(customer, "Test Customer");
        assertEquals("Test Customer", nameProperty.getValue(customer));
        assertEquals("Test Customer", customer.getName());
    }
}