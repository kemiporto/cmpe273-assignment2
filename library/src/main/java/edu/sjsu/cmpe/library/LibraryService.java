package edu.sjsu.cmpe.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

import org.fusesource.stomp.client.BlockingConnection;
import org.fusesource.stomp.client.Stomp;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;

import javax.jms.TopicSession;
import javax.jms.Session;
import javax.jms.TopicConnection;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static BlockingConnection connection;

    private static String libraryName;

    public static TopicSession tSession;

    public static void main(String[] args) throws Exception {
	new LibraryService().run(args);
    }

    public static String getLibraryName() {
	return libraryName;
    }

    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
	bootstrap.setName("library-service");
	bootstrap.addBundle(new ViewBundle());
	bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(LibraryServiceConfiguration configuration,
	    Environment environment) throws Exception {
	libraryName = configuration.getLibraryName();
	// This is how you pull the configurations from library_x_config.yml
	String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	log.debug("{} - Queue name is {}. Topic name is {}",
		configuration.getLibraryName(), queueName,
		topicName);
	// TODO: Apollo STOMP Broker URL and login
	Stomp stomp = new Stomp(configuration.getApolloHost(), configuration.getApolloPort());
	stomp.setPasscode(configuration.getApolloPassword());
	stomp.setLogin(configuration.getApolloUser());
	connection = stomp.connectBlocking();

	/** Root API */
	environment.addResource(RootResource.class);
	/** Books APIs */
	BookRepositoryInterface bookRepository = new BookRepository();
	BookResource bookResource = new BookResource(bookRepository);
	environment.addResource(bookResource);

	/** UI Resources */
	environment.addResource(new HomeResource(bookRepository));

	StompJmsConnectionFactory factory =  new StompJmsConnectionFactory();
	factory.setBrokerURI("tcp://" + configuration.getApolloHost() + ":" + configuration.getApolloPort());
	factory.setUsername(configuration.getApolloUser());
	factory.setPassword(configuration.getApolloPassword());
	factory.setTopicPrefix("/topic/26642.books.");
	TopicConnection tConnection = factory.createTopicConnection();
	tConnection.start();
	tSession = tConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
	tSession.setMessageListener(bookResource);
    }
}
