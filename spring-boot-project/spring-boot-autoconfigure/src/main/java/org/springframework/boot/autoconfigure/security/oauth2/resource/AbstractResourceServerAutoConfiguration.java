package org.springframework.boot.autoconfigure.security.oauth2.resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.IssuerUriCondition;
import org.springframework.boot.autoconfigure.security.oauth2.resource.KeyValueCondition;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Supplier;

public abstract class  AbstractResourceServerAutoConfiguration {


	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean(JwtDecoder.class)
	static class AbstractJwtConfiguration {
		private final OAuth2ResourceServerProperties.Jwt properties;

		AbstractJwtConfiguration(OAuth2ResourceServerProperties properties) {
			this.properties = properties.getJwt();
		}



		protected void jwsAlgorithms(Set<SignatureAlgorithm> signatureAlgorithms) {
			for (String algorithm : this.properties.getJwsAlgorithms()) {
				signatureAlgorithms.add(SignatureAlgorithm.from(algorithm));
			}
		}

		protected OAuth2TokenValidator<Jwt> getValidators(Supplier<OAuth2TokenValidator<Jwt>> defaultValidator) {
			OAuth2TokenValidator<Jwt> defaultValidators = defaultValidator.get();
			List<String> audiences = this.properties.getAudiences();
			if (CollectionUtils.isEmpty(audiences)) {
				return defaultValidators;
			}
			List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
			validators.add(defaultValidators);
			validators.add(new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
					(aud) -> aud != null && !Collections.disjoint(aud, audiences)));
			return new DelegatingOAuth2TokenValidator<>(validators);
		}



		protected byte[] getKeySpec(String keyValue) {
			keyValue = keyValue.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
			return Base64.getMimeDecoder().decode(keyValue);
		}

		protected String exactlyOneAlgorithm() {
			List<String> algorithms = this.properties.getJwsAlgorithms();
			int count = (algorithms != null) ? algorithms.size() : 0;
			if (count != 1) {
				throw new IllegalStateException(
						"Creating a JWT decoder using a public key requires exactly one JWS algorithm but " + count
								+ " were configured");
			}
			return algorithms.get(0);
		}

		

	}
}
