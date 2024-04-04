package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.EmailDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.PasswordDTO;
import org.keycloak.representations.idm.CredentialRepresentation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;


    //TODO add user verification (somehow)

    public void changeEmail(EmailDTO emailDTO) {
        UserRepresentation user = keycloak.realm(realm)
                .users()
                .get(emailDTO.getId().toString())
                .toRepresentation();

        user.setEmail(emailDTO.getEmail());
    }

    public void changePassword(PasswordDTO passwordDTO) {
        UserRepresentation user = keycloak.realm(realm)
                .users()
                .get(passwordDTO.getId().toString())
                .toRepresentation();

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(passwordDTO.getPassword());

        List<CredentialRepresentation> credential = List.of(passwordCred);

        user.setCredentials(credential);
    }
}
