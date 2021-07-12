package com.tenxerconsulting.swagger.doclet.parser;

import static com.tenxerconsulting.swagger.doclet.parser.ParserHelper.createRef;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.tenxerconsulting.swagger.doclet.DocletOptions;
import com.tenxerconsulting.swagger.doclet.model.*;
import com.tenxerconsulting.swagger.doclet.model.HttpMethod;
import com.tenxerconsulting.swagger.doclet.translator.Translator;
import com.tenxerconsulting.swagger.doclet.translator.Translator.OptionalName;
import io.swagger.oas.models.media.*;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.models.responses.ApiResponse;
import io.swagger.oas.models.responses.ApiResponses;
import io.swagger.oas.models.security.Scopes;
import io.swagger.oas.models.security.SecurityRequirement;
import io.swagger.oas.models.security.SecurityScheme;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The ApiMethodParser represents a parser for resource methods
 * @version $Id$
 */
@Slf4j
public class ApiMethodParser {

	private static final Pattern GENERIC_RESPONSE_PATTERN = Pattern.compile("(.*)<(.*)>");

	// pattern that can match a code, a description and an optional response model type
	private static final Pattern[] RESPONSE_MESSAGE_PATTERNS = new Pattern[] { Pattern.compile("(\\d+)([^`]+)(`.*)?") };

	private Method parentMethod;
	private String parentPath;

	private final DocletOptions options;
	private final Translator translator;
	private final MethodDoc methodDoc;
	private final Set<ModelWrapper> models;
	private final HttpMethod httpMethod;
	private final Collection<ClassDoc> classes;
	private final String classDefaultErrorType;
	private final String methodDefaultErrorType;

	/**
	 * This creates a ApiMethodParser
	 * @param options
	 * @param parentPath
	 * @param methodDoc
	 * @param classes
	 * @param classDefaultErrorType
	 */
	public ApiMethodParser(DocletOptions options, String parentPath, MethodDoc methodDoc, Collection<ClassDoc> classes, String classDefaultErrorType) {
		this.options = options;
		this.translator = options.getTranslator();
		this.parentPath = parentPath;
		this.methodDoc = methodDoc;
		this.models = new LinkedHashSet<>();
		this.httpMethod = ParserHelper.resolveMethodHttpMethod(methodDoc, options);
		this.parentMethod = null;
		this.classDefaultErrorType = classDefaultErrorType;
		this.methodDefaultErrorType = ParserHelper.getInheritableTagValue(methodDoc, options.getDefaultErrorTypeTags(), options);
		this.classes = classes;
	}

	/**
	 * This creates a ApiMethodParser
	 * @param options
	 * @param parentMethod
	 * @param methodDoc
	 * @param classes
	 * @param classDefaultErrorType
	 */
	public ApiMethodParser(DocletOptions options, Method parentMethod, MethodDoc methodDoc, Collection<ClassDoc> classes, String classDefaultErrorType) {
		this(options, parentMethod.getPath(), methodDoc, classes, classDefaultErrorType);

		this.parentPath = parentMethod.getPath();
		this.parentMethod = parentMethod;

	}

