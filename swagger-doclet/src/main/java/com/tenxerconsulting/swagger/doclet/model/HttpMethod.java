package com.tenxerconsulting.swagger.doclet.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.parser.Parser;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.parser.ParserHelper;

public enum HttpMethod {

    // JAX-RS
	GET("javax.ws.rs.GET", "org.springframework.web.bind.annotation.RequestMethod.GET"),
	PUT("javax.ws.rs.PUT", "org.springframework.web.bind.annotation.RequestMethod.PUT"),
	POST("javax.ws.rs.POST", "org.springframework.web.bind.annotation.RequestMethod.POST"),
	DELETE("javax.ws.rs.DELETE", "org.springframework.web.bind.annotation.RequestMethod.DELETE"),
	HEAD("javax.ws.rs.HEAD", "org.springframework.web.bind.annotation.RequestMethod.HEAD"),
	OPTIONS("javax.ws.rs.OPTIONS", "org.springframework.web.bind.annotation.RequestMethod.OPTIONS"),
    PATCH(".PATCH", ".PATCH", true);

	// NOTE Patch is not part of JAXRS 1 or 2 as it stands (people can add it but it will have an arbitrary package)
	// so we will look for any annotation ending in .PATCH

	private final String className;
	private final String springMvcClassName;
	private final boolean useContains;

	private HttpMethod(String className, String springMvcClassName) {
		this.className = className;
		this.springMvcClassName = springMvcClassName;
		this.useContains = false;
	}

	private HttpMethod(String className, String springMvcClassName, boolean useContains) {
		this.className = className;
        this.springMvcClassName = springMvcClassName;
		this.useContains = useContains;
	}

	/**
	 * This finds a HTTP method for the given method
	 * @param method The java method to check
	 * @return The HTTP method or null if there is not HTTP method annotation on the java method
	 */
	public static HttpMethod fromMethod(ExecutableMemberDoc method, DocletOptions options) {
		for (AnnotationDesc annotation : method.annotations()) {
			String qName = annotation.annotationType().qualifiedTypeName();
			
			for (HttpMethod value : HttpMethod.values()) {
				if (value.useContains && qName.contains(value.className)) {
					return value;
				} else if (!value.useContains && qName.equals(value.className)) {
					return value;
				}
			}
			
			if (qName.equals("org.springframework.web.bind.annotation.RequestMapping")) {
			    List<String> methods = ParserHelper.listInheritableValues(method, ParserHelper.SPRING_MVC_REQUEST_MAPPING, "method", options);
			    
			    if (methods == null) {
			        methods = ParserHelper.listValues(method.containingClass(), ParserHelper.SPRING_MVC_REQUEST_MAPPING, "method", options);   
			    }
			    
			    if (methods != null && methods.size() > 0) {
			        // FIXME: Spring MVC supports multile methods
			        String m = methods.get(0);
			    
		            for (HttpMethod value : HttpMethod.values()) {
		                if (value.useContains && m.contains(value.springMvcClassName)) {
		                    return value;
		                } else if (!value.useContains && m.equals(value.springMvcClassName)) {
		                    return value;
		                }
		            }
			    }
			}
		}
		
		return null;
	}
}
