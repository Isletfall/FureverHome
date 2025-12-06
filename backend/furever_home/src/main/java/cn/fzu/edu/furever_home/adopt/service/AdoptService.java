package cn.fzu.edu.furever_home.adopt.service;

import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.adopt.request.UpdateAdoptRequest;
import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;

import java.util.List;

public interface AdoptService {
    Integer submit(Integer userId, SubmitAdoptRequest req);

    AdoptDTO getById(Integer id);

    void review(Integer reviewerUserId, Integer id, ApplicationStatus status);

    void cancel(Integer userId, Integer id);

    List<Adopt> listByAnimalOwner(Integer ownerUserId);

    List<Adopt> listByApplicant(Integer applicantUserId);

    java.util.List<cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO> listTodoItems(Integer ownerUserId);

    java.util.List<cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO> listMyApplicationItems(Integer applicantUserId);

    void updateMyApplication(Integer userId, Integer id, UpdateAdoptRequest req);
}
