package com.qittiq.avolkov.challenge;


import com.qittiq.avolkov.challenge.model.Statistics;
import com.qittiq.avolkov.challenge.model.Transaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

@SpringBootApplication
@EnableScheduling
public class Application {

	private static final ConcurrentSkipListSet<Transaction> TRANSACTIONS =
			new ConcurrentSkipListSet<>(Comparator.comparing((Transaction::getTimestamp)));

	static int TIME_LIMIT = 60;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@RestController
	public class RestAPIController {

		@PostMapping("/transactions")
		public ResponseEntity saveTransaction(@RequestBody Transaction transaction) {
			// TODO save Transactions to repository or do whatever you need
			TRANSACTIONS.add(transaction);
			return ResponseEntity.noContent().build();
		}

		@GetMapping("/statistics")
		public ResponseEntity<Statistics> getStatistics() {
			Instant statisticsTime = Instant.now().minus(TIME_LIMIT, ChronoUnit.SECONDS);
			Iterator<Transaction> iterator = TRANSACTIONS.descendingIterator();
			int size = 0;
			long sum = 0;
			long min = Long.MAX_VALUE;
			long max = 0;
			boolean fitTimeLimit = true;
			while (fitTimeLimit && iterator.hasNext()) {
				Transaction transaction = iterator.next();
				size++;
				long amount = transaction.getAmount();
				sum += amount;
				if (amount > max) {
					max = amount;
				}
				if (amount < min) {
					min = amount;
				}
				Instant transactionTime = Instant.ofEpochMilli(transaction.getTimestamp());
				if (transactionTime.isBefore(statisticsTime)) {
					fitTimeLimit = false;
				}
			}

			Statistics statistics = Statistics.builder()
					.count(size)
					.sum(sum)
					.max(max)
					.min(min)
					.avg(size != 0 ? sum / size : 0)
					.build();
			return ResponseEntity.ok(statistics);
		}
	}

	// cleanup in order to keep good performance
	// TODO remove if you need full history and you have infinite RAM
	@Scheduled(fixedRate = 1000)
	public void cleanQueueTail() {
		long transactionCleanupTime = TIME_LIMIT * 2;
		Instant cleanupTime = Instant.now().minus(transactionCleanupTime, ChronoUnit.SECONDS);
		Iterator<Transaction> tailIterator = TRANSACTIONS.iterator();
		while (tailIterator.hasNext()) {
			Transaction transaction = tailIterator.next();
			Instant transactionTime = Instant.ofEpochMilli(transaction.getTimestamp());
			if (transactionTime.isBefore(cleanupTime)) {
				tailIterator.remove();
			} else {
				return;
			}
		}
	}
}
