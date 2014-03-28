package edu.sjsu.cmpe.procurement.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.ProcurementService;

import org.fusesource.stomp.codec.StompFrame;

/**
 * This job will run at every 5 second.
 */
@Every("5s")
public class ProcurementSchedulerJob extends Job {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doJob() {
	/*
	String strResponse = ProcurementService.jerseyClient.resource(
		"http://54.219.156.168").get(String.class);
	log.debug("Response from broker: {}", strResponse);
	*/
	ArrayList<Integer> orderBook = new ArrayList<Integer>();
	while(true) {
	    try {
		StompFrame received = ProcurementService.connection.receive();
		String content =  received.content().toString();
		log.info("message content: " + content);
		int isbn = Integer.parseInt(content.substring(content.lastIndexOf(":") + 1));
		orderBook.add(isbn);
		log.info("isbn added: " + isbn);
	    }
	    catch(IOException e) {
		return;
	    }
	}
    }
}
