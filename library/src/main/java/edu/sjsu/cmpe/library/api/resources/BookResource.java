package edu.sjsu.cmpe.library.api.resources;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.BooksDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.LibraryService;

import org.fusesource.stomp.codec.StompFrame;
import static org.fusesource.stomp.client.Constants.*;
import static org.fusesource.hawtbuf.Buffer.ascii;
import org.fusesource.stomp.jms.message.StompJmsTextMessage;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.AsciiBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource implements MessageListener{
    /** bookRepository instance */
    private final BookRepositoryInterface bookRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * BookResource constructor
     * 
     * @param bookRepository
     *            a BookRepository instance
     */
    public BookResource(BookRepositoryInterface bookRepository) {
	this.bookRepository = bookRepository;
    }

    @GET
    @Path("/{isbn}")
    @Timed(name = "view-book")
    public BookDto getBookByIsbn(@PathParam("isbn") LongParam isbn) {
	Book book = bookRepository.getBookByISBN(isbn.get());
	BookDto bookResponse = new BookDto(book);
	bookResponse.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(),
		"GET"));
	bookResponse.addLink(new LinkDto("update-book-status", "/books/"
		+ book.getIsbn(), "PUT"));
	// add more links

	return bookResponse;
    }

    @POST
    @Timed(name = "create-book")
    public Response createBook(@Valid Book request) {
	// Store the new book in the BookRepository so that we can retrieve it.
	Book savedBook = bookRepository.saveBook(request);

	String location = "/books/" + savedBook.getIsbn();
	BookDto bookResponse = new BookDto(savedBook);
	bookResponse.addLink(new LinkDto("view-book", location, "GET"));
	bookResponse
	.addLink(new LinkDto("update-book-status", location, "PUT"));

	return Response.status(201).entity(bookResponse).build();
    }

    @GET
    @Path("/")
    @Timed(name = "view-all-books")
    public BooksDto getAllBooks() {
	BooksDto booksResponse = new BooksDto(bookRepository.getAllBooks());
	booksResponse.addLink(new LinkDto("create-book", "/books", "POST"));

	return booksResponse;
    }

    @PUT
    @Path("/{isbn}")
    @Timed(name = "update-book-status")
    public Response updateBookStatus(@PathParam("isbn") LongParam isbn,
	    @DefaultValue("available") @QueryParam("status") Status status) {
	if(status == Status.lost) {
	    StompJmsTextMessage stompMessage = new StompJmsTextMessage();
	    try {
		stompMessage.setText(LibraryService.getLibraryName() + ":" + isbn);
		LibraryService.producer.send(stompMessage);
	    }
	    catch (Exception e) {
		return Response.status(500).build();
	    }
	}
	Book book = bookRepository.getBookByISBN(isbn.get());
	book.setStatus(status);

	BookDto bookResponse = new BookDto(book);
	String location = "/books/" + book.getIsbn();
	bookResponse.addLink(new LinkDto("view-book", location, "GET"));

	return Response.status(200).entity(bookResponse).build();
    }

    @DELETE
    @Path("/{isbn}")
    @Timed(name = "delete-book")
    public BookDto deleteBook(@PathParam("isbn") LongParam isbn) {
	bookRepository.delete(isbn.get());
	BookDto bookResponse = new BookDto(null);
	bookResponse.addLink(new LinkDto("create-book", "/books", "POST"));

	return bookResponse;
    }
    public void onMessage(Message message) {
	try {
	    String tMessage = ((TextMessage) message).getText();
	    log.info("Receiving message {}", tMessage);
	    Pattern pattern = Pattern.compile(":");
	    String[] split = pattern.split(tMessage, 4);
	    String isbn = split[0];
	    String title = split[1];
	    String category = split[2];
	    String coverImage = split[3];
	    log.info("received book from publisher: isbn: {}, title: {}, category: {}, coverimage: {}",
		     isbn, title, category, coverImage);
	    Long lIsbn = (long) Integer.parseInt(isbn);
	    Book book = bookRepository.getBookByISBN(lIsbn);
	    
	    if(book != null && book.getStatus() == Status.lost) {
		log.info("changing book {} status to available", book.getTitle());
		book.setStatus(Status.available);
	    } else  if (book == null){
		book = new Book();
		book.setTitle(title);
		book.setCategory(category);
		book.setCoverimage(new URL(coverImage));
		book.setIsbn(lIsbn);
		((BookRepository) bookRepository).saveBookWithIsbn(book);
		log.info("added new book {}", book);
	    }
	} catch (Exception e) {
	    log.info("Exception " + e.getClass() + ":" +  e.getMessage());
	    throw new RuntimeException(e);
	}
    }
}

