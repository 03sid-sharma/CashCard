package example.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
// @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldNotReturnACashCardWithAnUnknownId(){
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards/1000", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateANewCashCard(){
		CashCard newCashCard = new CashCard(null, 250.00, null);
		ResponseEntity<Void> createResponse = restTemplate
			.withBasicAuth("sid", "abc123")
			.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity(locationOfNewCashCard, String.class);
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
		// if Dirties context is missing test will fail as we have already created 1 cashcard above for 250.00 amount 
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards",  new CashCard(null, 250.00, "sid"), Void.class);
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 123.45, "sid"), Void.class);
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 1.00, "sid"), Void.class);

		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards", String.class); 
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		// in db table must have 4 cards
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		// their ids must be
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(101, 100, 99);

		// amount values must be
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(250.00, 1.00, 123.45);
	}

	@Test
	void shouldReturnAPageOfCashCards(){
		// Add sample data in db dirties context will delete it :)
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 250.00, "sid"), Void.class);
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 123.45, "sid"), Void.class);
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 1.00, "sid"), Void.class);
		
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues(){
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 250.00, "sid"), Void.class);
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 123.45, "sid"), Void.class);
		// restTemplate.withBasicAuth("sid", "abc123").postForEntity("/cashcards", new CashCard(null, 1.00, "sid"), Void.class);

		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amount = documentContext.read("$..amount");
		assertThat(amount).containsExactly(250.00, 123.45, 1.00);
	}

	@Test
	void shouldNotReturnCashCardWithBadCreds(){
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("BAD-USER", "abc123")
			.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate.withBasicAuth("sid", "BAD-PWD")
				.getForEntity("/cashcards",
				String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners(){
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("hankOwnsNoCards", "qwerty")
			.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardsNotOwned(){
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards/102", String.class); // hardik's giftcard
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard(){
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("sid", "abc123")
			.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
			.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(19.99);
	}	

	@Test
	void shouldNotUpdateInvalidCashCard(){
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sid", "abc123")
				.exchange("/cashcards/99999999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateCashCardOfOtherOwner() {
		CashCard cashCardUpdate = new CashCard(null, 333.33, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sid", "abc123") // logged in with sid
				.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class); // hardik's gift card
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard(){
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sid", "abc123")
			.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sid", "abc123")
			.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteInvalidCashCard(){
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sid","abc123")
			.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfCashCardsNotOwned(){
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sid", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("hardik", "xyz123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
