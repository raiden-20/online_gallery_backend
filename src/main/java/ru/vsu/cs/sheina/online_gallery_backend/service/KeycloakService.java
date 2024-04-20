package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.DeleteDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.EmailDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.PasswordDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.EmailAlreadyExistsException;


@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;


    //TODO add user verification (somehow)

    public void changeEmail(EmailDTO emailDTO) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(String.valueOf(emailDTO.getId()));
        UserRepresentation userRepresentation = userResource.toRepresentation();

        if (!usersResource.searchByEmail(emailDTO.getEmail(), true).isEmpty()) {
            throw new EmailAlreadyExistsException();
        }

        userRepresentation.setEmail(emailDTO.getEmail());
        userRepresentation.setEmailVerified(false);

        userResource.update(userRepresentation);
        userResource.sendVerifyEmail();

    }

    public void changePassword(PasswordDTO passwordDTO) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(String.valueOf(passwordDTO.getId()));

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(passwordDTO.getPassword());

        userResource.resetPassword(passwordCred);
    }

    public void deleteAccount(DeleteDTO deleteDTO) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        usersResource.delete(String.valueOf(deleteDTO.getId()));
    }
}
