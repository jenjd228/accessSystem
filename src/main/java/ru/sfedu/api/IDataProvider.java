package ru.sfedu.api;

import ru.sfedu.model.*;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Kotsaba Eugeny
 */
public interface IDataProvider {
    /**
     * Method for registering a user in the system.
     * If a subject is valid returns a Result with a 200 code and an object which was saved.
     * If a subject is not valid returns a Result with a 422 code and a pair that consists of an object and errors.
     *
     * @param subject - the object to be saved.
     * @return an object that contains the result of the save.
     **/
    Result<Object> subjectRegistration(Subject subject);

    /**
     * Method for registering a barrier in the system
     *
     * @param barrierFloor - the floor of barrier.
     * @return a value indicating whether a barrier was created
     **/
    boolean barrierRegistration(Integer barrierFloor);

    /**
     * Method of granting access to a subject if a subject and a barrier exist.
     * If the barrier and the subject are exist it returns result with 200 code.
     * If the barrier or the subject are not exist it returns 422 code and
     * tree map of errors where key is 'barrier' or 'subject' and value is an error description.
     *
     * @param subjectId -  the id of a subject.
     * @param barrierId -  the id of a barrier.
     * @param year      - the year until which access will be provided.
     * @param month     - the month until which access will be provided.
     * @param day       - the day until which access will be provided.
     * @param hours     - hours until which access will be provided.
     * @return an object that contains the result.
     **/
    Result<Object> grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours);

    /**
     * Method that open a barrier if the subject has access and the barrier is exists.
     * All actions are recorded in the table of movements.
     *
     * @param subjectId -  the id of a subject.
     * @param barrierId -  the id of a barrier.
     * @param moveType  - type of movement (IN,OUT).
     * @return boolean - a value indicating whether an action was successful.
     **/
    boolean gateAction(Integer subjectId, Integer barrierId, MoveType moveType);

    /**
     * Method that returns all users in the system.
     *
     * @return a list of subjects in the system.
     **/
    List<Subject> getAllUsers();

    /**
     * Method that returns all AccessBarriers by subject id.
     *
     * @param subjectId - the id of a subject.
     * @return a list of accessBarriers.
     **/
    List<AccessBarrier> getAccessBarriersBySubjectId(Integer subjectId);

    /**
     * Method that returns all barriers in the system.
     *
     * @return a list of barriers in the system.
     **/
    List<Barrier> getAllBarriers();

    /**
     * Method that removes the user by id.
     * It removes and all the barriers accesses of subject.
     * If a subject is exists returns a Result with a 200 code and an object which was deleted.
     * If a subject is not exists returns a Result with a 404 code.
     *
     * @param subjectId - the id of a subject.
     * @return an object that contains the result of delete.
     **/
    Result<Subject> deleteSubjectById(Integer subjectId);

    /**
     * Method that removes all accesses by subject and barrier id.
     *
     * @param subjectId - the id of a subject.
     * @param barrierId - the id of a barrier.
     * @return the last deleted AccessBarrier.
     **/
    Result<AccessBarrier> deleteAccessBarrierBySubjectAndBarrierId(Integer subjectId, Integer barrierId);

    /**
     * Method that returns the subject history by subjectId.
     *
     * @param subjectId - the id of a subject.
     * @return the tree map where the key is a history, a value is a list of motions.
     **/
    Result<TreeMap<History, List<Motion>>> getSubjectHistoryBySubjectId(Integer subjectId);
}
