package fr.sii.ogham.email.builder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sii.ogham.core.builder.AbstractParent;
import fr.sii.ogham.core.builder.Builder;
import fr.sii.ogham.core.builder.env.EnvironmentBuilder;
import fr.sii.ogham.core.builder.resolution.ClassPathResolutionBuilder;
import fr.sii.ogham.core.builder.resolution.FileResolutionBuilder;
import fr.sii.ogham.core.builder.resolution.ResourceResolutionBuilder;
import fr.sii.ogham.core.builder.resolution.ResourceResolutionBuilderHelper;
import fr.sii.ogham.core.builder.resolution.StringResolutionBuilder;
import fr.sii.ogham.core.resource.resolver.FirstSupportingResourceResolver;
import fr.sii.ogham.core.resource.resolver.ResourceResolver;
import fr.sii.ogham.core.translator.content.ContentTranslator;
import fr.sii.ogham.html.inliner.CssInliner;
import fr.sii.ogham.html.inliner.impl.jsoup.JsoupCssInliner;
import fr.sii.ogham.html.translator.InlineCssTranslator;

/**
 * Configures how CSS are applied on HTML emails.
 * 
 * Inlining CSS means that CSS styles are loaded and applied on the matching
 * HTML nodes using the {@code style} HTML attribute.
 * 
 * @author Aurélien Baudet
 *
 */
public class CssInliningBuilder extends AbstractParent<CssHandlingBuilder> implements ResourceResolutionBuilder<CssInliningBuilder>, Builder<ContentTranslator> {
	private static final Logger LOG = LoggerFactory.getLogger(CssInliningBuilder.class);

	private ResourceResolutionBuilderHelper<CssInliningBuilder> resourceResolutionBuilderHelper;
	private boolean useJsoup;

	/**
	 * Initializes the builder with a parent builder. The parent builder is used
	 * when calling {@link #and()} method. The {@link EnvironmentBuilder} is
	 * used to evaluate properties when {@link #build()} method is called.
	 * 
	 * @param parent
	 *            the parent builder
	 * @param environmentBuilder
	 *            the configuration for property resolution and evaluation
	 */
	public CssInliningBuilder(CssHandlingBuilder parent, EnvironmentBuilder<?> environmentBuilder) {
		super(parent);
		resourceResolutionBuilderHelper = new ResourceResolutionBuilderHelper<>(this, environmentBuilder);
	}

	/**
	 * Enable CSS inlining: CSS styles are loaded and applied on the matching
	 * HTML nodes using the {@code style} HTML attribute.
	 * 
	 * The implementation uses <a href="https://jsoup.org/">jsoup</a> to parse
	 * HTML content.
	 * 
	 * @return this instance for fluent chaining
	 */
	public CssInliningBuilder jsoup() {
		useJsoup = true;
		return this;
	}

	@Override
	public ClassPathResolutionBuilder<CssInliningBuilder> classpath() {
		return resourceResolutionBuilderHelper.classpath();
	}

	@Override
	public FileResolutionBuilder<CssInliningBuilder> file() {
		return resourceResolutionBuilderHelper.file();
	}

	@Override
	public StringResolutionBuilder<CssInliningBuilder> string() {
		return resourceResolutionBuilderHelper.string();
	}

	@Override
	public CssInliningBuilder resolver(ResourceResolver resolver) {
		return resourceResolutionBuilderHelper.resolver(resolver);
	}

	@Override
	public ContentTranslator build() {
		ResourceResolver resourceResolver = buildResolver();
		CssInliner cssInliner = buildInliner();
		if (cssInliner == null) {
			LOG.info("CSS won't be applied on HTML content of your emails because no inliner is configured");
			return null;
		}
		return new InlineCssTranslator(cssInliner, resourceResolver);
	}

	private CssInliner buildInliner() {
		if (useJsoup) {
			return new JsoupCssInliner();
		}
		return null;
	}

	private ResourceResolver buildResolver() {
		List<ResourceResolver> resolvers = resourceResolutionBuilderHelper.buildResolvers();
		return new FirstSupportingResourceResolver(resolvers);
	}

}
