package edu.prog3.mssecurity.Services;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class HttpService {
    
    @Value("${url.notification}")
    private String url;


    public String consumePostNotification(String ruta, JSONObject body) {

        String answer="";

        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity= new HttpEntity<>(body.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();

        URI url;
        try {
            url = new URI(this.url);
            answer = restTemplate.postForObject(url+ruta,httpEntity, String.class);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            answer=e.toString();
            e.printStackTrace();
        }

        return answer;
    }

}
