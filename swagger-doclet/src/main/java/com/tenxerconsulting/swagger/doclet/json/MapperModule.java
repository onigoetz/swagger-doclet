package com.tenxerconsulting.swagger.doclet.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;

public class MapperModule extends SimpleModule {
    public MapperModule() {
        super();

        this.addDeserializer(Parameter.class, new ParameterDeserializer());
        this.addDeserializer(Schema.class, new SchemaDeserializer());
    }
}
