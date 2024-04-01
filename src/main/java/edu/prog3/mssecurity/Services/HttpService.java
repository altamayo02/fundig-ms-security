package main.java.edu.prog3.mssecurity.Services;

public class HttpService {
    @PostMapping
	public void call(String URI, String requestBody) {   
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(URI))
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