	/**
	 * This parses a javadoc method doc and builds a pojo representation of it.
	 * @return The method with appropriate data set
	 */
	public Method parse() {
		String methodPath = ParserHelper.resolveMethodPath(this.methodDoc, this.options);
		if (this.httpMethod == null && methodPath.isEmpty()) {
			if (this.options.isLogDebug()) {
				log.debug("skipping method: {} as it has neither @Path nor a http method annotation", this.methodDoc.name());
			}
			return null;
		}

		// check if deprecated and exclude if set to do so
		boolean deprecated = false;
		if (ParserHelper.isInheritableDeprecated(this.methodDoc, this.options)) {
			if (this.options.isExcludeDeprecatedOperations()) {
				if (this.options.isLogDebug()) {
					log.debug("skipping method: {} as it is deprecated and configuration excludes deprecated methods", this.methodDoc.name());
				}
				return null;
			}
			deprecated = true;
		}

		// exclude if it has exclusion tags/annotations
		if (ParserHelper.hasInheritableTag(this.methodDoc, this.options.getExcludeOperationTags())) {
			if (this.options.isLogDebug()) {
				log.debug("skipping method: {} as it has an exclusion tag", this.methodDoc.name());
			}
			return null;
		}
		if (ParserHelper.hasInheritableAnnotation(this.methodDoc, this.options.getExcludeOperationAnnotations(), this.options)) {
			if (this.options.isLogDebug()) {
				log.debug("skipping method: {} as it has an exclusion annotation", this.methodDoc.name());
			}
			return null;
		}

		String path;
		if (this.parentPath.equals("/")) {
			path = methodPath;
		} else {
			path = this.parentPath + methodPath;
		}



		// ************************************
		// Produces & consumes
		// ************************************
		List<String> consumes = ParserHelper.getConsumes(this.methodDoc, this.options);
		List<String> produces = ParserHelper.getProduces(this.methodDoc, this.options);

		// Build input
		RequestBody requestBody = null;
		if (consumes != null && !consumes.isEmpty()) {
			requestBody = new RequestBody();

			Content content = new Content();
			requestBody.content(content);

			consumes.forEach(item -> {
				io.swagger.oas.models.media.MediaType mediaType = new io.swagger.oas.models.media.MediaType();

				if ("text/plain".equals(item)) {
					StringSchema schema = new StringSchema();
					mediaType.setSchema(schema);
				}

				// TODO :: how can we handle other mediaTypes ?

				content.addMediaType(item, mediaType);
			});
		}

		// build params
		ParametersAndBody parameters = this.generateParameters(consumes);

		ClassDoc[] viewClasses = ParserHelper.getInheritableJsonViews(this.methodDoc, this.options);

		// ************************************
		// Return type
		// ************************************
		Type returnType = this.methodDoc.returnType();
		// first check if its a wrapper type and if so replace with the wrapped type
                Type alternateReturnType = ApiModelParser.getReturnType(this.options, returnType);
                if (alternateReturnType != null) {
                        returnType = alternateReturnType;
                }

		OptionalName returnTypeOName = this.translator.typeName(returnType, this.options.isUseFullModelIds());

		String returnTypeName = returnTypeOName.value();
		String returnTypeFormat = returnTypeOName.getFormat();

		Type modelType = returnType;

		// now see if it is a collection if so the return type will be array and the
		// containerOf will be added to the model

		String returnTypeItemsRef = null;
		String returnTypeItemsType = null;
		String returnTypeItemsFormat = null;
		List<String> returnTypeItemsAllowableValues = null;
		Type containerOf = ParserHelper.getContainerType(returnType, null, this.classes);

		Map<String, Type> varsToTypes = new HashMap<String, Type>();

		// look for a custom return type, this is useful where we return a jaxrs Response in the method signature
		// but typically return a different object in its entity (such as for a 201 created response)
		String customReturnTypeName = ParserHelper.responseTypeTagOf(this.methodDoc, this.options);
		NameToType nameToType = readCustomReturnType(customReturnTypeName, viewClasses);
		if (nameToType != null) {
			returnTypeName = nameToType.returnTypeName;
			returnTypeFormat = nameToType.returnTypeFormat;
			returnType = nameToType.returnType;
			// set collection data
			if (nameToType.containerOf != null) {
				returnTypeName = "array";
				// its a model collection, add the container of type to the model
				modelType = nameToType.containerOf;
				returnTypeItemsRef = this.translator.typeName(nameToType.containerOf, this.options.isUseFullModelIds(), viewClasses).value();
			} else if (nameToType.containerOfPrimitiveType != null) {
				returnTypeName = "array";
				// its a primitive collection
				returnTypeItemsType = nameToType.containerOfPrimitiveType;
				returnTypeItemsFormat = nameToType.containerOfPrimitiveTypeFormat;
			} else {
				modelType = returnType;
				if (nameToType.varsToTypes != null) {
					varsToTypes.putAll(nameToType.varsToTypes);
				}
			}
		} else if (containerOf != null) {
			returnTypeName = "array";
			// its a collection, add the container of type to the model
			modelType = containerOf;
			returnTypeItemsAllowableValues = ParserHelper.getAllowableValues(containerOf.asClassDoc());
			if (returnTypeItemsAllowableValues != null) {
				returnTypeItemsType = "string";
			} else {
				// set the items type or ref
				if (ParserHelper.isPrimitive(containerOf, this.options)) {
					OptionalName oName = this.translator.typeName(containerOf, this.options.isUseFullModelIds());
					returnTypeItemsType = oName.value();
					returnTypeItemsFormat = oName.getFormat();
				} else {
					returnTypeItemsRef = this.translator.typeName(containerOf, this.options.isUseFullModelIds(), viewClasses).value();
				}
			}
		} else {
			// if its not a container then adjust the return type name for any views
			returnTypeOName = this.translator.typeName(returnType, this.options.isUseFullModelIds(), viewClasses);
			returnTypeName = returnTypeOName.value();
			returnTypeFormat = returnTypeOName.getFormat();

			// add parameterized types to the model
			// TODO: support variables e.g. for inherited or sub resources
			addParameterizedModelTypes(returnType, varsToTypes, viewClasses);
		}

		// read extra details for the return type
		FieldReader returnTypeReader = new FieldReader(this.options);

		// set enum values
		List<String> returnTypeAllowableValues = null;
		if (returnType != null) {
			returnTypeAllowableValues = ParserHelper.getAllowableValues(returnType.asClassDoc());
		}

		String tagFormat = returnTypeReader.getFieldFormatValue(this.methodDoc, returnType);
		if (tagFormat != null) {
			returnTypeFormat = tagFormat;
		}

		// Process all models to attach
		if (modelType != null && this.options.isParseModels()) {
			this.models.addAll(new ApiModelParser(this.options, this.translator, modelType, viewClasses, this.classes).addVarsToTypes(varsToTypes).parse());
		}

		Schema schema;
		if (returnType != null && returnTypeName.equals("array")) {
			Map<SchemaBuilder.PropertyId, Object> args = new HashMap<>();
			args.put(SchemaBuilder.PropertyId.ENUM, returnTypeItemsAllowableValues);
			args.put(SchemaBuilder.PropertyId.UNIQUE_ITEMS, false);

			Schema items = ParserHelper.buildItems(
					returnTypeItemsRef,
					returnTypeItemsType,
					returnTypeItemsFormat,
					args
			);

			ArraySchema arrayProperty = new ArraySchema();
			arrayProperty.setItems(items);
			if (ParserHelper.isSet(returnType.qualifiedTypeName())) {
				arrayProperty.setUniqueItems(true);
			}

			schema = arrayProperty;
		} else if(returnTypeName.equals("array")) {
			Map<SchemaBuilder.PropertyId, Object> args = new HashMap<>();
			args.put(SchemaBuilder.PropertyId.ENUM, returnTypeItemsAllowableValues);
			args.put(SchemaBuilder.PropertyId.UNIQUE_ITEMS, false);

			Schema items = ParserHelper.buildItems(
					returnTypeItemsRef,
					returnTypeItemsType,
					returnTypeItemsFormat,
					args
			);

			ArraySchema arrayProperty = new ArraySchema();
			arrayProperty.setItems(items);

			schema = arrayProperty;
		} else if("void".equals(returnTypeName)) {
			// Doing nothing with void types
			// TODO :: should this be handled at a separate place ?
			schema = null;
		} else {
			Map<SchemaBuilder.PropertyId, Object> args = new HashMap<>();
			args.put(SchemaBuilder.PropertyId.MINIMUM, returnTypeReader.getFieldMin(this.methodDoc, returnType));
			args.put(SchemaBuilder.PropertyId.MAXIMUM, returnTypeReader.getFieldMax(this.methodDoc, returnType));
			args.put(SchemaBuilder.PropertyId.DEFAULT, returnTypeReader.getFieldDefaultValue(this.methodDoc, returnType));
			args.put(SchemaBuilder.PropertyId.ENUM, returnTypeAllowableValues);

			schema = SchemaBuilder.build(returnTypeName, returnTypeFormat, args);

			if (schema == null) {
				// If we have a version of this model with a ___discriminatorResponse, use it instead
				String finalReturnTypeName = returnTypeName;

				if (this.models.stream().anyMatch(wrapper -> wrapper.getName().equals(finalReturnTypeName + "___discriminatorResponse"))) {
					returnTypeName += "___discriminatorResponse";
				}
				schema = createRef(returnTypeName);
			}
		}



		// ************************************
		// Response messages
		// ************************************

		ApiResponses responseMap = generateResponseMap(viewClasses, produces, schema);

		// ************************************
		// Summary and notes
		// ************************************
		// First Sentence of Javadoc method description
		String firstSentences = ParserHelper.getInheritableFirstSentenceTags(this.methodDoc);

		// default plugin behaviour
		String summary = firstSentences == null ? "" : firstSentences;
		String notes = ParserHelper.getInheritableCommentText(this.methodDoc);
		if (notes == null) {
			notes = "";
		}
		notes = notes.replace(summary, "").trim();

		// look for custom notes/summary tags to use instead
		String customNotes = ParserHelper.getInheritableTagValue(this.methodDoc, this.options.getOperationNotesTags(), this.options);
		if (customNotes != null) {
			notes = customNotes;
		}
		String customSummary = ParserHelper.getInheritableTagValue(this.methodDoc, this.options.getOperationSummaryTags(), this.options);
		if (customSummary != null) {
			summary = customSummary;
		}
		summary = this.options.replaceVars(summary);
		notes = this.options.replaceVars(notes);

		// Auth support
		List<SecurityRequirement> security = generateSecurityRequirements();

		if (this.options.isLogDebug()) {
			log.debug("Finished parsing method: {}", this.methodDoc.name());
		}

		// final result!
		return new Method(this.httpMethod, this.methodDoc.name(), path, parameters.getParameters(), summary, notes,
				responseMap, consumes, produces, security, deprecated, parameters.getBody() != null ? parameters.getBody() : requestBody);
	}

