package br.com.repassa.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
    @NotEmpty
    @Size(min = 1, max = 255)
    private String group;
    @Size(max = 255)
    private String description;

}
