package cn.fzu.edu.furever_home.post.service;

import cn.fzu.edu.furever_home.post.dto.PostDTO;
import cn.fzu.edu.furever_home.post.request.CreatePostRequest;
import cn.fzu.edu.furever_home.post.request.UpdatePostRequest;

import java.util.List;

public interface PostService {
    List<PostDTO> listAll();
    PostDTO getById(Integer id);
    Integer create(Integer userId, CreatePostRequest req);
    void update(Integer userId, Integer id, UpdatePostRequest req);
    void delete(Integer userId, Integer id);
}