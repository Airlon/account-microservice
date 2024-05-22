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
public class GroupResponseDTO {

    protected String id;
    protected String name;
    protected String path;
    protected Map<String, List<String>> attributes;
    protected List<String> realmRoles;
    protected Map<String, List<String>> clientRoles;
    protected List<GroupRepresentation> subGroups;
    private Map<String, Boolean> access;
}
