## Template Configuration

### Use prefix and suffix

In order to organize your templates and put them at one location, you can set a prefix for templates. This can be done using properties. Each kind of message has its own property key.

Here is the sample without prefix and suffix:

<span class="highlight" data-irrelevant-lines="1-11,15-20"></span>
<span class="collapse" data-lines="1-11,15-20"></span>

```java
package fr.sii.ogham.sample.standard.email;

import java.util.Properties;

import fr.sii.ogham.context.SimpleBean;
import fr.sii.ogham.core.builder.MessagingBuilder;
import fr.sii.ogham.core.exception.MessagingException;
import fr.sii.ogham.core.message.content.TemplateContent;
import fr.sii.ogham.core.service.MessagingService;
import fr.sii.ogham.email.message.Email;

public class HtmlTemplateSample {

	public static void main(String[] args) throws MessagingException {
		// configure properties (could be stored in a properties file or defined
		// in System properties)
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", "<your server host>");
		properties.setProperty("mail.smtp.port", "<your server port>");
		properties.setProperty("ogham.email.from", "<email address to display for the sender user>");
		// Instantiate the messaging service using default behavior and
		// provided properties
		MessagingService service = new MessagingBuilder().useAllDefaults(properties).build();
		// send the email
		service.send(new Email("subject", new TemplateContent("classpath:/template/thymeleaf/simple.html", new SimpleBean("foo", 42)), "<recipient address>"));
	}

}
```

And now adding the properties `ogham.email.template.path-prefix` and `ogham.email.template.path-suffix`:

<span class="highlight" data-lines="20-21,27" data-irrelevant-lines="1-11,15-19"></span>
<span class="collapse" data-lines="1-11,15-19"></span>

```java
package fr.sii.ogham.sample.standard.email;

import java.util.Properties;

import fr.sii.ogham.context.SimpleBean;
import fr.sii.ogham.core.builder.MessagingBuilder;
import fr.sii.ogham.core.exception.MessagingException;
import fr.sii.ogham.core.message.content.TemplateContent;
import fr.sii.ogham.core.service.MessagingService;
import fr.sii.ogham.email.message.Email;

public class HtmlTemplateSample {

	public static void main(String[] args) throws MessagingException {
		// configure properties (could be stored in a properties file or defined
		// in System properties)
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", "<your server host>");
		properties.setProperty("mail.smtp.port", "<your server port>");
		properties.setProperty("ogham.email.template.path-prefix", "/template/thymeleaf/");
		properties.setProperty("ogham.email.template.path-suffix", ".html");
		properties.setProperty("ogham.email.from", "<email address to display for the sender user>");
		// Instantiate the messaging service using default behavior and
		// provided properties
		MessagingService service = new MessagingBuilder().useAllDefaults(properties).build();
		// send the email
		service.send(new Email("subject", new TemplateContent("classpath:simple", new SimpleBean("foo", 42)), "<recipient address>"));
	}

}
```

It allows you to use a simple name for the template instead of the full path.

You can do the same for SMS by using `ogham.sms.template.path-prefix` and `ogham.sms.template.path-suffix`.
