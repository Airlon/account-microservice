package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPermissionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4821834664788626209L;

    private String category;
    private List<PermissionDTO> permissions;
}
