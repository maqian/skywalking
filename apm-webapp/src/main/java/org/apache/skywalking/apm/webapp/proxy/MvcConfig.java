/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.webapp.proxy;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Additional MVC Configuration.
 *
 * @author gaohongtao
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    private static final String PUBLIC_PATH = "/skywalking";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler(PUBLIC_PATH + "/index.html")
                .addResourceLocations("classpath:/public/index.html");
        registry
                .addResourceHandler(PUBLIC_PATH + "/css/**")
                .addResourceLocations("classpath:/public/css/");
        registry
                .addResourceHandler(PUBLIC_PATH + "/img/**")
                .addResourceLocations("classpath:/public/img/");
        registry
                .addResourceHandler(PUBLIC_PATH + "/js/**")
                .addResourceLocations("classpath:/public/js/");
        registry
                .addResourceHandler(PUBLIC_PATH + "/favicon.ico")
                .addResourceLocations("classpath:/public/favicon.ico");
        registry
                .addResourceHandler(PUBLIC_PATH + "/logo.png")
                .addResourceLocations("classpath:/public/logo.png");
    }
}