package cn.fzu.edu.furever_home.adopt.service;

import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;

public interface AdoptService {
    Integer submit(Integer userId, SubmitAdoptRequest req);
    AdoptDTO getById(Integer id);
    void review(Integer id, ApplicationStatus status);
}