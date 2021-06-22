package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import lombok.Getter;

public class ParameterOrBody {
    @Getter
    Parameter parameter;

    @Getter
    RequestBody body;

    @Getter
    FormItem formItem;

    public ParameterOrBody(Parameter parameter) {
        this.parameter = parameter;
    }

    public ParameterOrBody(RequestBody body) {
        this.body = body;
    }

    public ParameterOrBody(FormItem formItem) {
        this.formItem = formItem;
    }
}
