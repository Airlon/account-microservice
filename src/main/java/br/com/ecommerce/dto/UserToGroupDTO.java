package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserToGroupDTO {
    @NotEmpty(message = "Um ou mais ids de usuários devem ser informados na lista usersIds")
    private List<String> usersIds;
}
