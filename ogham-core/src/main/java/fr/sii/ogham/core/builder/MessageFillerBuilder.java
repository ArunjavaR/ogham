package fr.sii.ogham.core.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.sii.ogham.core.exception.builder.BuildException;
import fr.sii.ogham.core.filler.EveryFillerDecorator;
import fr.sii.ogham.core.filler.MessageAwareFiller;
import fr.sii.ogham.core.filler.MessageFiller;
import fr.sii.ogham.core.filler.PropertiesFiller;
import fr.sii.ogham.core.filler.SubjectFiller;
import fr.sii.ogham.core.subject.provider.FirstSupportingSubjectProvider;
import fr.sii.ogham.core.subject.provider.HtmlTitleSubjectProvider;
import fr.sii.ogham.core.subject.provider.MultiContentSubjectProvider;
import fr.sii.ogham.core.subject.provider.SubjectProvider;
import fr.sii.ogham.core.subject.provider.TextPrefixSubjectProvider;
import fr.sii.ogham.core.util.BuilderUtils;

/**
 * Builder that help construct the message fillers. The aim of a message filler
 * is to generate some values to put into the message object.
 * 
 * @author Aurélien Baudet
 *
 */
public class MessageFillerBuilder implements Builder<MessageFiller> {
	/**
	 * The fillers to use in chain
	 */
	private List<MessageFiller> fillers;

	public MessageFillerBuilder() {
		super();
		fillers = new ArrayList<>();
	}

	@Override
	public MessageFiller build() throws BuildException {
		return new EveryFillerDecorator(fillers);
	}

	/**
	 * Tells the builder to use all default behaviors and values:
	 * <ul>
	 * <li>Fill messages with system properties</li>
	 * <li>Generate subject from HTML title or first textual line starting with
	 * <code>"Subject:"</code></li>
	 * </ul>
	 * <p>
	 * Configuration values come from system properties.
	 * </p>
	 * 
	 * @param baseKeys
	 *            the prefix(es) for the keys used for filling the message
	 * @return this instance for fluent use
	 */
	public MessageFillerBuilder useDefaults(String... baseKeys) {
		return useDefaults(BuilderUtils.getDefaultProperties(), baseKeys);
	}

	/**
	 * Tells the builder to use all default behaviors and values:
	 * <ul>
	 * <li>Fill messages with provided properties</li>
	 * <li>Generate subject from HTML title or first textual line starting with
	 * <code>"Subject:"</code></li>
	 * </ul>
	 * <p>
	 * Configuration values come from provided properties.
	 * </p>
	 * 
	 * @param properties
	 *            the properties to use instead of default ones
	 * @param baseKeys
	 *            the prefix(es) for the keys used for filling the message
	 * @return this instance for fluent use
	 */
	public MessageFillerBuilder useDefaults(Properties properties, String... baseKeys) {
		withConfigurationFiller(properties, baseKeys);
		withSubjectFiller();
		withMessageAwareFiller(properties, baseKeys);
		return this;
	}

	/**
	 * Enables filling of messages with values that comes from provided
	 * configuration properties.
	 * <p>
	 * Automatically called by {@link #useDefaults(String...)} and
	 * {@link #useDefaults(Properties, String...)}
	 * </p>
	 * 
	 * @param props
	 *            the properties that contains the values to set on the message
	 * @param baseKeys
	 *            the prefix(es) for the keys used for filling the message
	 * @return this instance for fluent use
	 */
	public MessageFillerBuilder withConfigurationFiller(Properties props, String... baseKeys) {
		for (String baseKey : baseKeys) {
			fillers.add(new PropertiesFiller(props, baseKey));
		}
		return this;
	}

	/**
	 * Enables filling of messages with values that comes from system
	 * configuration properties.
	 * <p>
	 * Automatically called by {@link #useDefaults(String...)} and
	 * {@link #useDefaults(Properties, String...)}
	 * </p>
	 * 
	 * @param baseKeys
	 *            the prefix(es) for the keys used for filling the message
	 * @return this instance for fluent use
	 */
	public MessageFillerBuilder withConfigurationFiller(String... baseKeys) {
		return withConfigurationFiller(BuilderUtils.getDefaultProperties(), baseKeys);
	}

	/**
	 * Enable the generation of subject of the message. The subject can
	 * automatically be extracted from the content:
	 * <ul>
	 * <li>If content of the message is HTML, then the title is used as subject
	 * </li>
	 * <li>If content of the message is text and the first line starts with
	 * <code>"Subject:"</code>, then it is used as subject</li>
	 * </ul>
	 * <p>
	 * Automatically called by {@link #useDefaults(String...)} and
	 * {@link #useDefaults(Properties, String...)}
	 * </p>
	 * 
	 * @return this instance for fluent use
	 */
	public MessageFillerBuilder withSubjectFiller() {
		// TODO: builder for subject provider too ?
		FirstSupportingSubjectProvider provider = new FirstSupportingSubjectProvider(new TextPrefixSubjectProvider(), new HtmlTitleSubjectProvider());
		SubjectProvider multiContentProvider = new MultiContentSubjectProvider(provider);
		provider.addProvider(multiContentProvider);
		fillers.add(new SubjectFiller(provider));
		return this;
	}

	/**
	 * A filler that is able to add well known properties according to the
	 * message.
	 * 
	 * For email:
	 * <ul>
	 * <li>If properties contains <code>ogham.email.to</code>, it adds the "TO"
	 * recipients to the message</li>
	 * <li>If properties contains <code>ogham.email.cc</code>, it adds the "CC"
	 * recipients to the message</li>
	 * <li>If properties contains <code>ogham.email.bcc</code>, it adds the
	 * "BCC" recipients to the message</li>
	 * </ul>
	 * 
	 * For sms:
	 * <ul>
	 * <li>If properties contains <code>ogham.sms.to</code>, it adds the
	 * recipient to the message</li>
	 * </ul>
	 * 
	 * The provided property values may be a comma separated list of recipients.
	 * 
	 * @param properties
	 *            the properties that contains the values to set on the message
	 * @param baseKeys
	 *            the prefix(es) for the keys used for filling the message
	 * @return this instance for fluent use
	 */
	public MessageFillerBuilder withMessageAwareFiller(Properties properties, String... baseKeys) {
		for (String baseKey : baseKeys) {
			fillers.add(new MessageAwareFiller(properties, baseKey));
		}
		return this;
	}

}
