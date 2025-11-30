package cn.fzu.edu.furever_home.animal.service.impl;

import cn.fzu.edu.furever_home.animal.dto.AnimalDTO;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import cn.fzu.edu.furever_home.animal.request.CreateAnimalRequest;
import cn.fzu.edu.furever_home.animal.request.UpdateAnimalRequest;
import cn.fzu.edu.furever_home.animal.service.AnimalService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimalServiceImpl implements AnimalService {
    private final AnimalMapper animalMapper;
    private final ReviewService reviewService;

    @Override
    public List<AnimalDTO> listAll() {
        return animalMapper.selectList(new LambdaQueryWrapper<Animal>()
                        .eq(Animal::getReviewStatus, ReviewStatus.APPROVED))
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AnimalDTO getById(Integer id) {
        Animal a = animalMapper.selectById(id);
        if (a == null || a.getReviewStatus() != ReviewStatus.APPROVED) return null;
        return toDTO(a);
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
        a.setUpdatedAt(LocalDateTime.now());
        animalMapper.updateById(a);
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
}