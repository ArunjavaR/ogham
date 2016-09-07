package fr.sii.ogham.core.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sii.ogham.core.exception.builder.BuildException;
import fr.sii.ogham.core.resource.resolver.ClassPathResolver;
import fr.sii.ogham.core.resource.resolver.FileResolver;
import fr.sii.ogham.core.resource.resolver.FirstSupportingResourceResolver;
import fr.sii.ogham.core.resource.resolver.ResourceResolver;
import fr.sii.ogham.core.resource.resolver.StringResourceResolver;
import fr.sii.ogham.core.template.detector.FixedEngineDetector;
import fr.sii.ogham.core.template.detector.TemplateEngineDetector;
import fr.sii.ogham.core.template.parser.AutoDetectTemplateParser;
import fr.sii.ogham.core.template.parser.TemplateParser;
import fr.sii.ogham.core.util.BuilderUtils;
import fr.sii.ogham.template.TemplateConstants;
import fr.sii.ogham.template.thymeleaf.ThymeleafParser;
import fr.sii.ogham.template.thymeleaf.ThymeleafTemplateDetector;
import fr.sii.ogham.template.thymeleaf.builder.ThymeleafBuilder;

/**
 * A specialized builder for template management. It helps construct the
 * template engines. By default, the builder construct the following engines:
 * <ul>
 * <li>{@link ThymeleafParser} (delegate construction to
 * {@link ThymeleafBuilder})</li>
 * </ul>
 * 
 * By default, the builder will use the following template resolvers:
 * <ul>
 * <li>Resolver that is able to handle classpath resolution (
 * {@link ClassPathResolver})</li>
 * <li>Resolver that is able to handle file resolution (
 * {@link FileResolver})</li>
 * <li>Resolver that is able to handle string directly (
 * {@link StringResourceResolver})</li>
 * </ul>
 * 
 * This builder is also able to register a parent path and an extension for
 * template resolution. The aim is to be able to provide only the name of the
 * template without needing to provide the full path to it.
 * 
 * @author Aurélien Baudet
 *
 */
