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

package org.springframework.boot.autoconfigure.security.oauth2.resource.reactive;

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
import org.springframework.boot.autoconfigure.security.oauth2.resource.IssuerUriCondition;
import org.springframework.boot.autoconfigure.security.oauth2.resource.KeyValueCondition;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.jwt.SupplierReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.CollectionUtils;

/**
 * Configures a {@link ReactiveJwtDecoder} when a JWK Set URI, OpenID Connect Issuer URI
 * or Public Key configuration is available. Also configures a
 * {@link SecurityWebFilterChain} if a {@link ReactiveJwtDecoder} bean is found.
 *
 * @author Madhura Bhave
 * @author Artsiom Yudovin
 * @author HaiTao Zhang
 * @author Anastasiia Losieva
 * @author Mushtaq Ahmed
 */
@Configuration(proxyBeanMethods = false)
class ReactiveOAuth2ResourceServerJwkConfiguration extends  AbstractResourceServerAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(ReactiveJwtDecoder.class)
	static class JwtConfiguration extends AbstractJwtConfiguration  {


		JwtConfiguration(OAuth2ResourceServerProperties properties) {
			super(properties);
		}
		@Bean
		@ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri")
		ReactiveJwtDecoder jwtDecoder() {
			templateMethodjwtDecoderBy();
			return nimbusReactiveJwtDecoder;
		}


		@Bean
		@Conditional(KeyValueCondition.class)
		NimbusReactiveJwtDecoder jwtDecoderByPublicKeyValue() throws Exception {
			templateMethodDecoderByPublicKeyValue();
			return nimbusReactiveJwtDecoder;
		}


		void setJwtDecoder(){
			nimbusReactiveJwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(publicKey)
					.signatureAlgorithm(SignatureAlgorithm.from(exactlyOneAlgorithm())).build();
			nimbusReactiveJwtDecoder.setJwtValidator(getValidators(JwtValidators::createDefault));


		}
		void setJwtUri(){
			nimbusReactiveJwtDecoder = NimbusReactiveJwtDecoder
					.withJwkSetUri(this.properties.getJwkSetUri()).jwsAlgorithms(this::jwsAlgorithms).build();

		}
		void setJwtDecoderValidator(){

			nimbusReactiveJwtDecoder.setJwtValidator(super.getValidators(defaultValidator));

		}

	

		@Bean
		@Conditional(IssuerUriCondition.class)
		SupplierReactiveJwtDecoder jwtDecoderByIssuerUri() {
			return new SupplierReactiveJwtDecoder(() -> {
				NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders
						.fromIssuerLocation(this.properties.getIssuerUri());
				jwtDecoder.setJwtValidator(
						getValidators(() -> JwtValidators.createDefaultWithIssuer(this.properties.getIssuerUri())));
				return jwtDecoder;
			});
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(SecurityWebFilterChain.class)
	static class WebSecurityConfiguration {

		@Bean
		@ConditionalOnBean(ReactiveJwtDecoder.class)
		SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
			http.authorizeExchange((exchanges) -> exchanges.anyExchange().authenticated());
			http.oauth2ResourceServer((server) -> customDecoder(server, jwtDecoder));
			return http.build();
		}

		private void customDecoder(OAuth2ResourceServerSpec server, ReactiveJwtDecoder decoder) {
			server.jwt((jwt) -> jwt.jwtDecoder(decoder));
		}

	}

}
