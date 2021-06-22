package com.tenxerconsulting.swagger.doclet;

import static com.tenxerconsulting.swagger.doclet.apidocs.FixtureLoader.getMapper;
import static com.tenxerconsulting.swagger.doclet.apidocs.FixtureLoader.loadFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxerconsulting.swagger.doclet.model.ResourceListing;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Paths;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.ObjectSchema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.responses.ApiResponses;

public class JSONCompare {
    public static void compareListing(String file, Recorder recorder) throws IOException {
        compareCustom(file, recorder, ResourceListing.class, api -> api);
    }

    public static <T> void compareCustom(String file, Recorder recorder, Class<T> readAsClass, Function<OpenAPI, Object> transformer) throws IOException {
        ArgumentCaptor<OpenAPI> openapiCaptor = ArgumentCaptor.forClass(OpenAPI.class);
        verify(recorder).record(any(File.class), openapiCaptor.capture());

        ObjectMapper mapper = getMapper();

        // Transform OpenAPI to ResourceListing for comparison
        T actual = mapper.readValue(
                mapper.writeValueAsString(transformer.apply(openapiCaptor.getValue())),
                readAsClass
        );

        T expected = loadFixture(file, readAsClass);

        //assertThat(actual.toString()).isEqualTo(expected.toString());
        assertEquals(expected.toString(), actual.toString());
    }

    public static void sort(OpenAPI toSort) {
        // Order properties in schemas
        if (toSort.getComponents() != null && toSort.getComponents().getSchemas() != null) {
            toSort.getComponents().setSchemas(sortMap(toSort.getComponents().getSchemas()));

            toSort.getComponents().getSchemas().values().forEach(schema -> {
                if (schema instanceof ObjectSchema) {
                    schema.setProperties(sortMap(schema.getProperties()));
                }
            });
        }

        if (toSort.getPaths() == null) {
            return;
        }

        Paths paths = new Paths();
        paths.setExtensions(toSort.getPaths().getExtensions());

        toSort.setPaths(sortMap(toSort.getPaths(), paths));

        // Order Paths
        toSort.getPaths()
                .values()
                .forEach(entry -> {
                    // Order parameters in operations
                    entry.readOperations().stream()
                            .filter(operation -> operation.getParameters() != null)
                            .forEach(operation -> {
                                operation.setParameters(
                                        operation
                                                .getParameters()
                                                .stream()
                                                .sorted(Comparator.comparing(Parameter::getName))
                                                .collect(Collectors.toList())
                                );
                            });

                    entry.readOperations().stream()
                            .filter(operation -> operation.getResponses() != null)
                            .forEach(operation -> {
                                operation.setResponses(sortMap(operation.getResponses(), new ApiResponses()));
                            });

                });
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortMap(Map<K,V> toSort) {
        return sortMap(toSort, new HashMap<K,V>());
    }

    public static <K extends Comparable<? super K>, V, MAP extends Map<K,V>> MAP sortMap(Map<K,V> toSort, MAP newMap) {
        if (toSort == null) {
            return null;
        }
        toSort.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    newMap.put(entry.getKey(), entry.getValue());
                });
        return newMap;
    }

    public static void compare(String file, Recorder recorder) throws IOException {
        ArgumentCaptor<OpenAPI> openapiCaptor = ArgumentCaptor.forClass(OpenAPI.class);
        verify(recorder).record(any(File.class), openapiCaptor.capture());

        OpenAPI actual = openapiCaptor.getValue();
        sort(actual);
        OpenAPI expected = loadFixture(file, OpenAPI.class);
        sort(expected);

        assertEquals(expected, actual);
    }
}
