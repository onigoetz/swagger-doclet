package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.models.Model;

import java.util.List;
import java.util.Map;

/**
 * @author RJ Ewing
 */
public class ModelWrapper {

    private Map<String, PropertyWrapper> properties;
    private Model model;

    public ModelWrapper(Model model, Map<String, PropertyWrapper> properties) {
        this.properties = properties;
        this.model = model;
    }

    public Map<String, PropertyWrapper> getProperties() {
        return properties;
    }

    public Model getModel() {
        return model;
    }
}