	private List<SecurityRequirement> generateSecurityRequirements() {
		// build map of scopes from the api auth
		// For each oauth security scheme we collect the scopes
		// v scheme name
		Map<String, Scopes> apiScopes = new HashMap<>();
		if (this.options.getSecuritySchemes() != null) {
			this.options.getSecuritySchemes()
					.entrySet()
					.stream()
					.filter(scheme -> scheme.getValue().getType().equals(SecurityScheme.Type.OAUTH2)
							&& scheme.getValue().getFlows() != null
							&& scheme.getValue().getFlows().getImplicit() != null
							&& scheme.getValue().getFlows().getImplicit().getScopes() != null)
					.forEach(scheme -> {
						apiScopes.put(scheme.getKey(), scheme.getValue().getFlows().getImplicit().getScopes());
					});
		}

		// see if method has a tag that implies there is no authentication
		// in this case set the authentication object to {} to indicate we override
		// at the operation level
		// a) if method has an explicit unauth tag
		if (ParserHelper.hasInheritableTag(this.methodDoc, this.options.getUnauthOperationTags())) {
			return new ArrayList<>(Collections.singleton(new SecurityRequirement()));
		}

		List<SecurityRequirement> securityRequirements = new ArrayList<>();

		// otherwise if method has scope tags then add those to indicate method requires scope
		List<String> scopeValues = ParserHelper.getInheritableTagValues(this.methodDoc, this.options.getOperationScopeTags(), this.options);
		if (scopeValues != null) {
			for (String scopeVal : scopeValues) {
				addScopes(apiScopes, securityRequirements, scopeVal);
			}
		}

		// if not scopes see if its auth and whether we need to add default scope to it
		if (scopeValues == null || scopeValues.isEmpty()) {
			// b) if method has an auth tag that starts with one of the known values that indicates whether auth required.
			String authSpec = ParserHelper.getInheritableTagValue(this.methodDoc, this.options.getAuthOperationTags(), this.options);
			if (authSpec != null) {

				boolean unauthFound = false;
				for (String unauthValue : this.options.getUnauthOperationTagValues()) {
					if (authSpec.toLowerCase().startsWith(unauthValue.toLowerCase())) {
						securityRequirements.add(new SecurityRequirement());
						unauthFound = true;
						break;
					}
				}

				if (!unauthFound) {
					// its deemed to require authentication, however there is no explicit scope so we need to use
					// the default scopes
					List<String> defaultScopes = this.options.getAuthOperationScopes();
					if (defaultScopes != null && !defaultScopes.isEmpty()) {
						for (String scopeVal : defaultScopes) {
							addScopes(apiScopes, securityRequirements, scopeVal);
						}
					}
				}
			}
		}

		if (securityRequirements.isEmpty()) {
			return null;
		}

		return securityRequirements;
	}

