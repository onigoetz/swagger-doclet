package com.tenxerconsulting.swagger.doclet.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.swagger.oas.models.parameters.*;

public class ParameterDeserializer extends StdDeserializer<Parameter> {
    public ParameterDeserializer() {
        this(null);
    }

    public ParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Parameter deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        ObjectCodec mapper = jp.getCodec();
        JsonNode node = mapper.readTree(jp);

        String type = node.has("in") ? node.get("in").asText() : "";

        switch (type) {
            case "query":
                return mapper.treeToValue(node, QueryParameter.class);
            case "path":
                return mapper.treeToValue(node, PathParameter.class);
            case "header":
                return mapper.treeToValue(node, HeaderParameter.class);
            case "cookie":
                return mapper.treeToValue(node, CookieParameter.class);
            default:
                return mapper.treeToValue(node, Parameter.class);
        }
    }
}
