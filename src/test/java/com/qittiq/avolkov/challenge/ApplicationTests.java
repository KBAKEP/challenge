package com.qittiq.avolkov.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@org.junit.jupiter.api.Test
	void test() throws Exception {

		// bad practice though
		Application.TIME_LIMIT = 5;

		Random random = new Random(100);

		for (int i = 0; i < 10; i++) {
			long unixTime = System.currentTimeMillis();
			long amount = random.nextInt(100);
			String json = "{\"amount\": " + amount + ", \"timestamp\":" + unixTime + "}";

			mockMvc.perform(
					post("/transactions")
							.contentType(MediaType.APPLICATION_JSON)
							.content(json))
					.andExpect(status().is(HttpStatus.NO_CONTENT.value()));

			TimeUnit.MILLISECONDS.sleep(1000);
		}

		this.mockMvc.perform(get("/statistics"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count", is(5)));

	}

}