	private void addScopes(Map<String, Scopes> apiScopes, List<SecurityRequirement> securityRequirements, String scopeVal) {
		boolean foundScope = false;

		// Each security scheme has some scopes
		// Check each security scheme, create a security requirement named
		// after them if they contain the scope we're looking for
		for (Map.Entry<String, Scopes> securityScheme : apiScopes.entrySet()) {
			if (!securityScheme.getValue().containsKey(scopeVal)) {
				continue;
			}
			foundScope = true;

			String securitySchemeName = securityScheme.getKey();

			AtomicReference<Boolean> requirementExists = new AtomicReference<>(true);

			// Get or create the SecurityRequirement for the current scope
			SecurityRequirement requirement = securityRequirements
					.stream()
					.filter(req -> req.containsKey(securitySchemeName))
					.findFirst()
					.orElseGet(() -> {
						requirementExists.set(false);
						SecurityRequirement sr = new SecurityRequirement();
						sr.put(securitySchemeName, new ArrayList<>());
						return sr;
					});

			// Only add the scope if it isn't already in
			List<String> srScopes = requirement.get(securitySchemeName);
			if (!srScopes.contains(scopeVal)) {
				srScopes.add(scopeVal);
			}

			if (!requirementExists.get()) {
				securityRequirements.add(requirement);
			}
		}
		if (!foundScope) {
			throw new IllegalStateException("The scope: " + scopeVal + " was referenced in the method: " + this.methodDoc
					+ " but this scope was not part of the API service.json level authorization object.");
		}
	}

