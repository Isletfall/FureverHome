package cn.fzu.edu.furever_home.chat.mapper;

import cn.fzu.edu.furever_home.chat.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    @Select("select * from message where chat_id=#{chatId} order by message_id desc limit 1")
    Message findLastByChat(@Param("chatId") Integer chatId);

    @Select("select count(1) from message where chat_id=#{chatId} and sender_id<>#{userId} and message_id>#{lastReadId}")
    Long countUnread(@Param("chatId") Integer chatId, @Param("userId") Integer userId, @Param("lastReadId") Integer lastReadId);
}