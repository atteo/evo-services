package org.atteo.moonshine.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OrientDb} service.
 */
@MoonshineConfiguration(oneRequestPerClass = true, fromString = ""
        + "<config>"
        + "     <orientdb>"
        + "     </orientdb>"
        + "</config>"
)
public class OrientDbTest extends MoonshineTest {
    @Inject
    ODatabaseDocumentTx db;

    private final static String PAPER = "Paper";

    @Test
    public void shouldInjectDb() {
        assertThat(db).isNotNull();
    }

    @Before
    public void before() {
        if (!db.exists()) {
            db.create();
        }
    }

    @Test
    public void shouldInsertAndSelect() {
        OClass paper = db.getMetadata().getSchema().createClass(PAPER);
        assertThat(paper).isNotNull();
        ODocument fooPaper = db.newInstance(PAPER);
        ODocument barPaper = db.newInstance(PAPER);
        db.begin();
        fooPaper.fromJSON(getPaper("foo", "foo paper"));
        barPaper.fromJSON(getPaper("bar", "bar paper"));
        fooPaper.save();
        barPaper.save();
        db.commit();
        List<ODocument> documents = db.query(new OSQLSynchQuery<ODocument>(String.format("select * from %s", PAPER)));
        assertThat(documents.size()).isEqualTo(2);
        assertThat(documents).contains(fooPaper, barPaper);
    }

    private String getPaper(String name, String desc) {
        return String.format("{\"name\": \"%s\", \"description\": \"%s\"}", name, desc);
    }
}