	private ApiResponses generateResponseMap(ClassDoc[] viewClasses, List<String> produces, Schema defaultSchema) {
		ApiResponses responseMap = new ApiResponses();

		List<String> tagValues = ParserHelper.getInheritableTagValues(this.methodDoc, this.options.getResponseMessageTags(), this.options);

		boolean usedDefaultContent = false;

		ApiResponse defaultResponse = new ApiResponse();
		defaultResponse.setDescription("");

		if (defaultSchema != null) {
			Content defaultContent = new Content();
			io.swagger.oas.models.media.MediaType defaultMediaType = new io.swagger.oas.models.media.MediaType();
			defaultMediaType.setSchema(defaultSchema);
			(produces == null ? Collections.singletonList("*/*") : produces).forEach(item -> defaultContent.addMediaType(item, defaultMediaType));
			defaultResponse.setContent(defaultContent);
		}

		if (tagValues != null) {
			for (String tagValue : tagValues) {
				for (Pattern pattern : RESPONSE_MESSAGE_PATTERNS) {
					Matcher matcher = pattern.matcher(tagValue);
					if (matcher.find()) {
						int statusCode = Integer.parseInt(matcher.group(1).trim());
						// trim special chars the desc may start with
						String desc = ParserHelper.trimLeadingChars(matcher.group(2), '|', '-');
						if (desc == null) {
							desc = "";
						}

						// see if it has a custom response model
						String responseModelClass = null;
						if (matcher.groupCount() > 2) {
							responseModelClass = ParserHelper.trimLeadingChars(matcher.group(3), '`');
						}
						// If no custom one use the method level one if there is one
						if (statusCode >= 400) {
							if (responseModelClass == null) {
								responseModelClass = this.methodDefaultErrorType;
							}
							// If no custom one use the class level one if there is one
							if (responseModelClass == null) {
								responseModelClass = this.classDefaultErrorType;
							}
						}

						Schema schema = null;
						if (responseModelClass != null) {
							Type responseType = ParserHelper.findModel(this.classes, responseModelClass);
							if (responseType != null) {
								String responseModel = this.translator.typeName(responseType, this.options.isUseFullModelIds()).value();
								String finalResponseModel = responseModel;

								// If we have a version of this model with a ___discriminatorResponse, use it instead
								if (this.models.stream().anyMatch(wrapper -> wrapper.getName().equals(finalResponseModel + "___discriminatorResponse"))) {
									responseModel += "___discriminatorResponse";
								}
								schema = createRef(responseModel);
								if (this.options.isParseModels()) {
									this.models.addAll(new ApiModelParser(this.options, this.translator, responseType, viewClasses, this.classes).parse());
								}
							}
						}

						if (responseModelClass == null && statusCode >= 200 && statusCode < 300) {
							usedDefaultContent = true;
							schema = defaultSchema;
						}

						String stringStatusCode = String.valueOf(statusCode);

						if (responseMap.containsKey(stringStatusCode)) {
							ApiResponse apiResponse = responseMap.get(stringStatusCode);

							if (null != schema && null == apiResponse.getContent()) {
								apiResponse.setContent(createContent(produces, schema));
							}

							String description = apiResponse.getDescription();
							if (description == null || description.trim().length() == 0) {
								description = desc;
							} else {
								description += "<br>" + desc;
							}
							apiResponse.description(description);

						} else {
							ApiResponse apiResponse = new ApiResponse();
							apiResponse.description(desc);
							if (null != schema) {
								apiResponse.setContent(createContent(produces, schema));
							}

							responseMap.put(stringStatusCode, apiResponse);
						}
						break;
					}
				}
			}
		}

		// sort the response messages as required
		if (!responseMap.isEmpty() && this.options.getResponseMessageSortMode() != null) {
			Comparator<Map.Entry<String, ApiResponse>> comparator = null;

			switch (this.options.getResponseMessageSortMode()) {
				case CODE_ASC:
					comparator = Map.Entry.comparingByKey();
					break;
				case CODE_DESC:
					comparator = Collections.reverseOrder(Map.Entry.comparingByKey());
					break;
				case AS_APPEARS:
					// noop
					break;
				default:
					throw new UnsupportedOperationException("Unknown ResponseMessageSortMode: " + this.options.getResponseMessageSortMode());
			}

			if (comparator != null) {
				ApiResponses newMap = new ApiResponses();
				responseMap.entrySet()
						.stream()
						.sorted(comparator)
						.forEach(entry -> {
							newMap.put(entry.getKey(), entry.getValue());
						});

				responseMap = newMap;
			}
		}

		if (!usedDefaultContent) {
			responseMap.setDefault(defaultResponse);
		}

		return responseMap;
	}

