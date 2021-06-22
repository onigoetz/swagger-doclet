package com.tenxerconsulting.swagger.doclet.apidocs;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.tenxerconsulting.swagger.doclet.json.MapperModule;

public class FixtureLoader {

    private FixtureLoader() {
    }

    public static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MapperModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

        return mapper;
    }

    public static <T> T loadFixture(String path, Class<T> resourceClass) throws IOException {
        InputStream is = null;
        try {
            is = FixtureLoader.class.getResourceAsStream(path);
            return getMapper().readValue(is, resourceClass);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static <T> T loadFixture(String path, MapType typeRef) throws IOException {
        InputStream is = null;
        try {
            is = FixtureLoader.class.getResourceAsStream(path);
            return getMapper().readValue(is, typeRef);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
