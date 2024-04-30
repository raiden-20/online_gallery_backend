package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.EmailDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.PasswordDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.EmailAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;
    private final JWTParser jwtParser;

    @Value("${keycloak.realm}")
    private String realm;


    public void changeEmail(EmailDTO emailDTO, String token) {
        UUID userId = jwtParser.getIdFromAccessToken(token);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(String.valueOf(userId));
        UserRepresentation userRepresentation = userResource.toRepresentation();

        if (!usersResource.searchByEmail(emailDTO.getEmail(), true).isEmpty()) {
            throw new EmailAlreadyExistsException();
        }

        userRepresentation.setEmail(emailDTO.getEmail());
        userRepresentation.setEmailVerified(false);

        userResource.update(userRepresentation);
        userResource.sendVerifyEmail();

    }

    public void changePassword(PasswordDTO passwordDTO, String token) {
        UUID userId = jwtParser.getIdFromAccessToken(token);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(String.valueOf(userId));

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(passwordDTO.getPassword());

        userResource.resetPassword(passwordCred);
    }

    public void deleteAccount(String token) {
        UUID userId = jwtParser.getIdFromAccessToken(token);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        usersResource.delete(String.valueOf(userId));
    }
}
