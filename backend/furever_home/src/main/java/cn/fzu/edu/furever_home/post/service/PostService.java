package cn.fzu.edu.furever_home.post.service;

import cn.fzu.edu.furever_home.post.dto.PostDTO;
import cn.fzu.edu.furever_home.post.request.CreatePostRequest;
import cn.fzu.edu.furever_home.post.request.UpdatePostRequest;

import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.post.dto.PostPublicDTO;

public interface PostService {
    PageResult<PostDTO> pageAll(int page, int pageSize);
    PostDTO getById(Integer id);
    Integer create(Integer userId, CreatePostRequest req);
    void update(Integer userId, Integer id, UpdatePostRequest req);
    void delete(Integer userId, Integer id);
    PageResult<PostDTO> pageMine(Integer userId, int page, int pageSize);
    PageResult<PostPublicDTO> pageByUser(Integer userId, int page, int pageSize);
    cn.fzu.edu.furever_home.common.result.PageResult<cn.fzu.edu.furever_home.post.dto.PostCommentDTO> listComments(Integer postId, int page, int pageSize, cn.fzu.edu.furever_home.post.enums.CommentSortBy sortBy, cn.fzu.edu.furever_home.post.enums.SortOrder order);
    Integer submitComment(Integer userId, Integer postId, cn.fzu.edu.furever_home.post.request.SubmitCommentRequest req);
    PageResult<PostPublicDTO> search(String keyword, int page, int pageSize);
    boolean toggleLike(Integer userId, Integer postId);
    Integer getLikeCount(Integer postId);
    boolean toggleCommentLike(Integer userId, Integer commentId);
    Integer getCommentLikeCount(Integer commentId);
}