package edu.prog3.mssecurity.Services;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class HttpService {
    @Value("${url.notification}")
    private String url;


    public ResponseEntity<String> postNotification(String route, JSONObject body) {
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(body.toString(), headers);
		
        ResponseEntity<String> response;
        try {
            URI url = new URI(this.url);
			RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.postForEntity(url + route, httpEntity, String.class);
        } catch (URISyntaxException e) {
            response = new ResponseEntity<>(e.toString(), headers, HttpStatus.FAILED_DEPENDENCY);
            e.printStackTrace();
        }
		
        return response;
    }

}
