/**
 * Copyright 2014 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package org.apereo.openlrs;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate; 
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder; 
import org.springframework.boot.context.properties.ConfigurationProperties; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import javax.sql.DataSource; 
import java.util.List;

import lti.oauth.OAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ggilbert
 *
 */

@Configuration
@EnableAutoConfiguration(exclude = {ElasticsearchAutoConfiguration.class,ElasticsearchDataAutoConfiguration.class})
@ComponentScan(basePackages={"org.apereo.openlrs","lti"})
public class Application {
	
	@Autowired private OpenLRSAuthenticationFilter openLRSAuthenticationFilter;
	@Autowired private XAPIRequestValidationFilter xapiRequestValidationFilter;
	@Autowired private CORSFilter corsFilter;
	@Autowired private OAuthFilter oAuthFilter;
	
	public static void main(final String[] args) {
		SpringApplication springApplication = new SpringApplication(Application.class);
		springApplication.addListeners(new ApplicationPidFileWriter("openlrs.pid"));
		springApplication.run(args);
	}
	
	@Bean
    @Primary
    public ObjectMapper objectMapper() {
       try{
		ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        return mapper;
       }
       catch(Exception e)
       {
    	   e.printStackTrace();
       }
       return null;
    }
	
	@Bean
	public javax.validation.Validator localValidatorFactoryBean() {
	   return new LocalValidatorFactoryBean();
	}
	
	@Bean
	public FilterRegistrationBean corsFilterBean() {
		try{
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(corsFilter);
		List<String> urls = new ArrayList<String>(1);
		urls.add("/xAPI/*");
		registrationBean.setUrlPatterns(urls);
		registrationBean.setOrder(1);
		return registrationBean;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Bean
	public FilterRegistrationBean securityFilterBean() {
		try
		{
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(openLRSAuthenticationFilter);
		List<String> urls = new ArrayList<String>(2);
		urls.add("/xAPI/statements");
		urls.add("/xAPI/statements/*");
		urls.add("/api/*");
		registrationBean.setUrlPatterns(urls);
		registrationBean.setOrder(2);
		return registrationBean;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Bean
	public FilterRegistrationBean validationFilterBean() {
		try{
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(xapiRequestValidationFilter);
		List<String> urls = new ArrayList<String>(1);
		urls.add("/xAPI/*");
		registrationBean.setUrlPatterns(urls);
		registrationBean.setOrder(3);
		return registrationBean;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Bean
	public FilterRegistrationBean oAuthFilterBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(oAuthFilter);
		List<String> urls = new ArrayList<String>(1);
		urls.add("/lti");
		registrationBean.setUrlPatterns(urls);
		registrationBean.setOrder(4);
		return registrationBean;
	}
	
	// vamsi
	@Bean 
	public DataSource dataSource() {
		String driverClassName="org.postgresql.Driver";
				//String url="jdbc:mysql://localhost:3306/test";
		String url="jdbc:postgresql://localhost:5432/lrs";
				String username="postgres" ;
				String password="root";
		 DriverManagerDataSource dataSource = new DriverManagerDataSource();
		 
		  
		 
		   dataSource.setDriverClassName(driverClassName);
		 
		  
		 
		   dataSource.setUrl(url);
		 
		  
		 
		   dataSource.setUsername(username);
		 
		  
		 
		   dataSource.setPassword(password);
		 
		  
		 
		   return dataSource;
		
	   // return DataSourceBuilder.create().build();
	}
 
    @Bean 
    public JdbcTemplate jdbcTemplate(DataSource dsItems) { 
        return new JdbcTemplate(dsItems); 
    } 
	
	// vamsi
		
}
