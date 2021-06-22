package com.tenxerconsulting.swagger.doclet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperBuilder {

    interface CsvItemProcessor {
        void csvItem(String name, boolean val);
    }

    /**
     * This creates a ObjectMapperRecorder
     * @param serializationFeaturesCsv The CSV of serialization features to enable
     * @param deserializationFeaturesCsv The CSV of deserialization features to enable
     * @param defaultTyping The default typing to enable
     * @param serializationInclusion The serialization inclusion to use e.g. NON_NULL
     */
    public static ObjectMapper build(String serializationFeaturesCsv, String deserializationFeaturesCsv, String defaultTyping, String serializationInclusion) {

        ObjectMapper mapper = new ObjectMapper();

        // configure serialization features
        if (serializationFeaturesCsv == null) {
            serializationFeaturesCsv = SerializationFeature.INDENT_OUTPUT.toString() + ":true";
        }
        processCsv(serializationFeaturesCsv, (name, value) -> {
            for (SerializationFeature feature : SerializationFeature.values()) {
                if (feature.name().equalsIgnoreCase(name)) {
                    mapper.configure(feature, value);
                }
            }
        });

        // configure deserialization features
        if (deserializationFeaturesCsv != null) {
            processCsv(deserializationFeaturesCsv, (name, value) -> {
                for (DeserializationFeature feature : DeserializationFeature.values()) {
                    if (feature.name().equalsIgnoreCase(name)) {
                        mapper.configure(feature, value);
                    }
                }
            });
        }

        if (defaultTyping != null) {
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.valueOf(defaultTyping));
        }

        if (serializationInclusion == null) {
            serializationInclusion = JsonInclude.Include.NON_NULL.toString();
        }

        mapper.setSerializationInclusion(JsonInclude.Include.valueOf(serializationInclusion));

        return mapper;

    }

    private static void processCsv(String csv, ObjectMapperBuilder.CsvItemProcessor processor) {
        if (csv != null) {
            csv = csv.trim();
            if (csv.length() > 0) {
                String[] nvps = csv.split(",");
                for (String nvp : nvps) {
                    nvp = nvp.trim();
                    if (nvp.length() > 0) {
                        if (nvp.contains(":")) {
                            String[] nvpParts = nvp.split(":");
                            String name = nvpParts[0].trim();
                            String valPart = nvpParts[1].trim();
                            if (valPart.equalsIgnoreCase("true")) {
                                processor.csvItem(name, true);
                            } else if (valPart.equalsIgnoreCase("false")) {
                                processor.csvItem(name, false);
                            }
                        }
                    }
                }
            }
        }
    }
}