	public Content createContent(List<String> types, Schema schema) {
		Content content = new Content();
		io.swagger.oas.models.media.MediaType mediaType = new io.swagger.oas.models.media.MediaType();
		mediaType.setSchema(schema);

		(types == null ? Collections.singletonList("*/*") : types).forEach(item -> content.addMediaType(item, mediaType));

		return content;
	}

	@Data
	static class Parameters {
		List<io.swagger.oas.models.parameters.Parameter> parameters;
		RequestBody body;
	}

	private ParametersAndBody generateParameters(List<String> consumes) {
		// parameters
		ParametersAndBody parameters = new ParametersAndBody();

		// read whether the method consumes multipart
		boolean consumesMultipart = consumes != null && consumes.contains(MediaType.MULTIPART_FORM_DATA);

		// get raw parameter names from method signature
		Set<String> rawParamNames = ParserHelper.getParamNames(this.methodDoc);

		// get full list including any beanparam parameter names
		Set<String> allParamNames = new HashSet<String>(rawParamNames);
		for (int paramIndex = 0; paramIndex < this.methodDoc.parameters().length; paramIndex++) {
			final Parameter parameter = ParserHelper.getParameterWithAnnotations(this.methodDoc, paramIndex);
			String paramCategory = ParserHelper.paramTypeOf(consumesMultipart, parameter, this.options);
			// see if its a special composite type e.g. @BeanParam
			if ("composite".equals(paramCategory)) {
				Type paramType = parameter.type();
				ApiModelParser modelParser = new ApiModelParser(this.options, this.translator, paramType, consumesMultipart, true);
				Set<ModelWrapper> models = modelParser.parse();
				String rootModelId = modelParser.getRootModelId();
				for (ModelWrapper<?> modelWrapper : models) {
					if (rootModelId.equals(modelWrapper.getName())) {
						for (PropertyWrapper entry : modelWrapper.getProperties().values()) {
							String rawFieldName = entry.getRawFieldName();
							allParamNames.add(rawFieldName);
						}
					}
				}
			}
		}

		// read exclude params
		List<String> excludeParams = ParserHelper.getCsvParams(this.methodDoc, allParamNames, this.options.getExcludeParamsTags(), this.options);

		ParameterReader paramReader = new ParameterReader(this.options, this.classes);
		paramReader.readClass(this.methodDoc.containingClass());

		Set<String> addedParamNames = new HashSet<String>();
		Set<FormItem> formItems = new HashSet<>();

		// build params from the method's params
		for (int paramIndex = 0; paramIndex < this.methodDoc.parameters().length; paramIndex++) {
			final Parameter parameter = ParserHelper.getParameterWithAnnotations(this.methodDoc, paramIndex);
			if (!shouldIncludeParameter(this.httpMethod, excludeParams, parameter)) {
				continue;
			}

			List<ParameterOrBody> apiParams = paramReader.buildApiParams(this.methodDoc, parameter, consumesMultipart, allParamNames, this.models, consumes);
			addUniqueParam(addedParamNames, formItems, apiParams, parameters);
		}

		// add any parent method parameters that are inherited
		if (this.parentMethod != null) {
			List<ParameterOrBody> parentParameters = new ArrayList();
			if (parentMethod.getRequestBody() != null) {
				parentParameters.add(new ParameterOrBody(parentMethod.getRequestBody()));
			}
			parentParameters.addAll(this.parentMethod.getApiParameters().stream().map(ParameterOrBody::new).collect(Collectors.toList()));
			addUniqueParam(addedParamNames, formItems, parentParameters, parameters);
		}

		// add class level parameters
		List<ParameterOrBody> classLevelParams = paramReader.readClassLevelParameters(this.models, consumes);
		addUniqueParam(addedParamNames, formItems, classLevelParams, parameters);

		// add on any implicit params
		List<ParameterOrBody> implicitParams = paramReader.readImplicitParameters(this.methodDoc, this.models, consumes);
		addUniqueParam(addedParamNames, formItems, implicitParams, parameters);

		// Take all collected form items, and group them in a single RequestBody
		if (!formItems.isEmpty()) {
			ObjectSchema schema = createFormSchema(formItems);
			ParameterOrBody param = paramReader.buildForm(schema, schema.getRequired() != null, null, consumes);
			addUniqueParam(addedParamNames, null, Collections.singletonList(param), parameters);
		}

		return parameters;
	}

