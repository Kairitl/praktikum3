package ee.ttu.idu0080.raamatupood.client;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import ee.ttu.idu0080.raamatupood.client.Consumer.MessageListenerImpl;
import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimusRida;
import ee.ttu.idu0080.raamatupood.types.Toode;

public class Producer {
	private static final Logger log = Logger.getLogger(Producer.class);
	private String SEND_ORDER = "tellimuse.edastamine"; // järjekorra nimi
	private String RECEIVE_ANSWER = "tellimuse.vastus";

	private String user = ActiveMQConnection.DEFAULT_USER;// brokeri jaoks vaja
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;

	long sleepTime = 1000; // 1000ms
	
	private long timeToLive = 1000000;
	private String url = EmbeddedBroker.URL;

	public static void main(String[] args) {
		Producer producerTool = new Producer();
		producerTool.run();
	}

	public void run() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + url);
			log.debug("Sleeping between publish " + sleepTime + " ms");
			if (timeToLive != 0) {
				log.debug("Messages time to live " + timeToLive + " ms");
			}
			
			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			// Käivitame yhenduse
			connection.start();

			// 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SEND_ORDER);

			// 3. Loome teadete saatja
			MessageProducer producer = session.createProducer(destination);

			// producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setTimeToLive(timeToLive);

			// 4. teadete saatmine 
			sendTellimus(session, producer);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	protected void sendTellimus(Session session, MessageProducer producer)
			throws Exception {
		
		Toode iPhone = new Toode(5432344, "iPhone X", "1494.99");
		TellimusRida iPhoneOrder = new TellimusRida(iPhone,  3L);
		
		Toode jblCharge = new Toode(23456567, "JBL CHARGE 3", "159.95");
		TellimusRida jblChargeOrder = new TellimusRida(jblCharge, 2L);
		
		Tellimus tell = new Tellimus();
		tell.addTellimusRida(iPhoneOrder);
		tell.addTellimusRida(jblChargeOrder);
		
		ObjectMessage objectMessage = session.createObjectMessage();
		objectMessage.setObject(tell); // peab olema Serializable
		log.debug("Sending message: Consumer");
		producer.send(objectMessage);
		
		// ootab 1 sekundi
		Thread.sleep(sleepTime);
		
		getAnswer(session);
	}
	
	protected void getAnswer(Session session) {
		try {
			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(RECEIVE_ANSWER);
			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);
			log.info("Consuming queue: " + RECEIVE_ANSWER);
			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	class MessageListenerImpl implements javax.jms.MessageListener {

		public void onMessage(Message message) {
			try {
				TextMessage txtMsg = (TextMessage) message;
				String msg = txtMsg.getText();
				log.info("Received: " + msg);
			} catch (JMSException e) {
				log.warn("Caught: " + e);
				e.printStackTrace();
			}
		}
	}

	class ExceptionListenerImpl implements javax.jms.ExceptionListener {

		public synchronized void onException(JMSException ex) {
			log.error("JMS Exception occured. Shutting down client.");
			ex.printStackTrace();
		}
	}
}


