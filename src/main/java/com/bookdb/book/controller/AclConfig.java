package com.bookdb.book.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class AclConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public MutableAclService aclService() {
        JdbcMutableAclService aclService = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());

        // Настройки для PostgreSQL
        aclService.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
        aclService.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");

        return aclService;
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }


    @Bean
    public SpringCacheBasedAclCache aclCache() {
        final ConcurrentMapCache aclCache = new ConcurrentMapCache("acl_cache");
        return new SpringCacheBasedAclCache(aclCache, permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(
                dataSource,
                aclCache(),
                aclAuthorizationStrategy(),
                new ConsoleAuditLogger()
        );
    }

    @Bean
    public AuditLogger auditLogger() {
        return new ConsoleAuditLogger();
    }

    @Bean
    public PermissionGrantingStrategy grantingStrategy() {
        return new DefaultPermissionGrantingStrategy(auditLogger());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextHolderStrategy securityContextHolderStrategy() {
        return SecurityContextHolder.getContextHolderStrategy();
    }
}

