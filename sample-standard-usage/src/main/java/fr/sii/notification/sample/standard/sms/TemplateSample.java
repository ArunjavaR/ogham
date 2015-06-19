package fr.sii.notification.sample.standard.sms;

import java.util.Properties;

import fr.sii.notification.context.SimpleBean;
import fr.sii.notification.core.builder.NotificationBuilder;
import fr.sii.notification.core.exception.NotificationException;
import fr.sii.notification.core.message.content.TemplateContent;
import fr.sii.notification.core.service.NotificationService;
import fr.sii.notification.sms.message.Sms;

public class TemplateSample {

	public static void main(String[] args) throws NotificationException {
		// configure properties (could be stored in a properties file or defined
		// in System properties)
		Properties properties = new Properties();
		properties.setProperty("notification.sms.smpp.host", "<your server host>");
		properties.setProperty("notification.sms.smpp.port", "<your server port>");
		properties.setProperty("notification.sms.smpp.systemId", "<your server system ID>");
		properties.setProperty("notification.sms.smpp.password", "<your server password>");
		properties.setProperty("notification.sms.from", "<phone number to display for the sender>");
		// Instantiate the notification service using default behavior and
		// provided properties
		NotificationService service = new NotificationBuilder().useAllDefaults(properties).build();
		// send the sms
		service.send(new Sms(new TemplateContent("classpath:/template/thymeleaf/simple.txt", new SimpleBean("foo", 42)), "<recipient phone number>"));
	}

}
