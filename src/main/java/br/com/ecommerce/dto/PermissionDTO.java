package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6369941037641752333L;
    private String name;
    private String description;
    private Boolean permission;
}
