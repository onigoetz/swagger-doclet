package com.tenxerconsulting.swagger.doclet;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;

import javax.ws.rs.core.Application;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.tenxerconsulting.swagger.doclet.json.MapperModule;
import com.tenxerconsulting.swagger.doclet.parser.NamingConvention;
import com.tenxerconsulting.swagger.doclet.parser.ParserHelper;
import com.tenxerconsulting.swagger.doclet.parser.ResponseMessageSortMode;
import com.tenxerconsulting.swagger.doclet.parser.VariableReplacer;
import com.tenxerconsulting.swagger.doclet.translator.AnnotationAwareTranslator;
import com.tenxerconsulting.swagger.doclet.translator.FirstNotNullTranslator;
import com.tenxerconsulting.swagger.doclet.translator.NameBasedTranslator;
import com.tenxerconsulting.swagger.doclet.translator.Translator;
import io.swagger.oas.models.info.Info;
import io.swagger.oas.models.security.SecurityScheme;
import lombok.Getter;
import lombok.Setter;

/**
 * The DocletOptions represents the supported options for this doclet.
 * @version $Id$
 */
@SuppressWarnings("javadoc")
public class DocletOptions {

	private static <T> T loadModelFromJson(String option, String path, Class<T> resourceClass) {
		File file = new File(path);
                if (!file.isFile()) {
                        throw new IllegalArgumentException("Path for " + option + " (" + file.getAbsolutePath() + ") is expected to be an existing file!");
                }

		// load it as json and build the object from it
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new MapperModule());
			return mapper.readValue(is, resourceClass);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to read model file: " + path + ", error : " + ex.getMessage(), ex);
		}
	}

	private static <T> T loadModelFromJson(String option, String path, MapType resourceClass) {
		File file = new File(path);
                if (!file.isFile()) {
                        throw new IllegalArgumentException("Path for " + option + " (" + file.getAbsolutePath() + ") is expected to be an existing file!");
                }
		// load it as json and build the object from it
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new MapperModule());
			return mapper.readValue(is, resourceClass);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to read model file: " + path + ", error : " + ex.getMessage(), ex);
		}
	}

	/**
	 * This parses doclet options
	 * @param options The cmdline options
	 * @return The parse options
	 */
	public static DocletOptions parse(String[][] options) {

		DocletOptions parsedOptions = new DocletOptions();

		boolean clearedDefaultServers = false;

		// Object mapper settings
		String serializationFeaturesCsv = null;
		String deserializationFeaturesCsv = null;
		String defaultTyping = null;
		String serializationInclusion = null;

		for (String[] option : options) {
			if (option[0].equals("-d")) {
				parsedOptions.outputDirectory = new File(option[1]);
				if (!parsedOptions.outputDirectory.exists()) {
					boolean created = parsedOptions.outputDirectory.mkdirs();
					if (!created) {
						throw new IllegalArgumentException("Path after -d is expected to be a directory!");
					}
				}
			} else if (option[0].equals("-securitySchemesFile")) {
				MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, SecurityScheme.class);
				parsedOptions.securitySchemes = loadModelFromJson("-securitySchemesFile", option[1], type);
			} else if (option[0].equals("-apiInfoFile")) {
				parsedOptions.apiInfo = loadModelFromJson("-apiInfoFile", option[1], Info.class);

			} else if (option[0].equals("-variablesPropertiesFile")) {

				File varFile = new File(option[1]);
				if (!varFile.exists() && !varFile.canRead()) {
					throw new IllegalStateException("Unable to read variables file: " + varFile.getAbsolutePath() + " check it exists and is readable.");
				}
				Properties props = new Properties();
                                try (InputStream is = new FileInputStream(varFile)) {
					props.load(is);
					parsedOptions.variableReplacements = props;
				} catch (IOException ex) {
					throw new IllegalStateException("Failed to read variables file: " + varFile.getAbsolutePath(), ex);
				}
			} else if (option[0].equals("-servers")) {
				if (!clearedDefaultServers) {
					parsedOptions.servers.clear();
					clearedDefaultServers = true;
				}
				parsedOptions.servers.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-docBasePath")) {
				parsedOptions.docBasePath = option[1];
			} else if (option[0].equals("-apiVersion")) {
				parsedOptions.apiVersion = option[1];
			} else if (option[0].equals("-swaggerUiZipPath") || option[0].equals("-swaggerUiPath")) {
				parsedOptions.swaggerUiPath = option[1];
			} else if (option[0].equals("-responseMessageSortMode")) {
				parsedOptions.responseMessageSortMode = ResponseMessageSortMode.valueOf(option[1]);
			} else if (option[0].equals("-disableModels")) {
				parsedOptions.parseModels = false;
			} else if (option[0].equals("-useFullModelIds")) {
				parsedOptions.useFullModelIds = true;
			} else if (option[0].equals("-logDebug")) {
				parsedOptions.logDebug = true;
			} else if (option[0].equals("-modelFieldsRequiredByDefault")) {
				parsedOptions.modelFieldsRequiredByDefault = true;
			} else if (option[0].equals("-disableModelFieldsXmlAccessType")) {
				parsedOptions.modelFieldsXmlAccessTypeEnabled = false;
			} else if (option[0].equals("-defaultModelFieldsXmlAccessType")) {
				parsedOptions.modelFieldsDefaultXmlAccessTypeEnabled = true;
			} else if (option[0].equals("-modelFieldsNamingConvention")) {
				parsedOptions.modelFieldsNamingConvention = NamingConvention.forValue(option[1], NamingConvention.DEFAULT_NAME);
			} else if (option[0].equals("-disableCopySwaggerUi") || option[0].equals("-skipUiFiles")) {
				parsedOptions.includeSwaggerUi = false;
			} else if (option[0].equals("-disableSortApisByPath")) {
				parsedOptions.sortApisByPath = false;
			} else if (option[0].equals("-sortResourcesByPath")) {
				parsedOptions.sortResourcesByPath = true;
			} else if (option[0].equals("-sortResourcesByPriority")) {
				parsedOptions.sortResourcesByPriority = true;
			} else if (option[0].equals("-disableDeprecatedOperationExclusion")) {
				parsedOptions.excludeDeprecatedOperations = false;
			} else if (option[0].equals("-disableDeprecatedFieldExclusion")) {
				parsedOptions.excludeDeprecatedFields = false;
			} else if (option[0].equals("-disableDeprecatedParamExclusion")) {
				parsedOptions.excludeDeprecatedParams = false;
			} else if (option[0].equals("-disableDeprecatedResourceClassExclusion")) {
				parsedOptions.excludeDeprecatedResourceClasses = false;
			} else if (option[0].equals("-disableDeprecatedModelClassExclusion")) {
				parsedOptions.excludeDeprecatedModelClasses = false;

			} else if (option[0].equals("-profileMode")) {
				parsedOptions.profileMode = true;

			} else if (option[0].equals("-excludeModelPrefixes") || option[0].equals("-typesToTreatAsOpaque")) {
				parsedOptions.excludeModelPrefixes.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-excludeResourcePrefixes")) {
				parsedOptions.excludeResourcePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-includeResourcePrefixes")) {
				parsedOptions.includeResourcePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-genericWrapperTypes")) {
				parsedOptions.genericWrapperTypes.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-fileParameterAnnotations")) {
				parsedOptions.fileParameterAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-fileParameterTypes")) {
				parsedOptions.fileParameterTypes.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-formParameterAnnotations")) {
				parsedOptions.formParameterAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-formParameterTypes")) {
				parsedOptions.formParameterTypes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-discriminatorAnnotations")) {
				parsedOptions.discriminatorAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-subTypesAnnotations")) {
				parsedOptions.subTypesAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-compositeParamAnnotations")) {
				parsedOptions.compositeParamAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-compositeParamTypes")) {
				parsedOptions.compositeParamTypes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-parameterNameAnnotations")) {
				parsedOptions.parameterNameAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-longTypePrefixes")) {
				parsedOptions.longTypePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-intTypePrefixes")) {
				parsedOptions.intTypePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-floatTypePrefixes")) {
				parsedOptions.floatTypePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-doubleTypePrefixes")) {
				parsedOptions.doubleTypePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-stringTypePrefixes")) {
				parsedOptions.stringTypePrefixes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-responseMessageTags")) {
				addTagsOption(parsedOptions.responseMessageTags, option);

			} else if (option[0].equals("-excludeClassTags")) {
				addTagsOption(parsedOptions.excludeClassTags, option);

			} else if (option[0].equals("-excludeClassAnnotations")) {
				addTagsOption(parsedOptions.excludeClassAnnotations, option);

			} else if (option[0].equals("-excludeOperationTags")) {
				addTagsOption(parsedOptions.excludeOperationTags, option);

			} else if (option[0].equals("-excludeOperationAnnotations")) {
				addTagsOption(parsedOptions.excludeOperationAnnotations, option);

			} else if (option[0].equals("-excludeFieldTags")) {
				addTagsOption(parsedOptions.excludeFieldTags, option);

			} else if (option[0].equals("-excludeFieldAnnotations")) {
				addTagsOption(parsedOptions.excludeFieldAnnotations, option);

			} else if (option[0].equals("-excludeParamsTags")) {
				addTagsOption(parsedOptions.excludeParamsTags, option);

			} else if (option[0].equals("-excludeParamAnnotations")) {
				parsedOptions.excludeParamAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-csvParamsTags")) {
				addTagsOption(parsedOptions.csvParamsTags, option);

			} else if (option[0].equals("-implicitParamTags")) {
				addTagsOption(parsedOptions.implicitParamTags, option);

			} else if (option[0].equals("-paramsFormatTags")) {
				addTagsOption(parsedOptions.paramsFormatTags, option);

			} else if (option[0].equals("-paramsMinValueTags")) {
				addTagsOption(parsedOptions.paramsMinValueTags, option);

			} else if (option[0].equals("-paramsMaxValueTags")) {
				addTagsOption(parsedOptions.paramsMaxValueTags, option);

			} else if (option[0].equals("-paramsDefaultValueTags")) {
				addTagsOption(parsedOptions.paramsDefaultValueTags, option);

			} else if (option[0].equals("-paramsAllowableValuesTags")) {
				addTagsOption(parsedOptions.paramsAllowableValuesTags, option);

			} else if (option[0].equals("-paramsNameTags")) {
				addTagsOption(parsedOptions.paramsNameTags, option);

			} else if (option[0].equals("-resourceTags")) {
				addTagsOption(parsedOptions.resourceTags, option);

			} else if (option[0].equals("-responseTypeTags")) {
				addTagsOption(parsedOptions.responseTypeTags, option);

			} else if (option[0].equals("-inputTypeTags")) {
				addTagsOption(parsedOptions.inputTypeTags, option);

			} else if (option[0].equals("-defaultErrorTypeTags")) {
				addTagsOption(parsedOptions.defaultErrorTypeTags, option);

			} else if (option[0].equals("-apiDescriptionTags")) {
				addTagsOption(parsedOptions.apiDescriptionTags, option);

			} else if (option[0].equals("-operationNotesTags")) {
				addTagsOption(parsedOptions.operationNotesTags, option);

			} else if (option[0].equals("-operationSummaryTags")) {
				addTagsOption(parsedOptions.operationSummaryTags, option);

			} else if (option[0].equals("-fieldDescriptionTags")) {
				addTagsOption(parsedOptions.fieldDescriptionTags, option);

			} else if (option[0].equals("-fieldFormatTags")) {
				addTagsOption(parsedOptions.fieldFormatTags, option);

			} else if (option[0].equals("-fieldMinTags")) {
				addTagsOption(parsedOptions.fieldMinTags, option);

			} else if (option[0].equals("-fieldMaxTags")) {
				addTagsOption(parsedOptions.fieldMaxTags, option);

			} else if (option[0].equals("-fieldDefaultTags")) {
				addTagsOption(parsedOptions.fieldDefaultTags, option);

			} else if (option[0].equals("-fieldAllowableValuesTags")) {
				addTagsOption(parsedOptions.fieldAllowableValuesTags, option);

			} else if (option[0].equals("-requiredParamsTags")) {
				addTagsOption(parsedOptions.requiredParamsTags, option);

			} else if (option[0].equals("-optionalParamsTags")) {
				addTagsOption(parsedOptions.optionalParamsTags, option);

			} else if (option[0].equals("-requiredFieldTags")) {
				addTagsOption(parsedOptions.requiredFieldTags, option);

			} else if (option[0].equals("-optionalFieldTags")) {
				addTagsOption(parsedOptions.optionalFieldTags, option);

				// JSR 303
			} else if (option[0].equals("-paramMinValueAnnotations")) {
				parsedOptions.paramMinValueAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-paramMaxValueAnnotations")) {
				parsedOptions.paramMaxValueAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-fieldMinAnnotations")) {
				parsedOptions.fieldMinAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-fieldMaxAnnotations")) {
				parsedOptions.fieldMaxAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-requiredParamAnnotations")) {
				parsedOptions.requiredParamAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-optionalParamAnnotations")) {
				parsedOptions.optionalParamAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-requiredFieldAnnotations")) {
				parsedOptions.requiredFieldAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));
			} else if (option[0].equals("-optionalFieldAnnotations")) {
				parsedOptions.optionalFieldAnnotations.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-unauthOperationTags")) {
				addTagsOption(parsedOptions.unauthOperationTags, option);

			} else if (option[0].equals("-authOperationTags")) {
				addTagsOption(parsedOptions.authOperationTags, option);

			} else if (option[0].equals("-unauthOperationTagValues")) {
				parsedOptions.unauthOperationTagValues.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-authOperationScopes")) {
				parsedOptions.authOperationScopes.addAll(asList(copyOfRange(option, 1, option.length)));

			} else if (option[0].equals("-operationScopeTags")) {
				addTagsOption(parsedOptions.operationScopeTags, option);

			} else if (option[0].equals("-serializationFeatures")) {
				serializationFeaturesCsv = option[1];
			} else if (option[0].equals("-deserializationFeatures")) {
				deserializationFeaturesCsv = option[1];
			} else if (option[0].equals("-defaultTyping")) {
				defaultTyping = option[1];
			} else if (option[0].equals("-serializationInclusion")) {
				serializationInclusion = option[1];
			}
		}
		parsedOptions.mapper = ObjectMapperBuilder.build(serializationFeaturesCsv, deserializationFeaturesCsv, defaultTyping, serializationInclusion);
		parsedOptions.recorder = new ObjectMapperRecorder(parsedOptions.mapper);
		return parsedOptions;
	}

	public ObjectMapper getMapper() {
		if (mapper == null) {
			mapper = ObjectMapperBuilder.build(null, null, null, null);
		}

		return mapper;
	}

	private static void addTagsOption(List<String> list, String[] option) {
		List<String> tags = asList(copyOfRange(option, 1, option.length));
		for (String tag : tags) {
			if (tag.startsWith("@")) {
				tag = tag.substring(1);
			}
			list.add(tag);
		}
	}

	@Getter
	private File outputDirectory;

	@Getter
	@Setter
	private String docBasePath = null;

	@Getter
	private String swaggerUiPath = null;

	@Getter
	@Setter
	private String apiVersion = "0";

	@Getter
	@Setter
	private boolean includeSwaggerUi = true;

	@Getter
	@Setter
	private boolean profileMode = false;

	@Setter
	private Properties variableReplacements;

	@Getter
	@Setter
	private List<String> servers;

	/**
	 * This gets prefixes of the FQN of resource classes to exclude
	 */
	@Getter
	@Setter
	private List<String> excludeResourcePrefixes;

	/**
	 * prefixes of the FQN of resource classes to include, if specified then resources must match these
	 */
	@Getter
	@Setter
	private List<String> includeResourcePrefixes;

	/**
	 * This sets the prefixes of the FQN of model classes to exclude
	 */
	@Getter
	@Setter
	private List<String> excludeModelPrefixes;

	@Getter
	private List<String> genericWrapperTypes;
	@Getter
	private List<String> responseMessageTags;
	@Getter
	private List<String> responseTypeTags;

	/**
	 * This gets tags that can customize the type for input body params
	 */
	@Getter
	private List<String> inputTypeTags;
	@Getter
	private List<String> defaultErrorTypeTags;
	@Getter
	private List<String> compositeParamAnnotations;
	@Getter
	private List<String> compositeParamTypes;
	@Getter
	private List<String> discriminatorAnnotations;
	@Getter
	private List<String> subTypesAnnotations;
	@Getter
	private List<String> excludeParamsTags;
	@Getter
	private List<String> excludeParamAnnotations;
	@Getter
	private List<String> excludeClassTags;
	@Getter
	private List<String> excludeClassAnnotations;
	@Getter
	private List<String> excludeOperationTags;
	@Getter
	private List<String> excludeOperationAnnotations;
	@Getter
	private List<String> excludeFieldTags;
	@Getter
	private List<String> excludeFieldAnnotations;
	@Getter
	private List<String> csvParamsTags;
	@Getter
	private List<String> implicitParamTags;
	@Getter
	private List<String> paramsFormatTags;
	@Getter
	private List<String> paramsMinValueTags;
	@Getter
	private List<String> paramMinValueAnnotations;
	@Getter
	private List<String> paramsMaxValueTags;
	@Getter
	private List<String> paramMaxValueAnnotations;
	@Getter
	private List<String> paramsDefaultValueTags;
	@Getter
	private List<String> paramsAllowableValuesTags;
	@Getter
	private List<String> paramsNameTags;
	@Getter
	private List<String> resourceTags;

	/**
	 * This gets a list of javadoc tag names that can be used for the api description
	 */
	@Getter
	private List<String> apiDescriptionTags;

	/**
	 * This gets a list of javadoc tag names that can be used for the operation notes
	 */
	@Getter
	private List<String> operationNotesTags;

	/**
	 * This gets a list of javadoc tag names that can be used for the operation summary
	 */
	@Getter
	private List<String> operationSummaryTags;
	/**
	 * This gets list of javadoc tag names that can be used for the model field/method descriptions
	 */
	@Getter
	private List<String> fieldDescriptionTags;
	@Getter
	private List<String> fieldFormatTags;
	@Getter
	private List<String> fieldMinTags;
	@Getter
	private List<String> fieldMinAnnotations;
	@Getter
	private List<String> fieldMaxTags;
	@Getter
	private List<String> fieldMaxAnnotations;
	@Getter
	private List<String> fieldDefaultTags;
	@Getter
	private List<String> fieldAllowableValuesTags;
	@Getter
	private List<String> requiredParamsTags;
	@Getter
	private List<String> requiredParamAnnotations;
	@Getter
	private List<String> optionalParamsTags;
	@Getter
	private List<String> optionalParamAnnotations;
	@Getter
	private List<String> requiredFieldTags;
	@Getter
	private List<String> requiredFieldAnnotations;
	@Getter
	private List<String> optionalFieldTags;
	@Getter
	private List<String> optionalFieldAnnotations;

	/**
	 * tags that say a method does NOT require authorization
	 */
	@Getter
	private List<String> unauthOperationTags;

	/**
	 * tags that indicate whether an operation requires auth or not, coupled with a value from unauthOperationTagValues
	 */
	@Getter
	private List<String> authOperationTags;

	/**
	 * for tags in authOperationTags this is the value to look for to indicate method does NOT require authorization
	 */
	@Getter
	private List<String> unauthOperationTagValues;

	/**
	 * default scopes to add if authOperationTags is present but no scopes
	 */
	@Getter
	private List<String> authOperationScopes;

	/**
	 * explicit scopes that are required for authorization for a method
	 */
	@Getter
	private List<String> operationScopeTags;

	/**
	 * This gets list of javadoc tag names that can be used for ordering resources in the resource listing
	 */
	@Getter
	private List<String> resourcePriorityTags;

	/**
	 * This gets list of javadoc tag names that can be used for the description of resources
	 */
	@Getter
	private List<String> resourceDescriptionTags;

	/**
	 * FQN of annotations that if present denote a parameter as being a File data type
	 */
	@Getter
	private List<String> fileParameterAnnotations;

	/**
	 * FQN of types of a parameter that are File data types
	 */
	@Getter
	private List<String> fileParameterTypes;

	/**
	 * FQN of annotations that if present denote a parameter as being a form parameter type
	 */
	@Getter
	private List<String> formParameterAnnotations;

	/**
	 * FQN of types of a parameter that are form parameter types
	 */
	@Getter
	private List<String> formParameterTypes;

	@Getter
	private List<String> parameterNameAnnotations;

	/**
	 * list of type prefixes that are mapped to long data type
	 */
	@Getter
	private List<String> longTypePrefixes;

	/**
	 * list of type prefixes that are mapped to int data type
	 */
	@Getter
	private List<String> intTypePrefixes;

	/**
	 * list of type prefixes that are mapped to float data type
	 */
	@Getter
	private List<String> floatTypePrefixes;

	/**
	 * list of type prefixes that are mapped to double data type
	 */
	@Getter
	private List<String> doubleTypePrefixes;

	/**
	 * list of type prefixes that are mapped to string data type, can be used for example to map header types to string
	 */
	@Getter
	private List<String> stringTypePrefixes;

	@Getter
	private boolean excludeDeprecatedResourceClasses = true;

	@Getter
	private boolean excludeDeprecatedModelClasses = true;

	@Getter
	@Setter
	private boolean excludeDeprecatedOperations = true;

	@Getter
	@Setter
	private boolean excludeDeprecatedFields = true;

	@Getter
	@Setter
	private boolean excludeDeprecatedParams = true;

	@Getter
	private boolean logDebug = false;

	@Getter
	private boolean parseModels = true;

	@Getter
	@Setter
	private boolean useFullModelIds = false;

	/**
	 * This is whether model fields are required by default e.g. if it is not specified whether a field is optional or not
	 */
	@Getter
	@Setter
	private boolean modelFieldsRequiredByDefault = false;

	@Getter
	@Setter
	private boolean modelFieldsXmlAccessTypeEnabled = true;

	@Getter
	@Setter
	private boolean modelFieldsDefaultXmlAccessTypeEnabled = false;

	@Getter
	@Setter
	private NamingConvention modelFieldsNamingConvention = NamingConvention.DEFAULT_NAME;

	@Getter
	@Setter
	private boolean sortResourcesByPath = false;

	@Getter
	@Setter
	private boolean sortResourcesByPriority = false;

	@Getter
	@Setter
	private boolean sortApisByPath = true;

	@Getter
	@Setter
	private ResponseMessageSortMode responseMessageSortMode;

	@Getter
	@Setter
	private Map<String, SecurityScheme> securitySchemes;

	@Getter
	@Setter
	private Info apiInfo;

	ObjectMapper mapper;

	@Getter
	@Setter
	private Recorder recorder;

	@Getter
	@Setter
	private Translator translator;

	/**
	 * This creates a DocletOptions
	 */
	public DocletOptions() {

		this.servers = new ArrayList<>();
		this.servers.add("http://localhost:8080/");

                this.responseMessageTags = new ArrayList<>();
		this.responseMessageTags.add("responseMessage");
		this.responseMessageTags.add("status");
		this.responseMessageTags.add("errorResponse");
		this.responseMessageTags.add("errorCode");
		this.responseMessageTags.add("successResponse");
		this.responseMessageTags.add("successCode");

                this.excludeModelPrefixes = new ArrayList<>();
		this.excludeModelPrefixes.add("org.joda.time.DateTime");
		this.excludeModelPrefixes.add("java.util.UUID");
		this.excludeModelPrefixes.add("java.io.");
		this.excludeModelPrefixes.add("com.sun.jersey.core.header.");
		this.excludeModelPrefixes.add("org.springframework.web.multipart.");
		this.excludeModelPrefixes.add("org.jboss.resteasy.plugins.providers.multipart.");

		// custom types which are mapped to longs
                this.longTypePrefixes = new ArrayList<>();

		// custom types which are mapped to ints
                this.intTypePrefixes = new ArrayList<>();

		// custom types which are mapped to floats
                this.floatTypePrefixes = new ArrayList<>();

		// custom types which are mapped to doubles
                this.doubleTypePrefixes = new ArrayList<>();

		// custom types which are mapped to strings
                this.stringTypePrefixes = new ArrayList<>();
		this.stringTypePrefixes.add("com.sun.jersey.core.header.");

		// types which simply wrap an entity
                this.genericWrapperTypes = new ArrayList<>();
                // TODO :: add Java Optional
		this.genericWrapperTypes.add("com.sun.jersey.api.JResponse");
		this.genericWrapperTypes.add("com.google.common.base.Optional");
		this.genericWrapperTypes.add("jersey.repackaged.com.google.common.base.Optional");

		// annotations and types which are mapped to File data type,
		// NOTE these only apply for multipart resources
                this.fileParameterAnnotations = new ArrayList<>();
		this.fileParameterAnnotations.add("org.jboss.resteasy.annotations.providers.multipart.MultipartForm");

                this.fileParameterTypes = new ArrayList<>();
		this.fileParameterTypes.add("java.io.File");
		this.fileParameterTypes.add("java.io.InputStream");
		this.fileParameterTypes.add("byte[]");
		this.fileParameterTypes.add("org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput");

		// annotations and types which are mapped to form parameter type
                this.formParameterAnnotations = new ArrayList<>();
		this.formParameterAnnotations.add("com.sun.jersey.multipart.FormDataParam");
		this.formParameterAnnotations.add("javax.ws.rs.FormParam");

                this.formParameterTypes = new ArrayList<>();
		this.formParameterTypes.add("com.sun.jersey.core.header.FormDataContentDisposition");

		// overrides for parameter names
                this.paramsNameTags = new ArrayList<>();
		this.paramsNameTags.add("paramsName");
		this.paramsNameTags.add("overrideParamsName");

		// annotations to use for parameter names
                this.parameterNameAnnotations = new ArrayList<>();
		for (String annotation : ParserHelper.JAXRS_PARAM_ANNOTATIONS) {
			this.parameterNameAnnotations.add(annotation);
		}
		this.parameterNameAnnotations.add("com.sun.jersey.multipart.FormDataParam");

		// annotations/types to use for composite param objects
                this.compositeParamAnnotations = new ArrayList<>();
		this.compositeParamAnnotations.add("javax.ws.rs.BeanParam");
                this.compositeParamTypes = new ArrayList<>();

                this.discriminatorAnnotations = new ArrayList<>();
		this.discriminatorAnnotations.add("com.fasterxml.jackson.annotation.JsonTypeInfo");

                this.subTypesAnnotations = new ArrayList<>();
		this.subTypesAnnotations.add("com.fasterxml.jackson.annotation.JsonSubTypes");

                this.excludeResourcePrefixes = new ArrayList<>();
                this.includeResourcePrefixes = new ArrayList<>();

                this.excludeClassTags = new ArrayList<>();
		this.excludeClassTags.add("hidden");
		this.excludeClassTags.add("hide");
		this.excludeClassTags.add("exclude");

                this.excludeClassAnnotations = new ArrayList<>();

                this.excludeOperationTags = new ArrayList<>();
		this.excludeOperationTags.add("hidden");
		this.excludeOperationTags.add("hide");
		this.excludeOperationTags.add("exclude");

                this.excludeOperationAnnotations = new ArrayList<>();

                this.excludeFieldTags = new ArrayList<>();
		this.excludeFieldTags.add("hidden");
		this.excludeFieldTags.add("hide");
		this.excludeFieldTags.add("exclude");

                this.excludeFieldAnnotations = new ArrayList<>();

                this.excludeParamsTags = new ArrayList<>();
		this.excludeParamsTags.add("excludeParams");
		this.excludeParamsTags.add("hiddenParams");
		this.excludeParamsTags.add("hideParams");

                this.excludeParamAnnotations = new ArrayList<>();
		this.excludeParamAnnotations.add("javax.ws.rs.core.Context");
		this.excludeParamAnnotations.add("javax.ws.rs.CookieParam");
		this.excludeParamAnnotations.add("javax.ws.rs.MatrixParam");
		this.excludeParamAnnotations.add("javax.ws.rs.container.Suspended");

                this.csvParamsTags = new ArrayList<>();
		this.csvParamsTags.add("csvParams");

                this.implicitParamTags = new ArrayList<>();
		this.implicitParamTags.add("implicitParam");
		this.implicitParamTags.add("additionalParam");
		this.implicitParamTags.add("extraParam");

                this.paramsFormatTags = new ArrayList<>();
		this.paramsFormatTags.add("paramsFormat");
		this.paramsFormatTags.add("formats");

                this.paramsMinValueTags = new ArrayList<>();
		this.paramsMinValueTags.add("paramsMinValue");
		this.paramsMinValueTags.add("paramsMinimumValue");
		this.paramsMinValueTags.add("minValues");

                this.paramsMaxValueTags = new ArrayList<>();
		this.paramsMaxValueTags.add("paramsMaxValue");
		this.paramsMaxValueTags.add("paramsMaximumValue");
		this.paramsMaxValueTags.add("maxValues");

                this.paramsDefaultValueTags = new ArrayList<>();
		this.paramsDefaultValueTags.add("paramsDefaultValue");
		this.paramsDefaultValueTags.add("defaultValues");

                this.paramsAllowableValuesTags = new ArrayList<>();
		this.paramsAllowableValuesTags.add("paramsAllowableValues");
		this.paramsAllowableValuesTags.add("allowableValues");

                this.resourceTags = new ArrayList<>();
		this.resourceTags.add("resourceTag");
		this.resourceTags.add("parentEndpointName");
		this.resourceTags.add("resource");

                this.responseTypeTags = new ArrayList<>();
		this.responseTypeTags.add("responseType");
		this.responseTypeTags.add("outputType");
		this.responseTypeTags.add("returnType");

                this.inputTypeTags = new ArrayList<>();
		this.inputTypeTags.add("inputType");
		this.inputTypeTags.add("bodyType");

                this.defaultErrorTypeTags = new ArrayList<>();
		this.defaultErrorTypeTags.add("defaultErrorType");

                this.apiDescriptionTags = new ArrayList<>();
		this.apiDescriptionTags.add("apiDescription");

                this.operationNotesTags = new ArrayList<>();
		this.operationNotesTags.add("description");
		this.operationNotesTags.add("comment");
		this.operationNotesTags.add("notes");

                this.operationSummaryTags = new ArrayList<>();
		this.operationSummaryTags.add("summary");
		this.operationSummaryTags.add("endpointName");

                this.fieldDescriptionTags = new ArrayList<>();
		this.fieldDescriptionTags.add("description");
		this.fieldDescriptionTags.add("comment");
		this.fieldDescriptionTags.add("return");

                this.fieldFormatTags = new ArrayList<>();
		this.fieldFormatTags.add("format");

                this.fieldMinTags = new ArrayList<>();
		this.fieldMinTags.add("min");
		this.fieldMinTags.add("minimum");

                this.fieldMaxTags = new ArrayList<>();
		this.fieldMaxTags.add("max");
		this.fieldMaxTags.add("maximum");

                this.fieldDefaultTags = new ArrayList<>();
		this.fieldDefaultTags.add("default");
		this.fieldDefaultTags.add("defaultValue");

                this.fieldAllowableValuesTags = new ArrayList<>();
		this.fieldAllowableValuesTags.add("allowableValues");
		this.fieldAllowableValuesTags.add("values");
		this.fieldAllowableValuesTags.add("enum");

                this.requiredParamsTags = new ArrayList<>();
		this.requiredParamsTags.add("requiredParams");

                this.optionalParamsTags = new ArrayList<>();
		this.optionalParamsTags.add("optionalParams");

                this.requiredFieldTags = new ArrayList<>();
		this.requiredFieldTags.add("required");
		this.requiredFieldTags.add("requiredField");

                this.optionalFieldTags = new ArrayList<>();
		this.optionalFieldTags.add("optional");
		this.optionalFieldTags.add("optionalField");

		// JSR 303

                this.paramMinValueAnnotations = new ArrayList<>();
		this.paramMinValueAnnotations.add("javax.validation.constraints.Size");
		this.paramMinValueAnnotations.add("javax.validation.constraints.DecimalMin");

                this.paramMaxValueAnnotations = new ArrayList<>();
		this.paramMaxValueAnnotations.add("javax.validation.constraints.Size");
		this.paramMaxValueAnnotations.add("javax.validation.constraints.DecimalMax");

                this.fieldMinAnnotations = new ArrayList<>();
		this.fieldMinAnnotations.add("javax.validation.constraints.Size");
		this.fieldMinAnnotations.add("javax.validation.constraints.DecimalMin");

                this.fieldMaxAnnotations = new ArrayList<>();
		this.fieldMaxAnnotations.add("javax.validation.constraints.Size");
		this.fieldMaxAnnotations.add("javax.validation.constraints.DecimalMax");

                this.requiredParamAnnotations = new ArrayList<>();
		this.requiredParamAnnotations.add("javax.validation.constraints.NotNull");

                this.optionalParamAnnotations = new ArrayList<>();
		this.optionalParamAnnotations.add("javax.validation.constraints.Null");

                this.requiredFieldAnnotations = new ArrayList<>();
		this.requiredFieldAnnotations.add("javax.validation.constraints.NotNull");

                this.optionalFieldAnnotations = new ArrayList<>();
		this.optionalFieldAnnotations.add("javax.validation.constraints.Null");

                this.unauthOperationTags = new ArrayList<>();
		this.unauthOperationTags.add("noAuth");
		this.unauthOperationTags.add("unauthorized");

                this.authOperationTags = new ArrayList<>();
		this.authOperationTags.add("authentication");
		this.authOperationTags.add("authorization");

                this.unauthOperationTagValues = new ArrayList<>();
		this.unauthOperationTagValues.add("not required");
		this.unauthOperationTagValues.add("off");
		this.unauthOperationTagValues.add("false");
		this.unauthOperationTagValues.add("none");

                this.operationScopeTags = new ArrayList<>();
		this.operationScopeTags.add("scope");
		this.operationScopeTags.add("oauth2Scope");

                this.authOperationScopes = new ArrayList<>();

                this.resourcePriorityTags = new ArrayList<>();
		this.resourcePriorityTags.add("resourcePriority");
		this.resourcePriorityTags.add("resourceOrder");
		this.resourcePriorityTags.add("priority");

                this.resourceDescriptionTags = new ArrayList<>();
		this.resourceDescriptionTags.add("resourceDescription");

		this.responseMessageSortMode = ResponseMessageSortMode.CODE_ASC;

		FirstNotNullTranslator fnnTranslator = new FirstNotNullTranslator();
		for (String paramAnnotation : ParserHelper.JAXRS_PARAM_ANNOTATIONS) {
			fnnTranslator.addNext(new AnnotationAwareTranslator(this).element(paramAnnotation, "value"));
		}

		fnnTranslator
				.addNext(
						new AnnotationAwareTranslator(this).ignore("javax.xml.bind.annotation.XmlTransient")
								.element("javax.xml.bind.annotation.XmlElement", "name").rootElement("javax.xml.bind.annotation.XmlRootElement", "name"))
				.addNext(
						new AnnotationAwareTranslator(this).ignore("javax.xml.bind.annotation.XmlTransient").element("javax.xml.bind.annotation.XmlAttribute",
								"name"))
				.addNext(
						new AnnotationAwareTranslator(this).ignore("com.fasterxml.jackson.annotation.JsonIgnore")
								.element("com.fasterxml.jackson.annotation.JsonProperty", "value")
								.rootElement("com.fasterxml.jackson.annotation.JsonRootName", "value"))

				.addNext(
						new AnnotationAwareTranslator(this).ignore("org.codehaus.jackson.annotate.JsonIgnore")
								.element("org.codehaus.jackson.annotate.JsonProperty", "value")
								.rootElement("org.codehaus.jackson.map.annotate.JsonRootName", "value")).addNext(new NameBasedTranslator(this));

		fnnTranslator.addNext(new NameBasedTranslator(this));

		this.translator = fnnTranslator;
	}

	/**
	 * This replaces any variables in the given value with replacements defined in the doclets variable replacements file
	 * @param value The value to replace variables in
	 * @return The value with any variable references replaced
	 */
	public String replaceVars(String value) {
		if (value != null && this.variableReplacements != null && !this.variableReplacements.isEmpty()) {
			return VariableReplacer.replaceVariables(this.variableReplacements, value);
		}
		return value;
	}

}
