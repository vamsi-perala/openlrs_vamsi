/**
 * Copyright 2014 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package org.apereo.openlrs.repositories.statements;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.*;

import java.sql.Types;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Random;

import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apereo.openlrs.model.Statement;
import org.apereo.openlrs.model.statement.XApiActor;
import org.apereo.openlrs.model.statement.XApiObject;
import org.apereo.openlrs.model.statement.XApiVerb;
import org.apereo.openlrs.repositories.Repository;
import org.apereo.openlrs.utils.StatementUtils;
import org.hibernate.Session;
import org.apereo.openlrs.model.statement.Stock;
import org.apereo.openlrs.Hibernate.HibernateUtil;

/**
 * @author ggilbert
 *
 */
@org.springframework.stereotype.Repository("DatabaseStatementRepository")
public class DatabaseStatementRepository implements Repository<Statement> {

	private static Map<String, Statement> store = new HashMap<String, Statement>();

	@Override
	public Statement post(Statement entity) {
		// store.put(entity.getKey(), entity);

		try {
			// hibernate insert
			System.out.println("Maven + Hibernate + MySQL");
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			System.out.println("statement saved");

			// entity.getResult().setStatement(entity);
			session.save(entity);
			session.getTransaction().commit();
			// session.flush();
			session.close();
			System.out.println("committed");
			// hibernate insert done
		} catch (Exception e) {

			e.printStackTrace();
			System.out.println("exception in insert");
		}

		return entity;
	}

