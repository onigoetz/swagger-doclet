package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.oas.models.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @author RJ Ewing
 */
@Data
@AllArgsConstructor
public final class ModelWrapper<T> {
    private String name;
    private Schema<T> schema;
    /**
     * Only used by composite param beans ... maybe shouldn't be used like this
     */
    private Map<String, PropertyWrapper> properties;
}
