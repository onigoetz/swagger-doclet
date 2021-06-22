package com.tenxerconsulting.swagger.doclet.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import lombok.Getter;
import lombok.Setter;

public class ParametersAndBody {
    @Getter
    private final List<Parameter> parameters;

    @Getter
    @Setter
    private RequestBody body;

    public ParametersAndBody() {
        parameters = new ArrayList<>();
    }
}
