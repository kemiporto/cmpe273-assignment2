package edu.sjsu.cmpe.procurement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import de.spinscale.dropwizard.jobs.JobsBundle;
import edu.sjsu.cmpe.procurement.api.resources.RootResource;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsConnection;
import org.fusesource.stomp.jms.StompJmsQueue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;

public class ProcurementService extends Service<ProcurementServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * FIXME: THIS IS A HACK!
     */
    public static Client jerseyClient;

    public static QueueSession queueSession;
    public static javax.jms.MessageConsumer consumer;
    public static StompJmsConnectionFactory factory = null;
    public static TopicSession tSession;
    public static ProcurementServiceConfiguration configuration;

    public static void main(String[] args) throws Exception {
	new ProcurementService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProcurementServiceConfiguration> bootstrap) {
	bootstrap.setName("procurement-service");
	/**
	 * NOTE: All jobs must be placed under edu.sjsu.cmpe.procurement.jobs
	 * package
	 */
	bootstrap.addBundle(new JobsBundle("edu.sjsu.cmpe.procurement.jobs"));
    }

    @Override
    public void run(ProcurementServiceConfiguration configuration,
	    Environment environment) throws Exception {
	this.configuration = configuration;

	log.info("host: " + configuration.getApolloHost());
	log.info("port: " + configuration.getApolloPort());
	jerseyClient = new JerseyClientBuilder()
	    .using(configuration.getJerseyClientConfiguration())
	    .using(environment)
	    .build();
	/**
	 * Root API - Without RootResource, Dropwizard will throw this
	 * exception:
	 * 
	 * ERROR [2013-10-31 23:01:24,489]
	 * com.sun.jersey.server.impl.application.RootResourceUriRules: The
	 * ResourceConfig instance does not contain any root resource classes.
	 */
	environment.addResource(RootResource.class);

	factory = new StompJmsConnectionFactory();
	factory.setBrokerURI("tcp://" + configuration.getApolloHost() + ":" + configuration.getApolloPort());
	factory.setUsername(configuration.getApolloUser());
	factory.setPassword(configuration.getApolloPassword());
	factory.setQueuePrefix(configuration.getStompQueuePrefix());
	factory.setTopicPrefix(configuration.getStompTopicPrefix());

	StompJmsConnection connection = (StompJmsConnection) factory.createConnection();
	connection.start();
	QueueSession session = connection.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

	consumer = session.createConsumer
	    (new StompJmsQueue(connection, 
			       configuration.getStompQueueName()
			       .replaceFirst(configuration.getStompQueuePrefix(), "")));

	tSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    }

}
