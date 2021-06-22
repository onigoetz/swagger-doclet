package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.oas.models.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author RJ Ewing
 */
@Data
@AllArgsConstructor
public class TagWrapper {
    private Tag tag;
    private int priority = Integer.MAX_VALUE;
}
