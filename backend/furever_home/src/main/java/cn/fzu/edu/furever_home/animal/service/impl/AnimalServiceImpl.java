package cn.fzu.edu.furever_home.animal.service.impl;

import cn.fzu.edu.furever_home.animal.dto.AnimalDTO;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.animal.request.CreateAnimalRequest;
import cn.fzu.edu.furever_home.animal.request.UpdateAnimalRequest;
import cn.fzu.edu.furever_home.animal.service.AnimalService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import cn.fzu.edu.furever_home.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@RequiredArgsConstructor
public class AnimalServiceImpl implements AnimalService {
    private final AnimalMapper animalMapper;
    private final ReviewService reviewService;
    private final UserMapper userMapper;

    @Override
    public PageResult<AnimalDTO> pageAll(int page, int pageSize) {
        Page<Animal> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
                .eq(Animal::getReviewStatus, ReviewStatus.APPROVED)
                .orderByDesc(Animal::getCreatedAt);
        Page<Animal> result = animalMapper.selectPage(mpPage, wrapper);
        java.util.List<AnimalDTO> records = result.getRecords().stream().map(this::toDTO).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PageResult<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO> pageAllPublic(int page, int pageSize,
            String province,
            String city,
            cn.fzu.edu.furever_home.common.enums.Species species,
            cn.fzu.edu.furever_home.common.enums.Gender gender,
            Integer minAge,
            Integer maxAge,
            cn.fzu.edu.furever_home.common.enums.AdoptionStatus adoptionStatus) {
        Page<Animal> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
                .eq(Animal::getReviewStatus, ReviewStatus.APPROVED);
        if (province != null && !province.isBlank()) {
            wrapper.eq(Animal::getCurrentProvince, province);
        }
        if (city != null && !city.isBlank()) {
            wrapper.eq(Animal::getCurrentCity, city);
        }
        if (species != null) {
            wrapper.eq(Animal::getSpecies, species);
        }
        if (gender != null) {
            wrapper.eq(Animal::getGender, gender);
        }
        if (adoptionStatus != null) {
            wrapper.eq(Animal::getAdoptionStatus, adoptionStatus);
        }
        if (minAge != null) {
            wrapper.ge(Animal::getAnimalAge, minAge);
        }
        if (maxAge != null) {
            wrapper.le(Animal::getAnimalAge, maxAge);
        }
        wrapper.orderByDesc(Animal::getCreatedAt);
        Page<Animal> result = animalMapper.selectPage(mpPage, wrapper);
        java.util.List<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO> records = result.getRecords().stream()
                .map(a -> {
                    cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO d = new cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO();
                    d.setAnimalId(a.getAnimalId());
                    d.setUserId(a.getUserId());
                    d.setAnimalName(a.getAnimalName());
                    d.setPhotoUrls(a.getPhotoUrls());
                    d.setSpecies(a.getSpecies());
                    d.setBreed(a.getBreed());
                    d.setGender(a.getGender());
                    d.setAnimalAge(a.getAnimalAge());
                    d.setIsSterilized(a.getIsSterilized());
                    d.setAdoptionStatus(a.getAdoptionStatus());
                    d.setShortDescription(a.getShortDescription());
                    User owner = a.getUserId() == null ? null : userMapper.selectById(a.getUserId());
                    d.setAdopterName(owner == null ? null : owner.getUserName());
                    d.setUserAvatar(owner == null ? null : owner.getAvatarUrl());
                    Integer days = a.getCreatedAt() == null ? null
                            : (int) java.time.temporal.ChronoUnit.DAYS.between(a.getCreatedAt(),
                                    java.time.LocalDateTime.now());
                    d.setAdoptionDays(days);
                    return d;
                }).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public AnimalDTO getById(Integer id, Integer currentUserId) {
        Animal a = animalMapper.selectById(id);
        if (a == null)
            return null;
        if (a.getReviewStatus() == ReviewStatus.APPROVED)
            return toDTO(a);
        if (currentUserId != null && a.getUserId() != null && a.getUserId().equals(currentUserId))
            return toDTO(a);
        return null;
    }

    @Override
    public Integer create(Integer userId, CreateAnimalRequest req) {
        Animal a = new Animal();
        a.setUserId(userId);
        a.setAnimalName(req.getAnimalName());
        a.setPhotoUrls(req.getPhotoUrls());
        a.setSpecies(req.getSpecies());
        a.setBreed(req.getBreed());
        a.setGender(req.getGender());
        a.setAnimalAge(req.getAnimalAge());
        a.setHealthStatus(req.getHealthStatus());
        a.setIsSterilized(req.getIsSterilized());
        a.setAdoptionStatus(req.getAdoptionStatus());
        a.setShortDescription(req.getShortDescription());
        a.setContactPhone(req.getContactPhone());
        a.setCurrentLocation(req.getCurrentLocation());
        a.setContactEmail(req.getContactEmail());
        a.setCurrentProvince(req.getProvince());
        a.setCurrentCity(req.getCity());
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        animalMapper.insert(a);
        reviewService.createPending(ReviewTargetType.ANIMAL, a.getAnimalId());
        return a.getAnimalId();
    }

    @Override
    public void update(Integer userId, Integer id, UpdateAnimalRequest req) {
        Animal a = animalMapper.selectById(id);
        if (a == null)
            throw new IllegalStateException("动物不存在");
        if (!a.getUserId().equals(userId))
            throw new IllegalStateException("无权修改该动物信息");
        if (req.getAnimalName() != null)
            a.setAnimalName(req.getAnimalName());
        if (req.getPhotoUrls() != null)
            a.setPhotoUrls(req.getPhotoUrls());
        if (req.getSpecies() != null)
            a.setSpecies(req.getSpecies());
        if (req.getBreed() != null)
            a.setBreed(req.getBreed());
        if (req.getGender() != null)
            a.setGender(req.getGender());
        if (req.getAnimalAge() != null)
            a.setAnimalAge(req.getAnimalAge());
        if (req.getHealthStatus() != null)
            a.setHealthStatus(req.getHealthStatus());
        if (req.getIsSterilized() != null)
            a.setIsSterilized(req.getIsSterilized());
        if (req.getAdoptionStatus() != null)
            a.setAdoptionStatus(req.getAdoptionStatus());
        if (req.getShortDescription() != null)
            a.setShortDescription(req.getShortDescription());
        if (req.getContactPhone() != null)
            a.setContactPhone(req.getContactPhone());
        if (req.getCurrentLocation() != null)
            a.setCurrentLocation(req.getCurrentLocation());
        if (req.getContactEmail() != null)
            a.setContactEmail(req.getContactEmail());
        if (req.getProvince() != null)
            a.setCurrentProvince(req.getProvince());
        if (req.getCity() != null)
            a.setCurrentCity(req.getCity());
        a.setReviewStatus(ReviewStatus.PENDING);
        a.setUpdatedAt(LocalDateTime.now());
        animalMapper.updateById(a);
        reviewService.createPending(ReviewTargetType.ANIMAL, a.getAnimalId());
    }

    @Override
    public void delete(Integer userId, Integer id) {
        Animal a = animalMapper.selectById(id);
        if (a == null)
            return;
        if (!a.getUserId().equals(userId))
            throw new IllegalStateException("无权删除该动物信息");
        animalMapper.deleteById(id);
    }

    private AnimalDTO toDTO(Animal a) {
        if (a == null)
            return null;
        AnimalDTO d = new AnimalDTO();
        d.setAnimalId(a.getAnimalId());
        d.setUserId(a.getUserId());
        User u = a.getUserId() == null ? null : userMapper.selectById(a.getUserId());
        d.setUserName(u == null ? null : u.getUserName());
        d.setUserAvatar(u == null ? null : u.getAvatarUrl());
        d.setOwnerLocation(u == null ? null : u.getLocation());
        d.setOwnerEmail(u == null ? null : u.getEmail());
        d.setOwnerPhone(a.getContactPhone());
        d.setCurrentLocation(a.getCurrentLocation());
        d.setContactEmail(a.getContactEmail());
        d.setAnimalName(a.getAnimalName());
        d.setPhotoUrls(a.getPhotoUrls());
        d.setSpecies(a.getSpecies());
        d.setBreed(a.getBreed());
        d.setGender(a.getGender());
        d.setAnimalAge(a.getAnimalAge());
        d.setHealthStatus(a.getHealthStatus());
        d.setIsSterilized(a.getIsSterilized());
        d.setAdoptionStatus(a.getAdoptionStatus());
        d.setShortDescription(a.getShortDescription());
        d.setReviewStatus(a.getReviewStatus());
        return d;
    }

    @Override
    public PageResult<AnimalDTO> pageMineByAdoption(Integer userId, AdoptionStatus adoptionStatus, int page,
            int pageSize) {
        Page<Animal> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
                .eq(Animal::getUserId, userId)
                .eq(Animal::getAdoptionStatus, adoptionStatus)
                .orderByDesc(Animal::getCreatedAt);
        Page<Animal> result = animalMapper.selectPage(mpPage, wrapper);
        java.util.List<AnimalDTO> records = result.getRecords().stream().map(this::toDTO).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PageResult<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO> pageUserByAdoption(Integer userId,
            AdoptionStatus adoptionStatus, int page, int pageSize) {
        Page<Animal> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<Animal>()
                .eq(Animal::getUserId, userId)
                .eq(Animal::getAdoptionStatus, adoptionStatus)
                .eq(Animal::getReviewStatus, ReviewStatus.APPROVED)
                .orderByDesc(Animal::getCreatedAt);
        Page<Animal> result = animalMapper.selectPage(mpPage, wrapper);
        java.util.List<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO> records = result.getRecords().stream()
                .map(a -> {
                    cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO d = new cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO();
                    d.setAnimalId(a.getAnimalId());
                    d.setUserId(a.getUserId());
                    d.setAnimalName(a.getAnimalName());
                    d.setPhotoUrls(a.getPhotoUrls());
                    d.setSpecies(a.getSpecies());
                    d.setBreed(a.getBreed());
                    d.setGender(a.getGender());
                    d.setAnimalAge(a.getAnimalAge());
                    d.setIsSterilized(a.getIsSterilized());
                    d.setAdoptionStatus(a.getAdoptionStatus());
                    d.setShortDescription(a.getShortDescription());
                    User owner = a.getUserId() == null ? null : userMapper.selectById(a.getUserId());
                    d.setAdopterName(owner == null ? null : owner.getUserName());
                    d.setUserAvatar(owner == null ? null : owner.getAvatarUrl());
                    Integer days = a.getCreatedAt() == null ? null
                            : (int) java.time.temporal.ChronoUnit.DAYS.between(a.getCreatedAt(),
                                    java.time.LocalDateTime.now());
                    d.setAdoptionDays(days);
                    return d;
                }).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }
}
