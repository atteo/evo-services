package org.atteo.evo.database;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.atteo.evo.jta.Transaction;
import org.atteo.evo.jta.Transactional;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public abstract class DatabaseTest {
}
