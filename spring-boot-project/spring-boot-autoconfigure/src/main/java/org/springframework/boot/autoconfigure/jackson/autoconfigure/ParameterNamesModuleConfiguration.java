package org.springframework.boot.autoconfigure.jackson.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ParameterNamesModule.class)
public  class ParameterNamesModuleConfiguration {

	@Bean
	@ConditionalOnMissingBean
	ParameterNamesModule parameterNamesModule() {
		return new ParameterNamesModule(JsonCreator.Mode.DEFAULT);
	}

}