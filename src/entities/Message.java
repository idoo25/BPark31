package entities;

import java.io.Serializable;

/**
 * This class represents a message between the server and the client for the
 * ParkB system. It contains the type of the message and its content.
 * 
 * @author ParkB Team
 * @version 1.0
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	// Class variables *************************************************
	/**
	 * The message type for parking system operations
	 */
	private MessageType type;

	/**
	 * The content of the message, such as ParkingOrder objects, subscriber codes,
	 * etc.
	 */
	private Serializable content;

	/**
	 * The message type enumeration for parking system operations.
	 */
	public enum MessageType {
		// General operations
		REGISTER_SUBSCRIBER,
		REGISTRATION_RESPONSE,
		GENERATE_USERNAME,
		USERNAME_RESPONSE,
		SUBSCRIBER_LOGIN,
		SUBSCRIBER_LOGIN_RESPONSE,

		// Parking operations
		CHECK_PARKING_AVAILABILITY,
		PARKING_AVAILABILITY_RESPONSE,
		RESERVE_PARKING,
		RESERVATION_RESPONSE,
		ENTER_PARKING,
		ENTER_PARKING_RESPONSE,
		EXIT_PARKING,
		EXIT_PARKING_RESPONSE,
		EXTEND_PARKING,
		EXTEND_PARKING_RESPONSE,
		REQUEST_LOST_CODE,
		LOST_CODE_RESPONSE,
		GET_PARKING_HISTORY,
		PARKING_HISTORY_RESPONSE,

		// Manager operations
		MANAGER_LOGIN,
		MANAGER_LOGIN_RESPONSE,
		GET_ACTIVE_PARKINGS,
		ACTIVE_PARKINGS_RESPONSE,
		MANAGER_GET_REPORTS,
		MANAGER_SEND_REPORTS,
		UPDATE_SUBSCRIBER_INFO,
		UPDATE_SUBSCRIBER_RESPONSE,
		GENERATE_MONTHLY_REPORTS,
		MONTHLY_REPORTS_RESPONSE,

		// Reservations
		GET_TIME_SLOTS,
		TIME_SLOTS_RESPONSE,
		MAKE_PREBOOKING,
		PREBOOKING_RESPONSE,
		SPONTANEOUS_PARKING,
		SPONTANEOUS_RESPONSE,
		REQUEST_EXTENSION,
		EXTENSION_RESPONSE,
		GET_SYSTEM_STATUS,
		SYSTEM_STATUS_RESPONSE,

		// Activation & cancellation
		ACTIVATE_RESERVATION,
		ACTIVATION_RESPONSE,
		CANCEL_RESERVATION,
		CANCELLATION_RESPONSE,

		// Kiosk-specific operations
		KIOSK_LOGIN_RESPONSE,
		KIOSK_RF_LOGIN,
		KIOSK_ID_LOGIN,
		ENTER_PARKING_KIOSK,
		ENTER_PARKING_KIOSK_RESPONSE,
		RETRIEVE_CAR_KIOSK,
		RETRIEVE_CAR_KIOSK_RESPONSE,
		FORGOT_CODE_KIOSK,
		FORGOT_CODE_KIOSK_RESPONSE,
		ACTIVATE_RESERVATION_KIOSK,
		ACTIVATE_RESERVATION_KIOSK_RESPONSE,

		// Additional subscriber operations
		GET_SUBSCRIBER_BY_NAME,
		GET_ALL_SUBSCRIBERS,
		SHOW_ALL_SUBSCRIBERS,
		SHOW_SUBSCRIBER_DETAILS,
		REQUEST_SUBSCRIBER_DATA,
		SUBSCRIBER_DATA_RESPONSE
	}

	// Constructors ******************************************************
	/**
	 * Constructs a new Message with the specified type and content.
	 * 
	 * @param type    the type of the message
	 * @param content the content of the message
	 */
	public Message(MessageType type, Serializable content) {
		this.setType(type);
		this.setContent(content);
	}

	// Methods ***********************************************************
	/**
	 * Returns the type of the message.
	 * 
	 * @return the type of the message
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Sets the type of the message.
	 * 
	 * @param type the new type of the message
	 */
	public void setType(MessageType type) {
		this.type = type;
	}

	/**
	 * Returns the content of the message.
	 * 
	 * @return the content of the message
	 */
	public Serializable getContent() {
		return content;
	}

	/**
	 * Sets the content of the message.
	 * 
	 * @param content the new content of the message
	 */
	public void setContent(Serializable content) {
		this.content = content;
	}
}