package edu.sjsu.cmpe.procurement.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.jms.TopicPublisher;
import javax.jms.Session;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.ProcurementService;
import edu.sjsu.cmpe.procurement.domain.Book;

import org.fusesource.stomp.codec.StompFrame;
import org.fusesource.stomp.jms.message.StompJmsMessage;
import org.fusesource.stomp.jms.message.StompJmsTextMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This job will run every 5 minutes.
 */
@Every("5min")
public class ProcurementSchedulerJob extends Job {
    private final Logger log = LoggerFactory.getLogger(getClass());

    static class BookOrderRequest {
	private static final ObjectMapper mapper = new ObjectMapper();

	public String getId() {
	    return ProcurementService.configuration.getProcurementId();
	}

	@JsonProperty("order_book_isbns")
	private ArrayList<Integer> ordeBookIsbns;

	@JsonProperty("order_book_isbns")
	public ArrayList<Integer> getOrderBookIsbns() {
	    return ordeBookIsbns;
	}

	public BookOrderRequest(ArrayList<Integer> isbns) {
	    ordeBookIsbns = isbns;
	}

	public byte[] toJsonByteArray() throws IOException {
	    return mapper.writeValueAsBytes(this);
	}
    }

    static class BookOrderResponse {
	private String msg;
	public String getMsg() {
	    return msg;
	}

	public void setMsg(String msg) {
	    this.msg = msg;
	}

	public String toString() {
	    return msg;
	}
    }

    static class ShippedBooks {
	private static final ObjectMapper mapper = new ObjectMapper();

	@JsonProperty("shipped_books")
	private ArrayList<Book> shippedBooks;

	public ArrayList<Book> getShippedBooks() {
	    return shippedBooks;
	}

	public void setShippedBooks(ArrayList<Book> shippedBooks) {
	    this.shippedBooks = shippedBooks;
	}

	public String toString() {
	    try {
		return mapper.writeValueAsString(this);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
    }

    private void postOrderBook(ArrayList<Integer> order) throws Exception {
	log.info("sending POST request to publisher");
	BookOrderResponse response = ProcurementService.jerseyClient
	    .resource("http://54.193.56.218:9000/orders")
	    .entity(new BookOrderRequest(order).toJsonByteArray(), "application/json")
	    .post(BookOrderResponse.class);
	log.info("Response from broker: {}", response);
    }

    @Override
    public void doJob() {
	log.info("doJob()");
	// Synchronized here as consumer might not be thread-safe.
	synchronized (ProcurementService.consumer) {
	    try {
		// Part 1
		ArrayList<Integer> orderBook = new ArrayList<Integer>();
		for(;;) {
		    StompJmsMessage received;
		    try {
			// Try to receive a message from the broker, waiting 2s. If nothing is received
			// then we assume there are no more messages in the queue for us.
			received =
			    (StompJmsMessage) ProcurementService.consumer.receive(2000);
		    } catch (ClassCastException e) {
			log.info("Unexpected message type: {}.", e);
			continue;
		    }

		    if (received == null) {
			log.info("No new messages. Exiting due to timeout");
		     	break;
		    }

		    String body = received.getFrame().contentAsString();
		    log.info("Received {}", body);
		    int isbn = Integer.parseInt(body.substring(body.lastIndexOf(":") + 1));
		    orderBook.add(isbn);
		    log.info("isbn added: " + isbn);
		}

		if(!orderBook.isEmpty()) {
		    postOrderBook(orderBook);
		}
	    } catch (Exception e) {
		log.info("Exception {}: {}.", e.getClass(), e.getMessage());
		throw new RuntimeException(e);
	    }

	    // Part2
	    log.info("getting shipped books from publisher");
	    ShippedBooks response = null;
	    try {
		response = ProcurementService.jerseyClient
		    .resource(ProcurementService.configuration.getPublisherResource())
		    .get(ShippedBooks.class);
		log.info("response from publisher: {}", response);

		for(Book b : response.getShippedBooks()) {
		    TopicPublisher publisher = 
			ProcurementService.tSession.createPublisher(
			    ProcurementService.tSession.createTopic(b.getCategory()));
		    String message = b.getIsbn() + ":" + b.getTitle() + ":" + b.getCategory() + ":";
		    if(b.getCoverimage() != null) {
			message += b.getCoverimage();
		    } 
		    StompJmsTextMessage stompMessage = new StompJmsTextMessage();
		    stompMessage.setText(message);
		    publisher.publish(stompMessage);
		}
	    }
	    catch (Exception e) {
		log.info("Exception {}: {}.", e.getClass(), e.getMessage());
		throw new RuntimeException(e);
	    }
	}
    }
}
