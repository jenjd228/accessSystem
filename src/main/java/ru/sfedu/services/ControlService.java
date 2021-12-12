package ru.sfedu.services;

import ru.sfedu.model.MoveType;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;
import ru.sfedu.utils.Constants;

import java.util.AbstractMap;
import java.util.TreeMap;

public class ControlService implements IControlService {

    private final RegistrationService registrationServiceCsv;
    private final AccessService accessService;

    public ControlService() {
        this.registrationServiceCsv = new RegistrationService();
        this.accessService = new AccessService();
    }

    @Override
    public Result<AbstractMap.SimpleEntry<Subject, TreeMap<String, String>>> objectRegistration(Subject subject) {
        Result<AbstractMap.SimpleEntry<Subject, TreeMap<String, String>>> result = registrationServiceCsv.objectRegistration(subject);

        if (result.getCode() == Constants.CODE_ACCESS) {
            if (result.getResult().getValue().size() == 0) {
                return new Result<>(null, Constants.CODE_ACCESS, result.getResult());
            }
        }
        return new Result<>(null, Constants.CODE_INVALID_DATA, result.getResult());
    }

    @Override
    public void gateAction(Integer subId, Integer barrierId, MoveType moveType) {
        accessService.motionRegistration(subId, barrierId, moveType);
        boolean isSubjectHasAccess = accessService.checkPermission(subId, barrierId);
        if (isSubjectHasAccess) {
            accessService.openOrCloseBarrier(barrierId, true);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            accessService.openOrCloseBarrier(barrierId, false);
        }
    }

    @Override
    public void barrierRegistration(Integer barrierFloor) {
        registrationServiceCsv.barrierRegistration(barrierFloor);
    }

    @Override
    public void grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours) {
        accessService.grantAccess(subjectId, barrierId, year, month, day, hours);
    }
}
