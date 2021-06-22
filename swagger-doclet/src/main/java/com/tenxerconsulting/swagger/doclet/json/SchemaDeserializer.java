package com.tenxerconsulting.swagger.doclet.json;

import static com.tenxerconsulting.swagger.doclet.parser.ParserHelper.createRef;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.swagger.oas.models.media.*;

public class SchemaDeserializer extends StdDeserializer<Schema<?>> {
    public SchemaDeserializer() {
        this(null);
    }

    public SchemaDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Schema<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec mapper = jp.getCodec();
        JsonNode node = mapper.readTree(jp);

        if (node.has("allOf") || node.has("anyOf") || node.has("oneOf")) {
            return mapper.treeToValue(node, ComposedSchema.class);
        }

        if (node.has("$ref")) {
            return createRef(node.get("$ref").asText());
        }

        String type = node.has("type") ? node.get("type").asText() : "";
        String format = node.has("format") ? node.get("format").asText() : "";

        switch (type) {
            case "array":
                return mapper.treeToValue(node, ArraySchema.class);
            case "boolean":
                return mapper.treeToValue(node, BooleanSchema.class);
            case "integer":
                if ("int32".equals(format)) {
                    return mapper.treeToValue(node, IntegerSchema.class);
                }

                if ("int64".equals(format)) {
                    return mapper.treeToValue(node, IntegerSchema.class).format("int64");
                }

                throw new IllegalStateException("Dunno what to do with type=integer, format=" + format);
            case "number":
                return mapper.treeToValue(node, NumberSchema.class);
            case "object":
                //if (node.has("defaultObject")) {
                return mapper.treeToValue(node, ObjectSchema.class);
            //    return mapper.treeToValue(node, MapSchema.class);
            //} else {
            //}
            case "string":
                switch (format) {
                    case "password":
                        return mapper.treeToValue(node, PasswordSchema.class);
                    case "uuid":
                        return mapper.treeToValue(node, UUIDSchema.class);
                    case "binary":
                        return mapper.treeToValue(node, FileSchema.class);
                    // TODO :: BinarySchema uses the same type/format pair, how can we differentiate ?
                    //return mapper.treeToValue(node, BinarySchema.class);
                    case "email":
                        return mapper.treeToValue(node, EmailSchema.class);
                    case "date":
                        return mapper.treeToValue(node, DateSchema.class);
                    case "date-time":
                        return mapper.treeToValue(node, DateTimeSchema.class);
                    case "byte":
                        return mapper.treeToValue(node, ByteArraySchema.class);
                    default:
                        return mapper.treeToValue(node, StringSchema.class);
                }
            default:
                if (node.has("properties")) {
                    return mapper.treeToValue(node, ObjectSchema.class);
                }

                throw new JsonMappingException(jp, "Cannot deserialize Schema: " + node.toString());
        }
    }
}