	@Override
	public Statement get(Statement key) {
		// changed to fetch from db
		try {
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Criteria criteria = session.createCriteria(Statement.class).add(
					Restrictions.eq("id", key.getId()));
			List<Statement> statements = (List<Statement>) criteria.list();
			Iterator<Statement> iterator = statements.iterator();
			while (iterator.hasNext()) {
				return iterator.next();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception while retrieving statement list");
		}

		return store.get(key.getKey());
	}

	@Override
	public List<Statement> get() {

		List<Statement> stmts = new ArrayList<Statement>();

		try {
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Criteria criteria = session.createCriteria(Statement.class);
			stmts = (List<Statement>) criteria.list();

		} catch (Exception e) {

			e.printStackTrace();
			System.out.println("Exception while retrieving statement list");
		}

		return stmts;

	}

	@Override
	public List<Statement> get(Map<String, String> filters) {
		List<Statement> statements = get();
		if (statements != null && !statements.isEmpty()) {
			String actor = filters.get(StatementUtils.ACTOR_FILTER);
			String activity = filters.get(StatementUtils.ACTIVITY_FILTER);
			String related_activities = filters
					.get(StatementUtils.RELATED_ACTIVITIES_FILTER);
			List<Statement> filteredStatements = null;
			boolean related_flag;
			for (Statement statement : statements) {
				related_flag = false;
				if (StringUtils.isNotBlank(actor)
						&& StringUtils.isNotBlank(activity)) {
					// Here check if the related_activities is true and filter
					// accordingly-vamsi
					if (related_activities.equals("true")) {

						if (statement.toJSON().contains(activity)) {
							related_flag = true;
						}
					}
					if (related_flag
							|| statement.getObject().getId().equals(activity)) {

						List<Statement> stList = new ArrayList<Statement>();
						String homepage;
						String name;
						String mbox;

						try {
							mbox = (String) statement.getActor().getMbox();
						} catch (NullPointerException e) {
							System.out.println("mbox was null");
							mbox = null;
						}
						// TODO here I can simply place a check for account,
						// homepage.
						try {
							homepage = (String) statement.getActor()
									.getAccount().getHomePage();
						} catch (NullPointerException e) {
							System.out.println("homepage was null");
							homepage = null;
						}
						try {
							name = (String) statement.getActor().getAccount()
									.getName();
						} catch (NullPointerException e) {
							System.out.println("name was null");
							name = null;
						}
						if (actor.contains("mbox") && mbox != null) {
							if (actor.contains(statement.getActor().getMbox())) {
								if (filteredStatements == null) {
									filteredStatements = new ArrayList<Statement>();
								}
								filteredStatements.add(statement);
							}
						}
						if (actor.contains("homePage") && name != null
								&& homepage != null) {
							if (actor.contains(statement.getActor()
									.getAccount().getName())
									&& actor.contains(statement.getActor()
											.getAccount().getHomePage())) {

								if (filteredStatements == null) {
									filteredStatements = new ArrayList<Statement>();
								}
								filteredStatements.add(statement);
							}
						}

					}
				} else if (StringUtils.isNotBlank(actor)) {

					List<Statement> stList = new ArrayList<Statement>();
					String homepage;
					String name;
					String mbox;

					try {
						mbox = (String) statement.getActor().getMbox();
					} catch (NullPointerException e) {
						System.out.println("mbox was null");
						mbox = null;
					}
					
					try {
						homepage = (String) statement.getActor().getAccount()
								.getHomePage();
					} catch (NullPointerException e) {
						System.out.println("homepage was null");
						homepage = null;
					}
					try {
						name = (String) statement.getActor().getAccount()
								.getName();
					} catch (NullPointerException e) {
						System.out.println("name was null");
						name = null;
					}
					if (actor.contains("mbox") && mbox != null) {
						if (actor.contains(statement.getActor().getMbox())) {
							if (filteredStatements == null) {
								filteredStatements = new ArrayList<Statement>();
							}
							filteredStatements.add(statement);
						}
					}
					if (actor.contains("homePage") && name != null
							&& homepage != null) {
						if (actor.contains(statement.getActor().getAccount()
								.getName())
								&& actor.contains(statement.getActor()
										.getAccount().getHomePage())) {

							if (filteredStatements == null) {
								filteredStatements = new ArrayList<Statement>();
							}
							filteredStatements.add(statement);
						}
					}

					// stList=getByUser(actor);
					// Iterator it=stList.iterator();
					// while(it.hasNext())
					// filteredStatements.add((Statement)it.next());

				} else if (StringUtils.isNotBlank(activity)) {
					String oid = statement.getObject().getId();
					if (StringUtils.isNotBlank(related_activities)) {
						System.out.println("json is " + statement.toJSON());
						if (statement.toJSON().contains(activity)) {
							related_flag = true;
						}
					}
					if (related_flag || oid.equals(activity)) {
						if (filteredStatements == null) {
							filteredStatements = new ArrayList<Statement>();
						}

						filteredStatements.add(statement);
					}
				}
			}
			return filteredStatements;
		}
		return null;
	}

	@Override
	public List<Statement> getByUser(String userId) {
		List<Statement> statements = get();
		if (statements != null && !statements.isEmpty()) {
			List<Statement> filteredStatements = null;
			for (Statement statement : statements) {
				if (statement.toJSON().contains(userId)) {
					if (filteredStatements == null) {
						filteredStatements = new ArrayList<Statement>();
					}

					filteredStatements.add(statement);
				}
			}
			return filteredStatements;
		}
		return null;
	}

	@Override
	public List<Statement> getByContext(String context) {
		List<Statement> statements = get();
		if (statements != null && !statements.isEmpty()) {
			List<Statement> filteredStatements = null;
			for (Statement statement : statements) {
				if (statement.toJSON().contains(context)) {
					if (filteredStatements == null) {
						filteredStatements = new ArrayList<Statement>();
					}

					filteredStatements.add(statement);
				}
			}
			return filteredStatements;
		}
		return null;
	}

	@Override
	public List<Statement> getByContextAndUser(String context, String userId) {
		List<Statement> statements = get();
		if (statements != null && !statements.isEmpty()) {
			List<Statement> filteredStatements = null;
			for (Statement statement : statements) {
				if (statement.toJSON().contains(context)
						&& statement.toJSON().contains(userId)) {
					if (filteredStatements == null) {
						filteredStatements = new ArrayList<Statement>();
					}

					filteredStatements.add(statement);
				}
			}
			return filteredStatements;
		}
		return null;
	}

}
