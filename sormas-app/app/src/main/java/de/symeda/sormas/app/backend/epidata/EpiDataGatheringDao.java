package de.symeda.sormas.app.backend.epidata;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.symeda.sormas.app.backend.common.AbstractAdoDao;

/**
 * Created by Mate Strysewske on 08.03.2017.
 */

public class EpiDataGatheringDao extends AbstractAdoDao<EpiDataGathering> {

    public EpiDataGatheringDao(Dao<EpiDataGathering,Long> innerDao) throws SQLException {
        super(innerDao);
    }

    public List<EpiDataGathering> getByEpiData(EpiData epiData) throws SQLException {
        QueryBuilder qb = queryBuilder();
        qb.where().eq(EpiDataGathering.EPI_DATA + "_id", epiData);
        qb.orderBy(EpiDataGathering.CHANGE_DATE, false);
        return qb.query();
    }

    public void deleteOrphansOfEpiData(EpiData epiData) throws SQLException {
        DeleteBuilder deleteBuilder = deleteBuilder();
        Where where = deleteBuilder.where().eq(EpiDataTravel.EPI_DATA + "_id", epiData);
        if (epiData.getTravels() != null) {
            Set<Long> idsToKeep = new HashSet<>();
            for (EpiDataTravel travel : epiData.getTravels()) {
                if (travel.getId() != null) {
                    idsToKeep.add(travel.getId());
                }
            }
            where.and().notIn(EpiDataTravel.ID, idsToKeep);
        }
        deleteBuilder.delete();
    }

    @Override
    public String getTableName() {
        return EpiDataGathering.TABLE_NAME;
    }
}