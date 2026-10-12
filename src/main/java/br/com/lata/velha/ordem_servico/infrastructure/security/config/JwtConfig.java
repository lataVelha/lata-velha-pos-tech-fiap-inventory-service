package br.com.lata.velha.ordem_servico.infrastructure.security.config;

import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.security.interfaces.RSAPublicKey;

/**
 * Inventory nunca emite token — só valida (resource server). Mesma chave pública RSA do
 * OsService (que é quem emite); sem private key, sem JwtEncoder.
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final Logger logger;

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Bean
    public JwtDecoder jwtDecoder() {
        logger.logInfo("Configurando JwtDecoder");
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        logger.logInfo("Configurando JwtAuthenticationConverter");
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
