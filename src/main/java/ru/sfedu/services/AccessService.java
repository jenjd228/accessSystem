package ru.sfedu.services;

import ru.sfedu.api.IDataProvider;
import ru.sfedu.model.MoveType;

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
    public void grantAccess(Integer subjectId, Integer barrierId, Long date) {
        dataProvider.grantAccess(subjectId, barrierId, date);
    }
}
