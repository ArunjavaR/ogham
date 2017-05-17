package fr.sii.ogham.core.template.detector;

import fr.sii.ogham.core.exception.template.EngineDetectionException;
import fr.sii.ogham.core.template.context.Context;

/**
 * Defines the contract for template engine detection. The implementations
 * indicate if the template can be handle by the associated template engine
 * parser. The detection can be based on:
 * <ul>
 * <li>on the template name (may be the full template path)</li>
 * <li>on the template variable substitutions</li>
 * <li>on the template content</li>
 * </ul>
 * 
 * @author Aurélien Baudet
 *
 */
public interface TemplateEngineDetector {
	/**
	 * Indicates if the template can be parsed or not.
	 * 
	 * @param templateName
	 *            the name of the template (may be the full path)
	 * @param ctx
	 *            the variable substitutions
	 * @return true if the engine can parse the template, false otherwise
	 * @throws EngineDetectionException
	 *             when something went wrong during detection
	 */
	public boolean canParse(String templateName, Context ctx) throws EngineDetectionException;
}
