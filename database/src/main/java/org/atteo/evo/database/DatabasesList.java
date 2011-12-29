package org.atteo.evo.database;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.injection.InjectMembers;
import org.atteo.evo.services.Service;
import org.atteo.evo.services.TopLevelGroup;

@XmlRootElement(name = "databases")
@XmlAccessorType(XmlAccessType.NONE)
public class DatabasesList extends TopLevelGroup {
	@XmlElementRef
	@InjectMembers
	List<DatabaseService> databases;

	@Override
	public List<? extends Service> getServices() {
		return Collections.unmodifiableList(databases);
	}
}
