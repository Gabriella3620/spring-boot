package org.springframework.boot.autoconfigure.jackson.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.jackson.JsonMixinModule;
import org.springframework.boot.jackson.JsonMixinModuleEntries;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonMixinConfiguration {

	@Bean
	static JsonMixinModuleEntries jsonMixinModuleEntries(ApplicationContext context) {
		List<String> packages = AutoConfigurationPackages.has(context) ? AutoConfigurationPackages.get(context)
				: Collections.emptyList();
		return JsonMixinModuleEntries.scan(context, packages);
	}

	@Bean
	JsonMixinModule jsonMixinModule(ApplicationContext context, JsonMixinModuleEntries entries) {
		JsonMixinModule jsonMixinModule = new JsonMixinModule();
		jsonMixinModule.registerEntries(entries, context.getClassLoader());
		return jsonMixinModule;
	}

}