	private ObjectSchema createFormSchema(Set<FormItem> formItems) {
		ObjectSchema schema = new ObjectSchema();
		List<String> required = new ArrayList<>();
		formItems.forEach(item -> {
			schema.addProperties(item.getName(), item.getSchema());

			if (Boolean.TRUE.equals(item.getRequired())) {
				required.add(item.getName());
			}
		});

		if (!required.isEmpty()) {
			schema.required(required);
		}

		return schema;
	}

	private void addUniqueParam(Set<String> addedParamNames, Set<FormItem> formItems, List<ParameterOrBody> paramsToAdd, ParametersAndBody targetList) {
		if (paramsToAdd == null) {
			return;
		}

		for (ParameterOrBody apiParam : paramsToAdd) {
                        if (apiParam.getParameter() != null && !addedParamNames.contains(apiParam.getParameter().getName())) {
                                addedParamNames.add(apiParam.getParameter().getName());
                                targetList.getParameters().add(apiParam.getParameter());
			}

			if (apiParam.getBody() != null) {
				if (targetList.getBody() != null) {
					try {
						ObjectMapper mapper = this.options.getMapper();
						log.debug(
								"This method seems to have two bodies \nBefore: {}, \nAfter: {}",
								mapper.writeValueAsString(targetList.getBody()),
								mapper.writeValueAsString(apiParam.getBody())
						);
					} catch (JsonProcessingException e) {
						log.debug(
								"This method seems to have two bodies \nBefore: {}, \nAfter: {}",
								targetList.getBody(),
								apiParam.getBody()
						);
					}
				}
				targetList.setBody(apiParam.getBody());
			}

			if (apiParam.getFormItem() != null) {
				formItems.add(apiParam.getFormItem());
			}
		}

	}

	/**
	 * This gets the parsed models found for this method
	 * @return the set of parsed models found for this method
	 */
	public Set<ModelWrapper> models() {
		return this.models;
	}

	static class NameToType {
		Type returnType;
		Type containerOf;
		String containerOfPrimitiveType;
		String containerOfPrimitiveTypeFormat;
		String returnTypeName;
		String returnTypeFormat;
		Map<String, Type> varsToTypes;
	}

