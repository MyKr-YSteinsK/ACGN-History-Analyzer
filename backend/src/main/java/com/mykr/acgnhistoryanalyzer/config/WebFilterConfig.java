package com.mykr.acgnhistoryanalyzer.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration() {
        FilterRegistrationBean<RequestIdFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestIdFilter());
        bean.addUrlPatterns("/api/*");
        bean.setOrder(0);
        return bean;
    }
}
