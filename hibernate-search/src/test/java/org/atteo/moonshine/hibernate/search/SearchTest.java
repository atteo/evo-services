/*
 * Copyright 2012 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.moonshine.hibernate.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.lucene.search.Query;
import org.atteo.moonshine.jta.Transaction;
import org.atteo.moonshine.tests.ServicesTest;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;

public class SearchTest extends ServicesTest {
	@Inject
	private EntityManagerFactory factory;

	@Before
	public void setup() throws ParseException, InterruptedException {
		Transaction.require(new Transaction.Runnable() {
			@Override
			public void run() {
				EntityManager manager = factory.createEntityManager();

				Author author = new Author();
				author.setName("Juliusz Słowacki");

				Book book = new Book();
				book.setTitle("Kordian");
				try {
					book.setPublicationDate(new SimpleDateFormat("yyyy-MM-dd").parse("1834-01-01"));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				book.getAuthors().add(author);

				manager.persist(author);
				manager.persist(book);
				manager.flush();
				manager.close();
			}
		});
	}

	@Test
	public void simple() {
		EntityManager manager = factory.createEntityManager();

		FullTextEntityManager index = Search.getFullTextEntityManager(manager);
		QueryBuilder builder = index.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();
		Query query = builder.keyword().onFields("title", "authors.name")
				.matching("Słowacki").createQuery();
		javax.persistence.Query persistenceQuery = index.createFullTextQuery(query, Book.class);
		List<?> result = persistenceQuery.getResultList();
		Book book = (Book) result.get(0);

		assertEquals("Kordian", book.getTitle());
		index.close();
	}
}
