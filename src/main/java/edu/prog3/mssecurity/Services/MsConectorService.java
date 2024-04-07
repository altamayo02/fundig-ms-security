package edu.prog3.mssecurity.Services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class MsConectorService {
    
    private final String URL;
    private String body;
    private RestTemplate restTemplate;
    HttpHeaders headers = new HttpHeaders();

    public MsConectorService(String url, String body) {
        this.URL=url;
        this.body=body;
        this.headers.setContentType(MediaType.APPLICATION_JSON);

        this.restTemplate = new RestTemplate();
        
    }

    public void consumirServicio() {
        HttpEntity<String> httpEntity= new HttpEntity<>(body, headers);

        String resultado = restTemplate.postForObject(URL,httpEntity, String.class);
        System.out.println(resultado);
    }
}
