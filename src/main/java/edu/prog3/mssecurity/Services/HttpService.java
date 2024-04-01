package edu.prog3.mssecurity.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class HttpService {
    @PostMapping
	public void call(String URL, String requestBody) {   
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URL))
				.method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response.body());
	}
}