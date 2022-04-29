package org.jcm.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class CreateUserRequest {

    @NotNull(message ="Name is Required")
    private String name;
    @NotNull(message = "Age is Required")
    private Integer age;
}
