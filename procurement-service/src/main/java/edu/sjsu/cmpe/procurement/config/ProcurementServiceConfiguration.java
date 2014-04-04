package edu.sjsu.cmpe.procurement.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;

public class ProcurementServiceConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String stompQueueName;

   @NotEmpty
    @JsonProperty
    private String stompQueuePrefix;

    @NotEmpty
    @JsonProperty
    private String stompTopicPrefix;

    @JsonProperty
    private String apolloHost;

    @JsonProperty
    public int apolloPort;

    @JsonProperty
    public String apolloUser;

    @JsonProperty
    public String apolloPwd;
    
    @JsonProperty
    @NotEmpty
    private String procurementId;

    @JsonProperty
    @NotEmpty
    private String publisherResource;

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    /**
     * 
     * @return
     */
    public JerseyClientConfiguration getJerseyClientConfiguration() {
	return httpClient;
    }

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

    public String getStompTopicPrefix() {
	return stompTopicPrefix;
    }

    public void setStompTopicPrefix(String stompTopicPrefix) {
	this.stompTopicPrefix = stompTopicPrefix;
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

    public String getProcurementId() {
	return procurementId;
    }

    public void setprocurementId(String id) {
	procurementId = id;
    }

    public String getPublisherResource() {
	return publisherResource;
    }

    public void setPublicReesourde(String resource) {
	publisherResource = resource;
    }
}
