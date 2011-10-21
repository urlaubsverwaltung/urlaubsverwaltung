package org.synyx.urlaubsverwaltung.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 * implementation of the persondata-access-service. for now just passing
 * functions, but this can change(maybe)
 * 
 * @author johannes
 * 
 */
@Transactional
public class PersonServiceImpl implements PersonService {

	private PersonDAO personDAO;

	@Autowired
	public PersonServiceImpl(PersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	@Override
	public void save(Person person) {
		personDAO.save(person);

	}

	@Override
	public void delete(Person person) {
		personDAO.delete(person);
	}

	@Override
	public Person getPersonByID(Integer id) {
		return personDAO.findOne(id);
	}

	@Override
	public List<Person> getAllPersons() {
		return personDAO.findAll();
	}

}
