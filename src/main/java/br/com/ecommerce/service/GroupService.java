package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.CategoryPermissionDTO;
import br.com.repassa.dto.GroupDto;
import br.com.repassa.dto.GroupRoleDTO;
import br.com.repassa.dto.GroupStatisticsDTO;
import br.com.repassa.utils.PermissionComparator;
import br.com.repassa.dto.PermissionDTO;
import br.com.repassa.exception.AccountError;
import br.com.repassa.mapper.GroupMapper;
import br.com.repassa.resource.client.KeycloakConfig;
import br.com.repassa.utils.PermissionNameComparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@ApplicationScoped
@Slf4j
public class GroupService {

    @Inject
    KeycloakConfig keycloak;

    @Inject
    ClientService clientService;

    public Map<String, List<String>> getRolesClients(GroupResource group) {
        Map<String, List<String>> rolesClients = new HashMap<>();
        Map<String, String> clients = clientService.getClientsIdsNames();

        for (String clientId : clients.keySet()) {
            List<RoleRepresentation> rolesRepresentation = group.roles().clientLevel(clientId).listAll();
            if (!rolesRepresentation.isEmpty()) {
                rolesClients.put(
                        clients.get(clientId),
                        rolesRepresentation.stream().map(RoleRepresentation::getName).toList()
                );
            }

        }
        return rolesClients;
    }

    public Response getUserByGroup(String group) throws RepassaException {
        List<UserRepresentation> members = keycloak.getRealmResource().groups().group(group).members();

        if (members != null && !members.isEmpty()) {
            return Response.ok(members).build();
        }
        throw new RepassaException(AccountError.MEMBROS_NAO_ENCONTRADOS);
    }

