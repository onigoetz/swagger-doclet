package com.tenxerconsulting.swagger.doclet.model;

import java.util.List;
import java.util.Map;

import io.swagger.oas.models.info.Info;
import io.swagger.oas.models.tags.Tag;
import lombok.*;

@NoArgsConstructor
@Data
public class ResourceListing {
	private String openapi;
	private String basePath;
	private List<Tag> tags = null;
	private Map<String, ResourceListingAPI> paths;
	private Info info;
}
