package io.minijmix.autoconfigure;

import io.minijmix.core.metadata.Metadata;
import io.minijmix.core.metadata.impl.MetadataImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for MiniJmix framework.
 * Inspired by Jmix auto-configuration approach.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.minijmix.core")
public class MiniJmixAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public Metadata metadata() {
        return new MetadataImpl();
    }
}