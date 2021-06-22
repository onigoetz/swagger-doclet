package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.oas.models.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author RJ Ewing
 */
@Data
@AllArgsConstructor
public final class PropertyWrapper {
    /**
     * the raw field name the property came from
     */
    private String rawFieldName;

    private Schema<?> property;

    /**
     * category of parameter of the field, only applicable to composite parameter fields
     */
    private String paramCategory;

    /**
     * Is the property required ? only applicable to composite parameter fields
     */
    private Boolean required;

    /**
     * Is the property deprecated ? only applicable to composite parameter fields
     */
    private Boolean deprecated;
}