public class TemplateBuilder implements TemplateParserBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(TemplateBuilder.class);

	/**
	 * The builder used to generate template resolution
	 */
	private FirstSupportingResolverBuilder resolverBuilder;

	/**
	 * The parent path for template resolution
	 */
	private String parentPath;

	/**
	 * The extension for template resolution
	 */
	private String extension;

	/**
	 * A map that stores the engine detector and the associated engine. Each
	 * detector will indicate if the engine is able to parse the template.
	 */
	private Map<TemplateEngineDetector, TemplateParserBuilder> detectors;

	/**
	 * The property key for parentPath value
	 */
	private String parentPathPropKey;

	/**
	 * The property key for extension value
	 */
	private String extensionPropKey;

	/**
	 * The properties used for the configuration
	 */
	private Properties properties;

	public TemplateBuilder() {
		super();
		detectors = new HashMap<>();
		this.parentPathPropKey = TemplateConstants.PREFIX_PROPERTY;
		this.extensionPropKey = TemplateConstants.SUFFIX_PROPERTY;
	}

	/**
	 * Tells the builder to use all default behaviors and values. It will enable
	 * the following template engines:
	 * <ul>
	 * <li>{@link ThymeleafParser} with the associated detector (
	 * {@link ThymeleafTemplateDetector}) by calling
	 * {@link #withThymeleaf()}</li>
	 * </ul>
	 * 
	 * It will also use the default template resolvers:
	 * <ul>
	 * <li>Resolver that is able to handle classpath resolution (
	 * {@link ClassPathResolver})</li>
	 * <li>Resolver that is able to handle file resolution (
	 * {@link FileResolver})</li>
	 * <li>Resolver that is able to handle string directly (
	 * {@link StringResourceResolver})</li>
	 * </ul>
	 * 
	 * If the system properties provide values for parent path (
	 * ogham.template.prefix) and extension (ogham.template.suffix), then the
	 * builder will use them. If no value defined for these properties, then
	 * empty strings will be used.
	 * 
	 * @return this builder instance for fluent use
	 */
	public TemplateBuilder useDefaults() {
		return useDefaults(BuilderUtils.getDefaultProperties());
	}

	/**
	 * Tells the builder to use all default behaviors and values. It will enable
	 * the following template engines:
	 * <ul>
	 * <li>{@link ThymeleafParser} with the associated detector (
	 * {@link ThymeleafTemplateDetector})</li>
	 * </ul>
	 * 
	 * It will also use the default template resolvers (by calling
	 * {@link #useDefaultResolvers()}):
	 * <ul>
	 * <li>Resolver that is able to handle classpath resolution (
	 * {@link ClassPathResolver})</li>
	 * <li>Resolver that is able to handle file resolution (
	 * {@link FileResolver})</li>
	 * <li>Resolver that is able to handle string directly (
	 * {@link StringResourceResolver})</li>
	 * </ul>
	 * 
	 * If the provided properties provide values for parent path (
	 * ogham.template.prefix) and extension (ogham.template.suffix), then the
	 * builder will use them. If no value defined for these properties, then
	 * empty strings will be used.
	 * 
	 * @param properties
	 *            indicate which properties to use instead of using the system
	 *            ones
	 * @return this builder instance for fluent use
	 */
	public TemplateBuilder useDefaults(Properties properties) {
		this.properties = properties;
		useDefaultResolvers();
		withThymeleaf();
		return this;
	}

	/**
	 * Register a specialized builder for a particular template engine. Using
	 * this method will associate a detector to this engine that will accept all
	 * templates.
	 * 
	 * @param builder
	 *            the builder to register
	 * @return this builder instance for fluent use
	 */
	public TemplateBuilder registerTemplateParser(TemplateParserBuilder builder) {
		return registerTemplateParser(builder, new FixedEngineDetector());
	}

	/**
	 * Register a specialized builder for a particular template engine. It also
	 * registers the associated detector to indicate if the engine can handle
	 * the templates at runtime.
	 * 
	 * @param builder
	 *            the builder to register
	 * @param detector
	 *            the detector that indicates if the engine can handle the
	 *            provided template at runtime
	 * @return this builder instance for fluent use
	 */
	public TemplateBuilder registerTemplateParser(TemplateParserBuilder builder, TemplateEngineDetector detector) {
		detectors.put(detector, builder);
		return this;
	}

	/**
	 * Tells the builder to use the default template resolvers:
	 * <ul>
	 * <li>Resolver that is able to handle classpath resolution (
	 * {@link ClassPathResolver}). The lookup prefix is "classpath:"</li>
	 * <li>Resolver that is able to handle file resolution (
	 * {@link FileResolver}). The lookup is "file:"</li>
	 * <li>Resolver that is able to handle string directly (
	 * {@link StringResourceResolver}). The lookup is "string:"</li>
	 * <li>Default resolver if no lookup is used (
	 * {@link ClassPathResolver})</li>
	 * </ul>
	 * 
	 * This method is automatically called by {@link #useDefaults()} or
	 * {@link #useDefaults(Properties)}.
	 * 
	 * @return this builder instance for fluent use
	 * @see FirstSupportingResolverBuilder
	 */
	public TemplateBuilder useDefaultResolvers() {
		resolverBuilder = new FirstSupportingResolverBuilder().useDefaults();
		return this;
	}

	/**
	 * Tells the builder to use your own builder for template resolution.
	 * 
	 * @param resolverBuilder
	 *            the builder to use instead of default one
	 * @return this builder instance for fluent use
	 */
	public TemplateBuilder withResourceResolverBuilder(FirstSupportingResolverBuilder resolverBuilder) {
		this.resolverBuilder = resolverBuilder;
		return this;
	}

	@Override
	public TemplateBuilder withParentPath(String parentPath) {
		this.parentPath = parentPath;
		return this;
	}

	@Override
	public TemplateBuilder withExtension(String extension) {
		this.extension = extension;
		return this;
	}

	/**
	 * Enable Thymeleaf template engine. This engine is used only if the
	 * associated detector ({@link ThymeleafTemplateDetector}) indicates that
	 * Thymeleaf is able to handle the provided template.
	 * 
	 * @return this builder instance for fluent use
	 */
	public TemplateBuilder withThymeleaf() {
		// The try/catch clause
		try {
			registerTemplateParser(new ThymeleafBuilder(), new ThymeleafTemplateDetector());
		} catch (Exception e) {
			LOG.debug("Can't register Thymeleaf template engine", e);
		}
		return this;
	}

	/**
	 * Change the default property key for template resolution parent path. By
	 * default, the property key is ogham.template.prefix (see
	 * {@link TemplateConstants#PREFIX_PROPERTY}). Use this method to change the
	 * key.
	 * 
	 * @param parentPathKey
	 *            the new property key for template resolution parent path
	 * @return this instance for fluent use
	 */
	public TemplateBuilder setParentPathKey(String parentPathKey) {
		this.parentPathPropKey = parentPathKey;
		return this;
	}

	/**
	 * Change the default property key for template resolution extension. By
	 * default, the property key is ogham.template.suffix (see
	 * {@link TemplateConstants#SUFFIX_PROPERTY}). Use this method to change the
	 * key.
	 * 
	 * @param extensionKey
	 *            the new property key for template resolution extension
	 * @return this instance for fluent use
	 */
	public TemplateBuilder setExtensionKey(String extensionKey) {
		this.extensionPropKey = extensionKey;
		return this;
	}

	/**
	 * Build the template parser according to options previously enabled. If
	 * only one template engine has been activated then the parser will be this
	 * template engine parser. If there are several activated engines, then the
	 * builder will generate an {@link AutoDetectTemplateParser}. This kind of
	 * parser is able to detect which parser to use according to the provided
	 * template at runtime. The auto-detection is delegated to each defined
	 * {@link TemplateEngineDetector} associated with each engine.
	 * 
	 * The builder will also construct a template resolver based on the
	 * previously defined mappings. This mapping is done using a
	 * {@link FirstSupportingResourceResolver}. It uses a map indexed by the
	 * lookup string (classpath, file, ...) and the resolver implementation as
	 * value.
	 * 
	 * The builder will also provide the previously defined parent path and
	 * extension to the resolvers and to the delegated builders.
	 * 
	 * @return The parser implementation for templating system. If only one
	 *         template engine defined, then use it directly. Otherwise use the
	 *         auto-detection feature to automatically detect at runtime which
	 *         engine to use
	 * @throws BuildException
	 *             when the builder can't construct the parser
	 */
	@Override
	public TemplateParser build() throws BuildException {
		// resolve final parentPath and extension
		String resolvedParentPath = resolve("prefix", parentPath, parentPathPropKey, TemplateConstants.PREFIX_PROPERTY);
		String resolvedExtension = resolve("suffix", extension, extensionPropKey, TemplateConstants.SUFFIX_PROPERTY);
		// propagate the parentPath and extension for resource resolution
		// and also for template engines
		resolverBuilder.withParentPath(resolvedParentPath);
		resolverBuilder.withExtension(resolvedExtension);
		FirstSupportingResourceResolver firstSupportingResolver = resolverBuilder.build();
		List<ResourceResolver> resolvers = firstSupportingResolver.getResolvers();
		for (TemplateParserBuilder builder : detectors.values()) {
			// set parent path and extension for each implementation
			builder.withParentPath(resolvedParentPath);
			builder.withExtension(resolvedExtension);
			if (builder instanceof ThymeleafBuilder) {
				/*
				 * link between {@link ThymeleafResolverAdapter} and {@link
				 * ResourceResolver}
				 */
				((ThymeleafBuilder) builder).withFirstResourceResolver(firstSupportingResolver);
			}
		}
		if (detectors.isEmpty()) {
			// if no template parser available => exception
			throw new BuildException("No parser available. Either disable template features or register a template engine");
		} else if (detectors.size() == 1) {
			// if no detector defined or only one available parser => do not use
			// auto detection
			TemplateParser parser = detectors.values().iterator().next().build();
			LOG.info("Using single template engine: {}", parser);
			LOG.debug("Using parent path {} and extension {} for template resolution", parentPath, extension);
			LOG.debug("Using lookup mapping resolver: {}", resolvers);
			return parser;
		} else {
			// use auto detection if more than one parser available
			Map<TemplateEngineDetector, TemplateParser> map = new HashMap<>();
			for (Entry<TemplateEngineDetector, TemplateParserBuilder> entry : detectors.entrySet()) {
				map.put(entry.getKey(), entry.getValue().build());
			}
			LOG.info("Using auto detection mechanism");
			LOG.debug("Auto detection mechanisms: {}", map);
			LOG.debug("Using parent path {} and extension {} for template resolution", parentPath, extension);
			LOG.debug("Using lookup mapping resolver: {}", resolvers);
			return new AutoDetectTemplateParser(firstSupportingResolver, map);
		}
	}

	/**
	 * Get reference to the specialized builder. It may be useful to fine tune
	 * the template engine.
	 * 
	 * @param clazz
	 *            the class of the parser builder to get
	 * @param <B>
	 *            the type of the class to get
	 * @return the template parser builder instance
	 * @throws IllegalArgumentException
	 *             when provided class references an nonexistent builder
	 */
	@SuppressWarnings("unchecked")
	public <B extends TemplateParserBuilder> B getParserBuilder(Class<B> clazz) {
		for (TemplateParserBuilder builder : detectors.values()) {
			if (clazz.isAssignableFrom(builder.getClass())) {
				return (B) builder;
			}
		}
		throw new IllegalArgumentException("No implementation builder exists for " + clazz.getSimpleName());
	}

	/**
	 * <p>
	 * Get the reference to the specialized builder for Thymeleaf. It may be
	 * useful to fine tune Thymeleaf engine.
	 * </p>
	 * 
	 * Access this builder if you want to:
	 * <ul>
	 * <li>Use your own Thymeleaf template engine</li>
	 * <li>Customize the lookup resolution for Thymeleaf</li>
	 * <li>Customize the adapters</li>
	 * </ul>
	 * 
	 * @return The Thymeleaf builder
	 */
	public ThymeleafBuilder getThymeleafParser() {
		return getParserBuilder(ThymeleafBuilder.class);
	}

	/**
	 * <p>
	 * Get the builder used to handle resource resolution.
	 * </p>
	 * 
	 * Access this builder if you want to:
	 * <ul>
	 * <li>Customize how template resources are resolved</li>
	 * <li>Register a custom lookup mapping resolver for template resources</li>
	 * </ul>
	 * 
	 * @return the builder used to handle resource resolution
	 */
	public FirstSupportingResolverBuilder getResolverBuilder() {
		return resolverBuilder;
	}

	private String resolve(String which, String value, String key, String defaultKey) {
		String resolved = value;
		if (resolved == null) {
			if (properties == null) {
				LOG.debug("Property key specified ({}) but no properties provided. Empty {} is used", key, which);
				resolved = "";
			} else {
				resolved = properties.getProperty(key, properties.getProperty(defaultKey, ""));
				LOG.debug("Using {} provided by property key {}: {}", which, key, resolved);
			}
		} else {
			LOG.debug("Using provided {}: {}", which, resolved);
		}
		return resolved;
	}
}
