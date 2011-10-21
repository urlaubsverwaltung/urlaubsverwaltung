package org.synyx.urlaubsverwaltung.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;

/**
 * implementation of the requestdata-access-service.
 * 
 * @author johannes
 * 
 */
@Transactional
public class AntragServiceImpl implements AntragService {

	private AntragDAO antragDAO;

	@Autowired
	public AntragServiceImpl(AntragDAO antragDAO) {
		this.antragDAO = antragDAO;
	}

	@Override
	public void save(Antrag antrag) {
		antragDAO.save(antrag);
	}

	@Override
	public void approve(Antrag antrag) {
		antrag.setState(State.GENEHMIGT);
		antragDAO.save(antrag);
	}

	@Override
	public void decline(Antrag antrag) {
		antrag.setState(State.ABGELEHNT);
		antragDAO.save(antrag);
	}

	@Override
	public Antrag getRequestById(Integer id) {
		return antragDAO.findOne(id);
	}

	@Override
	public List<Antrag> getAllRequestsForPerson(Person person) {
		// TODO implement
		return null;
	}

	@Override
	public List<Antrag> getAllRequests() {
		return antragDAO.findAll();
	}

	@Override
	public List<Antrag> getAllRequestsByState(State state) {
		// TODO implement
		return null;
	}

}
