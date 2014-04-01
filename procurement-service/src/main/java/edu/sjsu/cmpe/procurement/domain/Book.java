package edu.sjsu.cmpe.procurement.domain;

import java.net.URL;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

public class Book {
    public enum Status {
	available, checkedout, lost;
    }

    @NotNull
    private long isbn;
    @NotEmpty
    private String title;
    @NotEmpty
    private String category;
    private URL coverimage;
    private Status status = Status.available;
    /**
     * @return the isbn
     */
    public long getIsbn() {
	return isbn;
    }

    /**
     * @param isbn
     *            the isbn to set
     */
    public void setIsbn(long isbn) {
	this.isbn = isbn;
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    public String getCategory() {
	return category;
    }

    public void setCategory(String category) {
	this.category =  category;
    }

    public URL getCoverimage() {
	return coverimage; 
    }

    public void setCoverimage(URL coverimage) {
	this.coverimage = coverimage;
    }

    public Status getStatus() {
	return status;
    }

    public void setStatus(Status status) {
	this.status = status;
    }
}
