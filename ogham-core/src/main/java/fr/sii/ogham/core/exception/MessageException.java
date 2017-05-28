package fr.sii.ogham.core.exception;

import static fr.sii.ogham.core.CoreConstants.SERIAL_VERSION_UID;

import fr.sii.ogham.core.message.Message;

public class MessageException extends MessagingException {
	private static final long serialVersionUID = SERIAL_VERSION_UID;
	
	private final transient Message message;
	
	public MessageException(String message, Message msg, Throwable cause) {
		super(message, cause);
		this.message = msg;
	}

	public MessageException(String message, Message msg) {
		super(message);
		this.message = msg;
	}

	public MessageException(Throwable cause, Message msg) {
		super(cause);
		this.message = msg;
	}

	public Message getOghamMessage() {
		return message;
	}
}
