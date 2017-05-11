package de.symeda.sormas.app.backend.user;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

import de.symeda.sormas.app.backend.common.AbstractAdoDao;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.facility.Facility;
import de.symeda.sormas.app.backend.person.Person;

/**
 * Created by Martin Wahnschaffe on 22.07.2016.
 */
public class UserDao extends AbstractAdoDao<User> {

    public UserDao(Dao<User,Long> innerDao) throws SQLException {
        super(innerDao);
    }

    @Override
    protected Class<User> getAdoClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTableName() {
        return User.TABLE_NAME;
    }

    @Override
    public boolean save(User user) throws DaoException {

        if (user.getAddress() != null) {
            DatabaseHelper.getLocationDao().save(user.getAddress());
        }

        return super.save(user);
    }

    @Override
    public boolean saveUnmodified(User user) throws DaoException {

        if (user.getAddress() != null) {
            DatabaseHelper.getLocationDao().saveUnmodified(user.getAddress());
        }

        return super.saveUnmodified(user);
    }

    public User getByUsername(String username) {
        try {
            QueryBuilder builder = queryBuilder();
            Where where = builder.where();
            where.eq(User.USER_NAME, username);

            return (User) builder.queryForFirst();
        } catch (SQLException e) {
            Log.e(getTableName(), "Could not perform getByUsername", e);
            throw new RuntimeException();
        }
    }
}
