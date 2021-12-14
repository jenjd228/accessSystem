package ru.sfedu.services;

import ru.sfedu.api.DataProviderCsv;
import ru.sfedu.api.IDataProvider;
import ru.sfedu.model.MoveType;

import java.util.Calendar;

public class AccessService implements IAccessService {

    private final IDataProvider dataProvider;

    public AccessService(IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean checkPermission(Integer subjectId, Integer barrierId) {
        return dataProvider.isSubjectHasAccess(subjectId, barrierId);
    }

    @Override
    public void openOrCloseBarrier(Integer barrierId, boolean flag) {
        dataProvider.openOrCloseBarrier(barrierId, flag);
    }

    @Override
    public void motionRegistration(Integer subjectId, Integer barrierId, MoveType moveType) {
        dataProvider.saveMotion(subjectId, barrierId, moveType);
    }

    @Override
    public void grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        dataProvider.grantAccess(subjectId, barrierId, calendar.getTimeInMillis() / 1000);
    }
}
