package de.symeda.sormas.app.backend.contact;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.AbstractAdoDao;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.event.Event;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.Region;

import static android.R.attr.value;

/**
 * Created by Stefan Szczesny on 29.11.2016.
 */
public class ContactDao extends AbstractAdoDao<Contact> {

    public ContactDao(Dao<Contact,Long> innerDao) throws SQLException {
        super(innerDao);
    }

    @Override
    protected Class<Contact> getAdoClass() {
        return Contact.class;
    }

    @Override
    public String getTableName() {
        return Contact.TABLE_NAME;
    }

    public List<Contact> getByCase(Case caze) {
        try {
            QueryBuilder qb = queryBuilder();
            qb.where().eq(Contact.CAZE + "_id", caze);
            qb.orderBy(Contact.LAST_CONTACT_DATE, false);
            return qb.query();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform getByCase on Contact");
            throw new RuntimeException(e);
        }
    }

    @Override
    public Contact create() {

        Contact contact = super.create();

        contact.setReportDateTime(new Date());
        contact.setReportingUser(ConfigProvider.getUser());

        return contact;
    }
}
