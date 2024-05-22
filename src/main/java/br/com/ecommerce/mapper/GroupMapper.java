package br.com.repassa.mapper;

import br.com.repassa.dto.GroupRoleDTO;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    GroupRoleDTO toRepresentation(GroupRepresentation toRepresentation);
}
