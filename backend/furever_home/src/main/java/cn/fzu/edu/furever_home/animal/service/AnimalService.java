package cn.fzu.edu.furever_home.animal.service;

import cn.fzu.edu.furever_home.animal.dto.AnimalDTO;
import cn.fzu.edu.furever_home.animal.request.CreateAnimalRequest;
import cn.fzu.edu.furever_home.animal.request.UpdateAnimalRequest;

import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;

public interface AnimalService {
    PageResult<AnimalDTO> pageAll(int page, int pageSize);
    AnimalDTO getById(Integer id, Integer currentUserId);
    Integer create(Integer userId, CreateAnimalRequest req);
    void update(Integer userId, Integer id, UpdateAnimalRequest req);
    void delete(Integer userId, Integer id);
    PageResult<AnimalDTO> pageMineByAdoption(Integer userId, AdoptionStatus adoptionStatus, int page, int pageSize);
    PageResult<AnimalPublicDTO> pageUserByAdoption(Integer userId, AdoptionStatus adoptionStatus, int page, int pageSize);
}