    public Response findGroupStatistics() throws RepassaException {
        try{
            GroupsResource groupsResource = keycloak.getRealmResource().groups();
            List<GroupRepresentation> groupRepresentationList = groupsResource.groups();
            List<GroupStatisticsDTO> groupStatisticsDTOList = new ArrayList<>();
            for (GroupRepresentation groupRepresentation : groupRepresentationList) {
                var group = keycloak.getRealmResource().groups().group(groupRepresentation.getId());
                GroupRepresentation data = group.toRepresentation();
                log.info("Processando grupo: {}", data.getName());

                GroupStatisticsDTO groupStatisticsDTO = new GroupStatisticsDTO();
                groupStatisticsDTO.setId(data.getId());
                groupStatisticsDTO.setGroupName(data.getName());
                groupStatisticsDTO.setTotalUsersInGroup(groupsResource.group(data.getId()).members().size());
                String descricao = Objects.isNull(data.getAttributes().get("description")) ? null : data.getAttributes().get("description").get(0);
                groupStatisticsDTO.setGroupDescription(descricao);
                groupStatisticsDTOList.add(groupStatisticsDTO);
            }

            if (!groupStatisticsDTOList.isEmpty()) {
                log.info("Exibindo estatisticas do grupo");
                return Response.ok(groupStatisticsDTOList).build();
            }
            log.info("Estatistica não encontrada!");
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (NotFoundException notFoundException){
            throw new RepassaException(AccountError.GRUPO_NAO_ENCONTRADO);
        }


    }

    public Response findAllGroups() {
        GroupsResource groupsResource = keycloak.getRealmResource().groups();
        List<GroupRepresentation> groups = groupsResource.groups();
        List<GroupRoleDTO> dtos = new ArrayList<>();
        for (GroupRepresentation groupRepresentation : groups) {
            var group = keycloak.getRealmResource().groups().group(groupRepresentation.getId());
            GroupRepresentation data = group.toRepresentation();
            GroupRoleDTO dto = GroupMapper.INSTANCE.toRepresentation(data);
            List<RoleRepresentation> roleRepresentationsRealm = group.roles().realmLevel().listAll();
            dto.setRealmRoles(roleRepresentationsRealm.stream().map(RoleRepresentation::getName).toList());
            dto.setClientRoles(getRolesClients(group));
            dtos.add(dto);
        }

        if (!groups.isEmpty()) {
            return Response.ok(dtos).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Response findByGroup(String id) throws RepassaException {
        var group = keycloak.getRealmResource().groups().group(id);
        GroupRepresentation groupRepresentation;
        try{
            groupRepresentation = group.toRepresentation();
        }catch (NotFoundException notFoundException){
            throw new RepassaException(AccountError.GRUPO_NAO_ENCONTRADO);
        }

        GroupRoleDTO groupMapper = GroupMapper.INSTANCE.toRepresentation(groupRepresentation);
        List<RoleRepresentation> allRolesRealm = group.roles().realmLevel().listAll();
        groupMapper.setRealmRoles(allRolesRealm.stream().map(RoleRepresentation::getName).toList());
        groupMapper.setClientRoles(getRolesClients(group));

        return Response.ok(groupMapper).build();

    }

    public Response createGroup(GroupDto groupRequest) throws RepassaException {
        validGroupNew(groupRequest);
        validGroupStrings(groupRequest);

        GroupRepresentation representationOfGroup = new GroupRepresentation();
        representationOfGroup.setName(groupRequest.getGroup().toUpperCase());
        representationOfGroup.setAttributes(new HashedMap<>());
        representationOfGroup.getAttributes().put("description", List.of(groupRequest.getDescription()));

        Response newGroup = keycloak.getRealmResource().groups().add(representationOfGroup);

        if (newGroup != null && newGroup.getEntity() != null) {
            GroupRepresentation repr = isExistsObject(representationOfGroup.getName());
            Response findGroupObject = findByGroup(repr.getId());
            if (findGroupObject.getStatus() == Response.Status.OK.getStatusCode()) {
                return Response.status(Response.Status.CREATED).entity(findGroupObject.getEntity()).build();
            }
            return Response.status(Response.Status.CREATED).entity(GroupMapper.INSTANCE.toRepresentation(repr)).build();
        }
        throw new RepassaException(AccountError.ERRO_CRIAR_GRUPO);
    }

    public Response updateGroup(String id, GroupDto groupRequest) throws RepassaException {
        GroupResource foundGroup = keycloak.getRealmResource().groups().group(id);

        if (isNull(foundGroup))
            throw new RepassaException(AccountError.GRUPO_NAO_ENCONTRADO);

        validGroupStrings(groupRequest);

        GroupRepresentation representationOfGroup = foundGroup.toRepresentation();

        representationOfGroup.setName(groupRequest.getGroup().toUpperCase());
        representationOfGroup.setAttributes(new HashedMap<>());
        representationOfGroup.getAttributes().put("description", List.of(groupRequest.getDescription()));

        foundGroup.update(representationOfGroup);

        return Response.ok(representationOfGroup).build();
    }

    public Response deleteGroup(String id) throws RepassaException {
        try {
            keycloak.getRealmResource().groups().group(id).remove();
        }catch (NotFoundException notFoundException){
            throw new RepassaException(AccountError.GRUPO_NAO_ENCONTRADO);
        }


        return Response.ok(id).build();
    }

    public void validGroupStrings(GroupDto groupRequest) throws RepassaException {
        if (groupRequest.getGroup().length() > 255 || groupRequest.getGroup().length() < 1) {
            throw new RepassaException(AccountError.NOME_GRUPO_INVALIDO);
        }
        if (groupRequest.getDescription().length() > 255) {
            throw new RepassaException(AccountError.DESCRICAO_GRUPO_INVALIDO);
        }
    }

    public void validGroupNew(GroupDto groupDto) throws RepassaException {
        if (isNull(groupDto.getGroup())) {
            throw new RepassaException(AccountError.NOME_GRUPO_NULO);
        }
        if (isExists(groupDto.getGroup().toUpperCase())) {
            throw new RepassaException(AccountError.NOME_GRUPO_REPETIDO);
        }
    }

    public boolean isExists(String groupName) {
        GroupsResource groupsResource = keycloak.getRealmResource().groups();
        List<GroupRepresentation> groups = groupsResource.groups();
        for (GroupRepresentation group : groups) {
            if (group.getName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    public GroupRepresentation isExistsObject(String groupName) {
        GroupsResource groupsResource = keycloak.getRealmResource().groups();
        List<GroupRepresentation> groups = groupsResource.groups();

        for (GroupRepresentation group : groups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    public List<CategoryPermissionDTO> getPermissionGroup(String idGroup) throws RepassaException {

        GroupRepresentation groupRepresentation;
        try {
            groupRepresentation = keycloak.getRealmResource().groups().group(idGroup).toRepresentation();
        } catch (NotFoundException e) {
            throw new RepassaException(AccountError.GRUPO_NAO_ENCONTRADO);
        }

        List<String> groupRolesList = groupRepresentation.getRealmRoles();

        List<RoleRepresentation> roles = keycloak.getRealmResource().roles().list();

        List<PermissionDTO> permissionDTOList = roles.stream()
                .filter(permissionRole -> permissionRole.getDescription() != null)
                .map(permissionRole -> PermissionDTO.builder()
                        .name(permissionRole.getName())
                        .description(permissionRole.getDescription())
                        .permission(false)
                        .build())
                .peek(permissionDTO ->
                        log.info("Salvando todas as permissoes {} em uma lista de permissionDTO setando permission false como padrao", permissionDTO))
                .toList();
        log.info("Roles salvas com sucesso!");

        groupRolesList.forEach(permissionGroup -> permissionDTOList.stream()
                .filter(permission -> permission.getName().equals(permissionGroup))
                .findFirst()
                .ifPresent(permission -> {
                    permission.setPermission(true);
                    log.info("Atualizando a lista de permissoes que o grupo possui {} para true", permission);
                }));
        log.info("Roles Atualizadas com sucesso!");

        List<CategoryPermissionDTO> categoryList = permissionDTOList.stream()
                .filter(category -> !getMainCategory(category.getName()).isEmpty())
                .collect(Collectors.groupingBy(category -> getMainCategory(category.getName())))
                .entrySet()
                .stream()
                .map(entry -> new CategoryPermissionDTO(entry.getKey(), entry.getValue()))
                .sorted(new PermissionComparator())
                .peek(categoryPermissionDTO ->
                        log.info("Retorna categoria {} de permissionDTO com {}, ordenado e classificado",
                                categoryPermissionDTO.getCategory(), categoryPermissionDTO.getPermissions()))
                .toList();
        log.info("Categorias validadas e ordenadas com sucesso!");

        List<CategoryPermissionDTO> categorySortedPermissionList = categoryList.stream()
                .map(categoryPermissionDTO -> {
                    List<PermissionDTO> sortedPermissions = categoryPermissionDTO.getPermissions().stream()
                            .sorted(new PermissionNameComparator())
                            .collect(Collectors.toList());
                    categoryPermissionDTO.setPermissions(sortedPermissions);
                    return categoryPermissionDTO;
                })
                .toList();
        log.info("As permissoes das categorias foram ordenadas com sucesso!");

        return categorySortedPermissionList;
    }

    private String getMainCategory(String category) {
        if (category == null) {
            return "";
        }

        Pattern pattern = Pattern.compile("^(.*?)\\.");
        Matcher matcher = pattern.matcher(category);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
