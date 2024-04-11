package edu.prog3.mssecurity.Services;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class HttpService {
    
    private final URI URL;
    private String body;
    private RestTemplate restTemplate;
    HttpHeaders headers = new HttpHeaders();

    public HttpService(String url, String body) throws URISyntaxException {
        this.URL = new URI(url);
        this.body=body;
        this.headers.setContentType(MediaType.APPLICATION_JSON);

        this.restTemplate = new RestTemplate();
        
    }

    public void consumePostService() {
        HttpEntity<String> httpEntity= new HttpEntity<>(this.body, this.headers);

        String answer = restTemplate.postForObject(this.URL,httpEntity, String.class);
        System.out.println("HttpService: " + answer);
    }
}
