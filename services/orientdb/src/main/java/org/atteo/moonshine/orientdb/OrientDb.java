package org.atteo.moonshine.orientdb;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.servlet.RequestScoped;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.TopLevelService;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orientdb")
public class OrientDb extends TopLevelService {
    @XmlElement(name = "url")
    @XmlDefaultValue("local:${dataHome}/orientdb")
    private String url;

    @XmlElement(name = "autocreate")
    @XmlDefaultValue("true")
    private Boolean autocreate;

    @XmlDefaultValue("admin")
    private String username;

    @XmlDefaultValue("admin")
    private String password;

    @XmlElement(name = "pool-timeout")
    @XmlDefaultValue("600000")
    private Integer poolTimeout;

    private PersistService persistService;

    private Provider<ODatabaseDocumentTx> provider = new Provider<ODatabaseDocumentTx>() {
        @Override
        public ODatabaseDocumentTx get() {
            return ODatabaseDocumentPool.global().acquire(url, username, password);
        }
    };

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getAutocreate() {
        return autocreate;
    }

    @Override
    public Module configure() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.setValue(poolTimeout);
                bind(ODatabaseDocumentTx.class).toProvider(provider).in(RequestScoped.class);
            }
        };
    }

    @Override
    public void start() {
        if (getAutocreate()) {
            ODatabaseDocumentTx db = new ODatabaseDocumentTx(url);
            if (!db.exists()) {
                db.create();
            }
            db.close();
        }
    }

    @Override
    public void stop() {
    }
}
