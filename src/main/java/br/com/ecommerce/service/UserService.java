package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.UserDTO;
import br.com.repassa.dto.UserToGroupDTO;
import br.com.repassa.exception.AccountError;
import br.com.repassa.resource.client.KeycloakConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    private static final String USUARIO_LOG = "Usuário";

    @Inject
    KeycloakConfig keycloakConfig;

    public Response findUsersOutOfGroup(String groupId) {
        RealmResource realmResource = keycloakConfig.getRealmResource();
        List<UserDTO> userPatternList = realmResource.users().list()
                .stream()
                .map(o -> UserDTO.builder()
                        .id(o.getId())
                        .name(o.getFirstName() + " " + o.getLastName())
                        .build())
                .toList();
        List<UserRepresentation> groupListMembers = realmResource.groups().group(groupId).members();

        List<UserDTO> membersOutOfGroup = userPatternList.stream()
                .filter(user -> groupListMembers.stream().noneMatch(member -> member.getId().equals(user.getId())))
                .collect(Collectors.toList());

        return Response.ok(membersOutOfGroup).build();
    }

    public Response findUsersByFilter(String filter) throws RepassaException {
        if (!isFilterMinNumberOfCharacters(filter)) {
            throw new RepassaException(AccountError.FILTRO_USUARIO_INVALIDO);
        }
        RealmResource realmResource = keycloakConfig.getRealmResource();
        List<UserRepresentation> usersByUsername = realmResource.users().searchByUsername(filter, false);
        List<UserRepresentation> usersByFirstName = realmResource.users().searchByFirstName(filter, false);
        List<UserRepresentation> usersByLastName = realmResource.users().searchByLastName(filter, false);
        List<UserRepresentation> usersByEmail = realmResource.users().searchByEmail(filter, false);

        List<UserRepresentation> users = new ArrayList<>();
        users.addAll(usersByUsername);
        users.addAll(usersByFirstName);
        users.addAll(usersByLastName);
        users.addAll(usersByEmail);

        List<UserRepresentation> usersWithoutDuplicates = users.stream()
                .filter(distinctByKey(UserRepresentation::getUsername))
                .toList();

        if (!usersWithoutDuplicates.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String usersString = mapper.writeValueAsString(usersWithoutDuplicates);
                return Response.ok(usersString).build();
            } catch (JsonProcessingException e) {
                throw new RepassaException(AccountError.JSON_GERADO_NAO_VALIDO);
            }
        } else {
            return Response.noContent().build();
        }
    }

    public Response addUsersToGroup(String id, UserToGroupDTO userIdList) {
        RealmResource realmResource = keycloakConfig.getRealmResource();
        GroupRepresentation groupRep = realmResource.groups().group(id).toRepresentation();
        List<UserRepresentation> groupMembers = realmResource.groups().group(id).members();
        List<String> warnings = new ArrayList<>();
        for(String userId : userIdList.getUsersIds()) {
            try {
                final var userOptional = findUserInGroup(groupMembers, userId);
                if(userOptional.isEmpty()) {
                    realmResource.users().get(userId).joinGroup(groupRep.getId());
                    warnings.add(USUARIO_LOG + " com userId: "
                            + userId
                            + " foi adicionado ao grupo");
                } else {
                    final var user = userOptional.orElse(new UserRepresentation());
                    warnings.add(USUARIO_LOG + " "
                            + user.getFirstName() + " "
                            + user.getLastName()
                            + " já pertence ao grupo");
                }
            } catch (NotFoundException e) {
                warnings.add(USUARIO_LOG + " com userId: "
                        + userId
                        + " não foi encontrado no keycloak");
            }
        }
        return Response.ok(warnings).build();
    }

    public Response removeUsersFromGroup(String id, UserToGroupDTO userIdList) {
        RealmResource realmResource = keycloakConfig.getRealmResource();
        GroupRepresentation groupRep = realmResource.groups().group(id).toRepresentation();
        List<UserRepresentation> groupMembers = realmResource.groups().group(id).members();
        List<String> warnings = new ArrayList<>();
        for(String userId : userIdList.getUsersIds()) {
            try {
                final var userOptional = findUserInGroup(groupMembers, userId);
                if(userOptional.isPresent()) {
                    realmResource.users().get(userId).leaveGroup(groupRep.getId());
                    warnings.add(USUARIO_LOG + " com userId: "
                            + userId
                            + " foi removido do grupo.");
                } else {
                    warnings.add(USUARIO_LOG + " "
                            + userId
                            + " não pertence ao grupo");
                }
            } catch (NotFoundException e) {
                warnings.add(USUARIO_LOG + " com userId: "
                        + userId
                        + " não foi encontrado");
            }
        }
        return Response.ok(warnings).build();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public Optional<UserRepresentation> findUserInGroup(List<UserRepresentation> groupMembers, String userId) {
        return groupMembers.stream().filter(member -> member.getId().equals(userId)).findFirst();
    }

    public boolean isFilterMinNumberOfCharacters(String filter) {
        return filter.length() > 2;
    }

}
