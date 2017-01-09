package com.tenxerconsulting.swagger.doclet.model;


import io.swagger.models.properties.Property;

/**
 * @author RJ Ewing
 */
public class PropertyWrapper {
    private String rawFieldName;
    private String paramCategory;

    private Property property;

    public PropertyWrapper(String rawFieldName, String paramCategory, Property property) {
        this.rawFieldName = rawFieldName;
        this.paramCategory = paramCategory;
        this.property = property;
    }

    /**
     * This gets the raw field name the property came from
     * @return the raw field name the property came from
     */
    public String getRawFieldName() {
        return rawFieldName;
    }

    /**
     * This gets category of parameter of the field, only applicable to composite parameter fields
     * @return the category of parameter of the field, only applicable to composite parameter fields
     */
    public String getParamCategory() {
        return paramCategory;
    }

    public Property getProperty() {
        return property;
    }
}
