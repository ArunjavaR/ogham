package fr.sii.ogham.template.thymeleaf.adapter;

import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import fr.sii.ogham.core.resource.resolver.ClassPathResolver;
import fr.sii.ogham.core.resource.resolver.DelegateResourceResolver;
import fr.sii.ogham.core.resource.resolver.ResourceResolver;
import fr.sii.ogham.template.thymeleaf.resolver.FixClassLoaderTemplateResolver;

/**
 * Adapter that converts general {@link ClassPathResolver} into Thymeleaf
 * specific {@link ClassLoaderTemplateResolver}.
 * 
 * @author Aurélien Baudet
 *
 */
public class ClassPathResolverAdapter extends AbstractTemplateResolverOptionsAdapter implements TemplateResolverAdapter {
	@Override
	public boolean supports(ResourceResolver resolver) {
		ResourceResolver actualResolver = resolver instanceof DelegateResourceResolver ? ((DelegateResourceResolver) resolver).getActualResourceResolver() : resolver;
		return actualResolver instanceof ClassPathResolver;
	}

	@Override
	public ITemplateResolver adapt(ResourceResolver resolver) {
		FixClassLoaderTemplateResolver templateResolver = new FixClassLoaderTemplateResolver();
		applyOptions(templateResolver);
		return templateResolver;
	}

}
