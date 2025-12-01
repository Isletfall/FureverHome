package cn.fzu.edu.furever_home.rating.service;

import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.rating.dto.ReceivedRatingItemDTO;
import cn.fzu.edu.furever_home.rating.dto.MyRatingDTO;

public interface RatingService {
    PageResult<ReceivedRatingItemDTO> pageReceived(Integer userId, int page, int pageSize);
    MyRatingDTO getMyRatingFor(Integer userId, Integer targetUserId);
    void addMyRating(Integer userId, Integer targetUserId, Integer score, String content);
    void updateMyRating(Integer userId, Integer targetUserId, Integer ratingId, Integer score, String content);
}