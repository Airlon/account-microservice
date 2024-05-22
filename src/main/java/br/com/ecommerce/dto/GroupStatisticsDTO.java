package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupStatisticsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6699607994682999948L;

    @NotEmpty
    private String id;

    @NotEmpty
    @Size(min = 1, max = 255)
    private String groupName;

    @Size(max = 255)
    private String groupDescription;

    private Integer totalUsersInGroup;
}
