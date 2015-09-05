package org.apereo.openlrs.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apereo.openlrs.Hibernate.HibernateUtil;
import org.apereo.openlrs.exceptions.StatementStateConflictException;
import org.apereo.openlrs.model.Statement;
import org.apereo.openlrs.model.StatementResult;
import org.apereo.openlrs.model.statement.XApiActor;
import org.apereo.openlrs.repositories.statements.StatementRepositoryFactory;
import org.apereo.openlrs.utils.TimestampUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatementService {
	private Logger log = Logger.getLogger(StatementService.class);

	@Autowired
	StatementRepositoryFactory factory;

	public Statement getStatement(String id) {

		Statement key = new Statement();
		key.setId(id);

		return factory.getRepository().get(key);
	}

	/**
	 * Gets statement objects for the specified criteria
	 * 
	 * @param filterMap
	 *            a hashmap of the filtering criteria
	 * @return JSON string of the statement objects
	 */
	public StatementResult getStatement(Map<String, String> filterMap) {

		StatementResult result = null;
		if (filterMap != null && !filterMap.isEmpty()) {
			result = new StatementResult(factory.getRepository().get(filterMap));
		} else {
			System.out.println("before result assignment statement service");
			result = new StatementResult(factory.getRepository().get());
			System.out.println("before return statement in statement service");
		}
		// result=null;
		return result;
	}

	/**
	 * Post a statement to the database
	 * 
	 * @param newStatement
	 *            a statement object
	 * @return a collection of statement ids
	 */
	public List<String> postStatement(Statement newStatement) {

		if (log.isDebugEnabled()) {
			log.debug(String.format("New Statement: %s", newStatement));
		}

		if (StringUtils.isBlank(newStatement.getId())) {
			newStatement.setId(UUID.randomUUID().toString());
		} else {
			// check for conflict
			Statement statement = factory.getRepository().get(newStatement);
			if (statement != null) {
				statement.setStored(null);
				newStatement.setStored(null);

				/*
				 * set the actor id of new statement to fetched statement's
				 * actor id as the actor id of the new statement will be null-
				 * vamsi
				 */
				newStatement.getActor().setId(statement.getActor().getId());
				
				
				String newStatementJSON = newStatement.toJSON();
				System.out.println("New statement is " + newStatementJSON);

				String statementJSON = statement.toJSON();
				System.out.println("fetched statement is " + statementJSON);
				if (newStatementJSON.equals(statementJSON)) {
					throw new StatementStateConflictException(String.format(
							"Matching statement for id: %s already exists",
							newStatement.getId()));
				}

				
			}
		}
		newStatement.setStored(TimestampUtils
				.getISO8601StringForDate(new Date()));
		

		// Check for existing mbox or account for actors-vamsi
		List<XApiActor> actors = new ArrayList<XApiActor>();
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Criteria criteria = session.createCriteria(XApiActor.class);
		actors = (List<XApiActor>) criteria.list();

		Iterator<XApiActor> iter = actors.iterator();

		while (iter.hasNext()) {
			XApiActor existingActor = iter.next();
			String mbox;
			String homepage;
			String name;
			String mbox_new;
			String homepage_new;
			String name_new;
			
			
			try
			{
			mbox = (String) existingActor.getMbox();
			}
			catch(NullPointerException e)
			{
				System.out.println("mbox was null");
				mbox=null;
			}
			
			try
			{
			homepage=(String) existingActor.getAccount().getHomePage();
			}
			catch( NullPointerException e)
			{
				System.out.println("homepage was null");
				homepage=null;
			}
			try
			{
			 name=(String) existingActor.getAccount().getName();
			}
			catch(NullPointerException e)
			{
				System.out.println("name was null");
				name=null;
			}
			
			try
			{
			mbox_new = (String) newStatement.getActor().getMbox();
			}
			catch(NullPointerException e)
			{
				System.out.println("mbox new was null");
				mbox_new=null;
			}
			// TODO here I can simply place a check for account, homepage.
			try
			{
			homepage_new=(String) newStatement.getActor().getAccount().getHomePage();
			}
			catch( NullPointerException e)
			{
				System.out.println("homepage new was null");
				homepage_new=null;
			}
			try
			{
			 name_new=(String) newStatement.getActor().getAccount().getName();
			}
			catch(NullPointerException e)
			{
				System.out.println("name new was null");
				name_new=null;
			}

			
			
			if(mbox_new!=null && mbox!=null)
			{
			if (mbox_new.equalsIgnoreCase(mbox)) {
				newStatement.getActor().setId(existingActor.getId());
				System.out.println("An existing user just got treated well");
				break;

			}
			}

			if(homepage_new!=null && name_new!=null	&& homepage!=null && name != null)
			{
			if (name_new.equalsIgnoreCase(name) &&
					homepage_new.equalsIgnoreCase(homepage)) {
				
				newStatement.getActor().setId(existingActor.getId());
				System.out.println("An existing user just got treated well");
				break;

			}
			}
			

		}

		session.close();

		// check done

		Statement savedStatement = factory.getRepository().post(newStatement);

		if (log.isDebugEnabled()) {
			log.debug(String.format("Saved Statement: %s", savedStatement));
		}

		List<String> statementIds = new ArrayList<String>();
		statementIds.add(savedStatement.getId());

		return statementIds;
	}

	public List<Statement> getByContext(String context) {
		return factory.getRepository().getByContext(context);
	}

	public List<Statement> getByUser(String user) {
		return factory.getRepository().getByUser(user);
	}

	public List<Statement> getByContextAndUser(String context, String user) {
		return factory.getRepository().getByContextAndUser(context, user);
	}

}
