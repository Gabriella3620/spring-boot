/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package org.springframework.boot.autoconfigure.gson;

 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import org.springframework.boot.autoconfigure.AutoConfiguration;
 import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
 import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
 import org.springframework.boot.context.properties.EnableConfigurationProperties;
 import org.springframework.boot.context.properties.PropertyMapper;
 import org.springframework.context.annotation.Bean;
 import org.springframework.core.Ordered;
 
 /**
  * {@link EnableAutoConfiguration Auto-configuration} for Gson.
  *This class allows to automatically configure Gson in the context of the application.
  * It creates and exposes a Gson bean configured with the specified properties.
  * It is annotated with {@link AutoConfiguration} to indicate that it is an automatic configuration
  * and with {@link ConditionalOnClass} to specify that this configuration will only be activated if the Gson class is present.
  * It is also annotated with {@link EnableConfigurationProperties} to link the specified configuration properties to an instance of {@link GsonProperties}.
  * This class exposes two beans: a {@link GsonBuilder} and a {@link Gson}.
  * The {@link GsonBuilder} bean is configured with one or more custom {@link GsonBuilderCustomizer}.
  * specified as beans. If no GsonBuilderCustomizer is specified, the default configuration will be used.
  * The {@link Gson} bean is created from the configured GsonBuilder.
  * If a Gson bean is already present in the context of the application, the creation of the bean will be ignored thanks to the {@link ConditionalOnMissingBean} annotation.
  * Finally, the inner class {@link StandardGsonBuilderCustomizer} allows to customize the GsonBuilder with properties specified in {@link GsonProperties}.
  * @author David Liu
  * @author Ivan Golovko
  * @author Ishimwe Gabriella Divine
  * @since 1.2.0
  */
 @AutoConfiguration
 @ConditionalOnClass(Gson.class)
 @EnableConfigurationProperties(GsonProperties.class)
 public class GsonAutoConfiguration {
 
 
	 /**
 
	  Creates a {@link Gson} bean from the configured {@link GsonBuilder}.
	  If a Gson bean is already present in the application context, the bean creation will be ignored thanks to the {@link ConditionalOnMissingBean} annotation.
	  @param gsonBuilder the configured GsonBuilder
	  @return the Gson bean created from the configured GsonBuilder
	  */
	 @Bean
	 @ConditionalOnMissingBean
	 public Gson gson(GsonBuilder gsonBuilder) {
		 return gsonBuilder.create();
	 }
	 /**
	  Creates a {@link GsonBuilder} bean configured with the specified {@link GsonBuilderCustomizer}.
	  If no GsonBuilderCustomizer is specified, the default configuration will be used.
	  @param customizers the list of custom GsonBuilderCustomizers
	  @return the configured GsonBuilder bean
	  */
	 @ConditionalOnMissingBean
	 public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {
		 GsonBuilder builder = new GsonBuilder();
		 customizers.forEach((c) -> c.customize(builder));
		 return builder;
	 }
 
 
 
	 @Bean
	 public StandardGsonBuilderCustomizer standardGsonBuilderCustomizer(GsonProperties gsonProperties) {
		 return new StandardGsonBuilderCustomizer(gsonProperties);
	 }
	 /**
 
	  Internal class that implements the {@link GsonBuilderCustomizer} interface.
 
	  This class allows to customize the GsonBuilder with properties specified in {@link GsonProperties}.
	  */
	 static final class StandardGsonBuilderCustomizer implements GsonBuilderCustomizer, Ordered {
 
		 // Gson configuration properties
		 private final GsonProperties properties;
 
		 /**
		  Creates an instance of {@link StandardGsonBuilderCustomizer} with the provided Gson configuration properties.
		  @param properties the Gson configuration properties
		  */
		 StandardGsonBuilderCustomizer(GsonProperties properties) {
			 this.properties = properties;
		 }
 
		 /**
		  Returns the sort order for this object.
		  @return the sort order
		  */
		 @Override
		 public int getOrder() {
			 return 0;
		 }
 
 
		 /**
		  Customize the {@link GsonBuilder} using the Gson configuration properties.
		  @param builder the GsonBuilder to customize
		  */
		 @Override
		 public void customize(GsonBuilder builder) {
			 GsonProperties referenceProperties = this.properties;
			 PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
 
			 // Apply the configuration for the generation of non-executable JSON.
			 map.from(referenceProperties::getGenerateNonExecutableJson).toCall(builder::generateNonExecutableJson);
 
			 // Apply the configuration to exclude fields without the @Expose annotation.
			 map.from(referenceProperties::getExcludeFieldsWithoutExposeAnnotation)
				 .toCall(builder::excludeFieldsWithoutExposeAnnotation);
 
			 // Apply the configuration for serialization of null values.
			 map.from(referenceProperties::getSerializeNulls).whenTrue().toCall(builder::serializeNulls);
 
			 // Apply the configuration for serialization of complex card keys.
			 map.from(referenceProperties::getEnableComplexMapKeySerialization).toCall(builder::enableComplexMapKeySerialization);
 
			 // Apply the configuration for disabling the serialization of internal classes.
			 map.from(referenceProperties::getDisableInnerClassSerialization).toCall(builder::disableInnerClassSerialization);
 
			 // Apply the configuration for the naming strategy of the fields.
			 map.from(referenceProperties::getLongSerializationPolicy).to(builder::setLongSerializationPolicy);
			 
			 // Apply the configuration for the indentation.
			 map.from(referenceProperties::getFieldNamingPolicy).to(builder::setFieldNamingPolicy);
 
			 // Apply the configuration for the indentation.
			 map.from(referenceProperties::getPrettyPrinting).toCall(builder::setPrettyPrinting);
 
			 // Apply the configuration for the tolerance.
			 map.from(referenceProperties::getLenient).toCall(builder::setLenient);
 
			 // Applies the configuration for disabling HTML escape.
			 map.from(referenceProperties::getDisableHtmlEscaping).toCall(builder::disableHtmlEscaping);
 
			 // Apply the configuration for the date format.
			 map.from(referenceProperties::getDateFormat).to(builder::setDateFormat);
		 }
 
	 }
 
 }
 