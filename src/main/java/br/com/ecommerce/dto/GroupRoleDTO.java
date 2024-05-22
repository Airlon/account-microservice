package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupRoleDTO {

    protected String id;
    protected String name;
    private Map<String, Boolean> access;
    private List<String> realmRoles;
    private Map<String,List<String>> clientRoles;
    protected Map<String, List<String>> attributes;
    protected List<GroupRepresentation> subGroups;
}
