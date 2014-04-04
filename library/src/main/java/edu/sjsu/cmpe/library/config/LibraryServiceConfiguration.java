package edu.sjsu.cmpe.library.config;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class LibraryServiceConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String stompQueueName;

    @NotEmpty
    @JsonProperty
    private String stompQueuePrefix;

   @NotEmpty
    @JsonProperty
    private String stompTopicName;

    @JsonProperty
    private String stompTopicPrefix;

    @NotEmpty
    @JsonProperty
    private String libraryName;

    @JsonProperty
    private String apolloHost;

    @JsonProperty
    public int apolloPort;

    @JsonProperty
    public String apolloUser;

    @JsonProperty
    public String apolloPwd;
    
    /**
     * @return the stompQueueName
     */
    public String getStompQueueName() {
	return stompQueueName;
    }

    /**
     * @param stompQueueName
     *            the stompQueueName to set
     */
    public void setStompQueueName(String stompQueueName) {
	this.stompQueueName = stompQueueName;
    }

    public String  getStompQueuePrefix() {
	return stompQueuePrefix;
    }

    public void setStompQueuePrefix(String prefix) {
	stompQueuePrefix = prefix;
    }

    /**
     * @return the stompTopicName
     */
    public String getStompTopicName() {
	if(stompTopicName == null || stompTopicName.isEmpty()) {
	    return stompTopicPrefix + "*";
	}
	return stompTopicName;
    }

    /**
     * @param stompTopicName
     *            the stompTopicName to set
     */
    public void setStompTopicName(String stompTopicName) {
	this.stompTopicName = stompTopicName;
    }

    /**
     * @return the libraryName
     */
    public String getLibraryName() {
	return libraryName;
    }

    public String getStompTopicPrefix() {
	return stompTopicPrefix;
    }

    public void setStompTopicPrefix(String stompTopicPrefix) {
	this.stompTopicPrefix = stompTopicPrefix;
    }

    /**
     * @param libraryName
     *            the libraryName to set
     */
    public void setLibraryName(String libraryName) {
	this.libraryName = libraryName;
    }
    public String getApolloHost() {
	return apolloHost;
    }

    public void setApolloHost(String host) {
	apolloHost = host;
    }

    public int getApolloPort() {
	return apolloPort;
    }

    public void setApolloPort(int port) {
	apolloPort = port;
    }

    public String getApolloUser() {
	return apolloUser;
    }

    public void setApolloUser(String user) {
	apolloUser = user;
    }

    public String getApolloPassword() {
	return apolloPwd;
    }

    public void setApolloPassword(String pwd) {
	apolloPwd = pwd;
    }

}
