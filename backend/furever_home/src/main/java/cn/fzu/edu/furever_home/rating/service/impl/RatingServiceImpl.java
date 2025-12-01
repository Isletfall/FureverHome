package cn.fzu.edu.furever_home.rating.service.impl;

import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.mapper.AdoptMapper;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.rating.dto.MyRatingDTO;
import cn.fzu.edu.furever_home.rating.dto.ReceivedRatingItemDTO;
import cn.fzu.edu.furever_home.rating.entity.Rating;
import cn.fzu.edu.furever_home.rating.mapper.RatingMapper;
import cn.fzu.edu.furever_home.rating.service.RatingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {
    private final RatingMapper ratingMapper;
    private final AdoptMapper adoptMapper;
    private final AnimalMapper animalMapper;
    private final UserMapper userMapper;

    public RatingServiceImpl(RatingMapper ratingMapper, AdoptMapper adoptMapper, AnimalMapper animalMapper, UserMapper userMapper) {
        this.ratingMapper = ratingMapper;
        this.adoptMapper = adoptMapper;
        this.animalMapper = animalMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<ReceivedRatingItemDTO> pageReceived(Integer userId, int page, int pageSize) {
        Page<Rating> mpPage = new Page<>(page, pageSize);
        Page<Rating> ratings = ratingMapper.selectPage(mpPage, new LambdaQueryWrapper<Rating>()
                .eq(Rating::getTargetUserId, userId)
                .orderByDesc(Rating::getCreateTime));
        List<ReceivedRatingItemDTO> items = ratings.getRecords().stream().map(r -> {
            ReceivedRatingItemDTO d = new ReceivedRatingItemDTO();
            d.setRatingId(r.getRatingId());
            Integer otherId = r.getUserId();
            d.setOtherUserId(otherId);
            User u = otherId == null ? null : userMapper.selectById(otherId);
            d.setOtherUserAvatar(u == null ? null : u.getAvatarUrl());
            d.setOtherUserName(u == null ? null : u.getUserName());
            d.setScore(r.getScore());
            d.setContent(r.getContent());
            d.setCreateTime(r.getCreateTime());
            return d;
        }).toList();
        return new PageResult<>(ratings.getCurrent(), ratings.getSize(), ratings.getTotal(), items);
    }

    @Override
    public MyRatingDTO getMyRatingFor(Integer userId, Integer targetUserId) {
        Rating r = ratingMapper.selectOne(new LambdaQueryWrapper<Rating>().eq(Rating::getTargetUserId, targetUserId).eq(Rating::getUserId, userId));
        if (r == null) return null;
        MyRatingDTO d = new MyRatingDTO();
        d.setRatingId(r.getRatingId());
        d.setScore(r.getScore());
        d.setContent(r.getContent());
        return d;
    }

    @Override
    public void addMyRating(Integer userId, Integer targetUserId, Integer score, String content) {
        if (score == null || score < 1 || score > 5) throw new IllegalArgumentException("评分需在1-5之间");
        Rating exists = ratingMapper.selectOne(new LambdaQueryWrapper<Rating>().eq(Rating::getUserId, userId).eq(Rating::getTargetUserId, targetUserId));
        if (exists != null) {
            throw new IllegalStateException("您对该用户已存在评价");
        }
        Rating r = new Rating();
        r.setUserId(userId);
        r.setTargetUserId(targetUserId);
        r.setAdoptId(null);
        r.setScore(score);
        r.setContent(content);
        r.setCreateTime(java.time.LocalDateTime.now());
        ratingMapper.insert(r);
    }

    @Override
    public void updateMyRating(Integer userId, Integer targetUserId, Integer ratingId, Integer score, String content) {
        Rating r = ratingMapper.selectById(ratingId);
        if (r == null || !userId.equals(r.getUserId())) return;
        r.setScore(score);
        r.setContent(content);
        ratingMapper.updateById(r);
    }

    // 允许所有人评价，无需领养关系
}