	// TODO refactor building type details from a string into a common class for reuse
	// across various parts of the doclet
	NameToType readCustomReturnType(String customTypeName, ClassDoc[] viewClasses) {
		if (customTypeName != null && customTypeName.trim().length() > 0) {
			customTypeName = customTypeName.trim();

			Type[] paramTypes = null;
			Type customType = null;

			boolean useFqn = this.options.isUseFullModelIds();

			// split it into container and container of, if its in the form X<Y>
			Matcher matcher = GENERIC_RESPONSE_PATTERN.matcher(customTypeName);
			if (matcher.find()) {
				customTypeName = matcher.group(1);
				if (ParserHelper.isCollection(customTypeName)) {
					String containerOfType = matcher.group(2);
					Type containerOf = null;
					String containerOfPrimitiveType = null;
					String containerOfPrimitiveTypeFormat = null;
					if (ParserHelper.isPrimitive(containerOfType, this.options)) {
						String[] typeFormat = ParserHelper.typeOf(containerOfType, useFqn, this.options);
						containerOfPrimitiveType = typeFormat[0];
						containerOfPrimitiveTypeFormat = typeFormat[1];
					} else {
						containerOf = ParserHelper.findModel(this.classes, containerOfType);
						if (containerOf == null) {
							raiseCustomTypeNotFoundError(containerOfType);
						}
					}

					NameToType res = new NameToType();
					String[] nameFormat = ParserHelper.typeOf(customTypeName, useFqn, this.options);
					res.returnTypeName = nameFormat[0];
					res.returnTypeFormat = nameFormat[1];
					res.returnType = null;
					res.containerOf = containerOf;
					res.containerOfPrimitiveType = containerOfPrimitiveType;
					res.containerOfPrimitiveTypeFormat = containerOfPrimitiveTypeFormat;
					return res;
				} else if (ParserHelper.isMap(customTypeName)) {
					NameToType res = new NameToType();
					String[] nameFormat = ParserHelper.typeOf(customTypeName, useFqn, this.options);
					res.returnTypeName = nameFormat[0];
					res.returnTypeFormat = nameFormat[1];
					res.returnType = null;
					return res;
				} else {
					// its a parameterized type, add the parameterized classes to the model
					String[] paramTypeNames = matcher.group(2).split(",");
					paramTypes = new Type[paramTypeNames.length];
					int i = 0;
					for (String paramTypeName : paramTypeNames) {
						paramTypes[i] = ParserHelper.findModel(this.classes, paramTypeName);
						i++;
					}
				}
			}

			// lookup the type from the doclet classes
			customType = ParserHelper.findModel(this.classes, customTypeName);
			if (customType == null) {
			    if (this.options.isLogDebug()) {
			    	log.debug("Warning: couldn't find model for customType {}", customTypeName);
				}
			} else {
                                Type alternateCustomType = ApiModelParser.getReturnType(this.options, customType);
                                if (alternateCustomType != null) {
                                        customType = alternateCustomType;
                                }

				// build map of var names to parameters if applicable
				Map<String, Type> varsToTypes = null;
				if (paramTypes != null) {
					varsToTypes = new HashMap<String, Type>();
					TypeVariable[] vars = customType.asClassDoc().typeParameters();
					int i = 0;
					for (TypeVariable var : vars) {
						varsToTypes.put(var.qualifiedTypeName(), paramTypes[i]);
						i++;
					}
					// add param types to the model
					for (Type type : paramTypes) {
						if (this.classes.contains(type)) {
							if (this.options.isParseModels()) {
								this.models.addAll(new ApiModelParser(this.options, this.translator, type, viewClasses, this.classes).addVarsToTypes(
										varsToTypes).parse());
							}
						}
					}
				}

				OptionalName translated = this.translator.typeName(customType, this.options.isUseFullModelIds(), viewClasses);
				if (translated != null && translated.value() != null) {
					NameToType res = new NameToType();
					res.returnTypeName = translated.value();
					res.returnTypeFormat = translated.getFormat();
					res.returnType = customType;
					res.varsToTypes = varsToTypes;
					return res;
				}
			}
		}
		return null;
	}

	private void addParameterizedModelTypes(Type returnType, Map<String, Type> varsToTypes, ClassDoc[] viewClasses) {
		// TODO support variable types e.g. parameterize sub resources or inherited resources
		List<Type> parameterizedTypes = ParserHelper.getParameterizedTypes(returnType, varsToTypes);
		for (Type type : parameterizedTypes) {
			if (this.classes.contains(type)) {
				if (this.options.isParseModels()) {
					this.models.addAll(new ApiModelParser(this.options, this.translator, type, viewClasses, null).addVarsToTypes(varsToTypes).parse());
				}
			}
		}
	}

	static void raiseCustomTypeNotFoundError(String customType) {
		throw new IllegalStateException(
				"Could not find the source for the custom response class: "
						+ customType
						+ ". If it is not in the same project as the one you have added the doclet to, "
						+ "for example if it is in a dependent project then you should copy the source to the doclet calling project using the maven-dependency-plugin's unpack goal,"
						+ " and then add it to the source using the build-helper-maven-plugin's add-source goal.");
	}

	private boolean shouldIncludeParameter(HttpMethod httpMethod, List<String> excludeParams, Parameter parameter) {
		List<AnnotationDesc> allAnnotations = Arrays.asList(parameter.annotations());

		// remove any params annotated with exclude param annotations e.g. jaxrs Context
		if (ParserHelper.hasAnnotation(parameter, this.options.getExcludeParamAnnotations(), this.options)) {
			return false;
		}

		// remove any params with exclude param tags
		if (excludeParams != null && excludeParams.contains(parameter.name())) {
			return false;
		}

		// remove any deprecated params
		if (this.options.isExcludeDeprecatedParams() && ParserHelper.isDeprecated(parameter, this.options)) {
			return false;
		}

        // include if it has a jaxrs annotation
        if (ParserHelper.hasJaxRsAnnotation(parameter, this.options)) {
            return true;
        }

        // include if it has a Spring MVC annotation
        if (ParserHelper.hasSpringMvcAnnotation(parameter, this.options)) {
            return true;
        }

		// include if there are either no annotations or its a put/post/patch
		// this means for GET/HEAD/OPTIONS we don't include if it has some non jaxrs annotation on it
		return (allAnnotations.isEmpty() || httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH);
	}

}
