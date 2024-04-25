package ru.vsu.cs.sheina.online_gallery_backend.utils;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;

import java.util.UUID;

@Component
public class JWTParser {

    public UUID getIdFromAccessToken(String token) {
        try {
            token = token.substring(7);
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setSkipSignatureVerification()
                    .setExpectedAudience("account")
                    .build();
            jwtConsumer.setSkipVerificationKeyResolutionOnNone(true);
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            return UUID.fromString(jwtClaims.getStringClaimValue("sub"));
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new BadCredentialsException();
        }
    }
}
