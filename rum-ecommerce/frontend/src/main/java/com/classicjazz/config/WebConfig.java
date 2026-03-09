package com.classicjazz.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    // Cart is stored in session via CartSession.getOrCreate(session), not as a scoped bean.
}
