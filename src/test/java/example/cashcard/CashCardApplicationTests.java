package example.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldNotReturnACashCardWithAnUnknownId(){
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	void shouldCreateANewCashCard(){
		CashCard newCashCard = new CashCard(null, 250.00);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isNotNull();
		
		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(250.00);
	}

	/* Please see shouldCreateANewCashCard test as this works on static data... */
	// @Test
	// void shouldReturnATestCardWhenDataIsSaved(){
	// ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1",
	// String.class);

	// assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

	// DocumentContext documentContext = JsonPath.parse(response.getBody());
	// Number id = documentContext.read("$.id");
	// assertThat(id).isEqualTo(1);

	// Double amount = documentContext.read("$.amount");
	// assertThat(amount).isEqualTo(250.00);
	// }

	@Test
	void shouldReturnAllCashCardsWhenListIsRequested(){
		// We have 1 cashcard created above for 250 amount rest 2 we'll add :)
		restTemplate.postForEntity("/cashcards", new CashCard(null, 123.45), Void.class);
		restTemplate.postForEntity("/cashcards", new CashCard(null, 1.00), Void.class);

		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class); 
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(1, 2, 3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(250.00, 1.00, 123.45);
	}
}
