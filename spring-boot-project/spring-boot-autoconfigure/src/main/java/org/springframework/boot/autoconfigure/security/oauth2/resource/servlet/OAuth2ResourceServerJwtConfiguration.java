/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.autoconfigure.security.oauth2.resource.servlet;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.oauth2.resource.IssuerUriCondition;
import org.springframework.boot.autoconfigure.security.oauth2.resource.KeyValueCondition;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;

/**
 * Configures a {@link JwtDecoder} when a JWK Set URI, OpenID Connect Issuer URI or Public
 * Key configuration is available. Also configures a {@link SecurityFilterChain} if a
 * {@link JwtDecoder} bean is found.
 *
 * @author Madhura Bhave
 * @author Artsiom Yudovin
 * @author HaiTao Zhang
 * @author Mushtaq Ahmed
 */
@Configuration(proxyBeanMethods = false)
class OAuth2ResourceServerJwtConfiguration extends AbstractResourceServerAutoConfiguration  {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(JwtDecoder.class)
	static class JwtDecoderConfiguration extends AbstractJwtConfiguration {
      
		private NimbusJwtDecoder nimbusJwtDecoder;

		JwtDecoderConfiguration(OAuth2ResourceServerProperties properties) {
			super(properties);
		}

		@Bean
		@ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri")
		JwtDecoder jwtDecoderByJwkKeySetUri() {
			templateMethodjwtDecoderBy();
			return nimbusJwtDecoder;
		}

		@Bean
		@Conditional(KeyValueCondition.class)
		JwtDecoder  jwtDecoderByPublicKeyValue() throws Exception {
			templateMethodDecoderByPublicKeyValue();
			return nimbusJwtDecoder;
		}

		void setJwtUri(){
			nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(this.properties.getJwkSetUri())
					.jwsAlgorithms(this::jwsAlgorithms).build();
		}
		void setJwtDecoderValidator(){
			nimbusJwtDecoder.setJwtValidator(super.getValidators(defaultValidator));

		}
		


		@Bean
		@Conditional(IssuerUriCondition.class)
		SupplierJwtDecoder jwtDecoderByIssuerUri() {
			return new SupplierJwtDecoder(() -> {
				String issuerUri = this.properties.getIssuerUri();
				NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
				jwtDecoder.setJwtValidator(getValidators(() -> JwtValidators.createDefaultWithIssuer(issuerUri)));
				return jwtDecoder;
			});
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnDefaultWebSecurity
	static class OAuth2SecurityFilterChainConfiguration {

		@Bean
		@ConditionalOnBean(JwtDecoder.class)
		SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
			http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
			http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
			return http.build();
		}

	}

}
