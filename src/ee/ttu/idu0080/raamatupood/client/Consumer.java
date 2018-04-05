package ee.ttu.idu0080.raamatupood.client;

import java.io.Serializable;
import java.math.BigDecimal;
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

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimusRida;

public class Consumer {
	private static final Logger log = Logger.getLogger(Consumer.class);
	private String SEND_ORDER = "tellimuse.edastamine";
	private String RECEIVE_ANSWER = "tellimuse.vastus";
	
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String url = EmbeddedBroker.URL;
	
	private long timeToLive = 1000000;

	public static void main(String[] args) {
		Consumer consumerTool = new Consumer();
		consumerTool.run();
	}

	public void run() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + url);
			log.info("Consuming queue : " + SEND_ORDER);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();

			// Kui ühendus kaob, lõpetatakse Consumeri töö veateatega.
			connection.setExceptionListener(new ExceptionListenerImpl());

			// Käivitame ühenduse
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

			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);

			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	protected void sendAnswer(BigDecimal sum, Long totalItems) {
		Connection connection = null;
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		try {
			connection = connectionFactory.createConnection();
			connection.start();

			Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

			Destination destination = session.createQueue(RECEIVE_ANSWER);

			MessageProducer producer = session.createProducer(destination);

			producer.setTimeToLive(timeToLive);
			
			TextMessage message = session
					.createTextMessage("toodete arv kokku: " + totalItems + " summa kokku " + sum);
			producer.send(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Käivitatakse, kui tuleb sõnum
	 */
	class MessageListenerImpl implements javax.jms.MessageListener {

		public void onMessage(Message message) {
			try {
				ObjectMessage objectMessage = (ObjectMessage) message;
				String msg = objectMessage.getObject().toString();
				Tellimus tellimus = (Tellimus) objectMessage.getObject();
		
				BigDecimal sum = BigDecimal.ZERO;
				long totalItems = 0;
				for(TellimusRida rida : tellimus.getTellimusRead()) {
					sum = sum.add(rida.toode.hind.multiply(BigDecimal.valueOf(rida.kogus)));
					totalItems = totalItems + rida.kogus;
					log.info("Received item: " + rida.toode.nimetus);
					log.info("id: " + rida.toode.kood);
					log.info("hind: " + rida.toode.hind);
					log.info("kogus: " + rida.kogus);
				}
				log.info("Sending message: toodete arv kokku: " + totalItems + " summa kokku " + sum);

				sendAnswer(sum, totalItems);

			
			} catch (JMSException e) {
				log.warn("Caught: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Käivitatakse, kui tuleb viga.
	 */
	class ExceptionListenerImpl implements javax.jms.ExceptionListener {

		public synchronized void onException(JMSException ex) {
			log.error("JMS Exception occured. Shutting down client.");
			ex.printStackTrace();
		}
	}

}