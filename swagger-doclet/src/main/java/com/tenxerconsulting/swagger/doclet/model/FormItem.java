package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.oas.models.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FormItem {
    String name;
    Schema schema;
    Boolean required;
}
