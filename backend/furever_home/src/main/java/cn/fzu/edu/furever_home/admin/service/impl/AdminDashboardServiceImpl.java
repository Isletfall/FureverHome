package cn.fzu.edu.furever_home.admin.service.impl;

import cn.fzu.edu.furever_home.admin.dto.DashboardStatisticsDTO;
import cn.fzu.edu.furever_home.admin.service.AdminDashboardService;
import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.post.entity.Post;
import cn.fzu.edu.furever_home.post.mapper.PostMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final PostMapper postMapper;
    private final AnimalMapper animalMapper;
    private final AdoptMapper adoptMapper;

    @Override
    public DashboardStatisticsDTO getStatistics() {
        long publishedPostCount = countPosts(ReviewStatus.APPROVED);
        long pendingPostCount = countPosts(ReviewStatus.PENDING);

        long longTermPetCount = countAnimals(ReviewStatus.APPROVED, AdoptionStatus.LONG_TERM);
        long shortTermPetCount = countAnimals(ReviewStatus.APPROVED, AdoptionStatus.SHORT_TERM);
        long pendingPetCount = countAnimals(ReviewStatus.PENDING, null);

        long pendingAdoptCount = countAdopts(ReviewStatus.PENDING);

        return DashboardStatisticsDTO.builder()
                .totalPostCount(publishedPostCount)
                .longTermPetCount(longTermPetCount)
                .shortTermPetCount(shortTermPetCount)
                .pendingPostCount(pendingPostCount)
                .pendingPetCount(pendingPetCount)
                .pendingAdoptCount(pendingAdoptCount)
                .build();
    }

    private long countPosts(ReviewStatus status) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Post::getReviewStatus, status);
        }
        Long count = postMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countAnimals(ReviewStatus reviewStatus, AdoptionStatus adoptionStatus) {
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<>();
        if (reviewStatus != null) {
            wrapper.eq(Animal::getReviewStatus, reviewStatus);
        }
        if (adoptionStatus != null) {
            wrapper.eq(Animal::getAdoptionStatus, adoptionStatus);
        }
        Long count = animalMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countAdopts(ReviewStatus status) {
        LambdaQueryWrapper<Adopt> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Adopt::getReviewStatus, status);
        }
        Long count = adoptMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }
}

