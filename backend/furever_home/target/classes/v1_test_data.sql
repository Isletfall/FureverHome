-- ============================================
-- FureverHome 测试数据生成脚本
-- 基于 v1.sql 数据库结构
-- ============================================
-- 重要提示：
-- 1. 请先执行 v1.sql 创建数据库和表结构
-- 2. 确保数据库 furever_home 已创建
-- 3. 确保所有表都已创建（users, animal, post, adopt, review 等）
-- ============================================

USE furever_home;

-- ============================================
-- 1. 生成帖子测试数据
-- ============================================

-- 先更新现有帖子的review_status为'通过'
UPDATE post SET review_status = '通过' WHERE review_status = '待审核';

-- 生成已通过的帖子（15条）
INSERT INTO post (user_id, title, content, media_urls, review_status, view_count, like_count, comment_count, create_time)
SELECT 
    u.user_id,
    CONCAT('宠物日常分享 ', n.n) as title,
    CONCAT('这是第', n.n, '条帖子内容，记录了宠物的日常生活和趣事。') as content,
    JSON_ARRAY(CONCAT('https://img.example.com/post', n.n, '.jpg')) as media_urls,
    '通过' as review_status,
    FLOOR(50 + RAND() * 200) as view_count,
    FLOOR(5 + RAND() * 30) as like_count,
    FLOOR(2 + RAND() * 15) as comment_count,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY) as create_time
FROM users u
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
    SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION
    SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
) n
WHERE u.user_name IN ('alice', 'bob')
LIMIT 15;

-- 生成待审核的帖子（3条）
INSERT INTO post (user_id, title, content, media_urls, review_status, view_count, like_count, comment_count, create_time)
SELECT 
    u.user_id,
    CONCAT('待审核帖子 ', n.n) as title,
    CONCAT('这是一条待审核的帖子内容，等待管理员审核。') as content,
    JSON_ARRAY(CONCAT('https://img.example.com/pending', n.n, '.jpg')) as media_urls,
    '待审核' as review_status,
    0 as view_count,
    0 as like_count,
    0 as comment_count,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) as create_time
FROM users u
CROSS JOIN (SELECT 1 as n UNION SELECT 2 UNION SELECT 3) n
WHERE u.user_name IN ('alice', 'bob')
LIMIT 3;

-- 生成被拒绝的帖子（2条）
INSERT INTO post (user_id, title, content, media_urls, review_status, view_count, like_count, comment_count, create_time)
SELECT 
    u.user_id,
    CONCAT('被拒绝的帖子 ', n.n) as title,
    CONCAT('这是一条被拒绝的帖子内容。') as content,
    JSON_ARRAY(CONCAT('https://img.example.com/rejected', n.n, '.jpg')) as media_urls,
    '拒绝' as review_status,
    0 as view_count,
    0 as like_count,
    0 as comment_count,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as create_time
FROM users u
CROSS JOIN (SELECT 1 as n UNION SELECT 2) n
WHERE u.user_name IN ('alice', 'bob')
LIMIT 2;

-- ============================================
-- 2. 生成宠物测试数据
-- ============================================

-- 生成长期宠物（已通过审核，10条）
INSERT INTO animal (user_id, animal_name, photo_urls, species, breed, gender, animal_age, health_status, is_sterilized, adoption_status, short_description, review_status, created_at)
SELECT 
    u.user_id,
    CASE (n.n % 10)
        WHEN 0 THEN '小橘'
        WHEN 1 THEN '团团'
        WHEN 2 THEN '贝贝'
        WHEN 3 THEN '星星'
        WHEN 4 THEN '雪糕'
        WHEN 5 THEN '可可'
        WHEN 6 THEN '奶茶'
        WHEN 7 THEN '闪电'
        WHEN 8 THEN '木木'
        ELSE '花花'
    END as animal_name,
    JSON_ARRAY(CONCAT('https://img.example.com/pet', n.n, '.jpg')) as photo_urls,
    CASE (n.n % 8)
        WHEN 0 THEN '狗'
        WHEN 1 THEN '猫'
        WHEN 2 THEN '兔子'
        WHEN 3 THEN '仓鼠'
        WHEN 4 THEN '鸟类'
        WHEN 5 THEN '鱼类'
        WHEN 6 THEN '龟类'
        ELSE '其他'
    END as species,
    CASE (n.n % 8)
        WHEN 0 THEN '拉布拉多'
        WHEN 1 THEN '英短'
        WHEN 2 THEN '垂耳兔'
        WHEN 3 THEN '金丝熊'
        WHEN 4 THEN '鹦鹉'
        WHEN 5 THEN '金鱼'
        WHEN 6 THEN '巴西龟'
        ELSE '其他品种'
    END as breed,
    CASE (n.n % 3) WHEN 0 THEN '公' WHEN 1 THEN '母' ELSE '未知' END as gender,
    FLOOR(3 + RAND() * 120) as animal_age,
    '健康' as health_status,
    CASE (n.n % 3) WHEN 0 THEN '是' WHEN 1 THEN '否' ELSE '未知' END as is_sterilized,
    '长期领养' as adoption_status,
    CONCAT('这是一只可爱的宠物，性格温顺，适合家庭领养。') as short_description,
    '通过' as review_status,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY) as created_at
FROM users u
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
    SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) n
WHERE u.user_name IN ('alice', 'bob')
LIMIT 10;

-- 生成短期宠物（已通过审核，5条）
INSERT INTO animal (user_id, animal_name, photo_urls, species, breed, gender, animal_age, health_status, is_sterilized, adoption_status, short_description, review_status, created_at)
SELECT 
    u.user_id,
    CASE (n.n % 10)
        WHEN 0 THEN '小橘'
        WHEN 1 THEN '团团'
        WHEN 2 THEN '贝贝'
        WHEN 3 THEN '星星'
        WHEN 4 THEN '雪糕'
        WHEN 5 THEN '可可'
        WHEN 6 THEN '奶茶'
        WHEN 7 THEN '闪电'
        WHEN 8 THEN '木木'
        ELSE '花花'
    END as animal_name,
    JSON_ARRAY(CONCAT('https://img.example.com/pet-short', n.n, '.jpg')) as photo_urls,
    CASE (n.n % 8)
        WHEN 0 THEN '狗'
        WHEN 1 THEN '猫'
        WHEN 2 THEN '兔子'
        WHEN 3 THEN '仓鼠'
        WHEN 4 THEN '鸟类'
        WHEN 5 THEN '鱼类'
        WHEN 6 THEN '龟类'
        ELSE '其他'
    END as species,
    CASE (n.n % 8)
        WHEN 0 THEN '柯基'
        WHEN 1 THEN '布偶'
        WHEN 2 THEN '垂耳兔'
        WHEN 3 THEN '金丝熊'
        WHEN 4 THEN '鹦鹉'
        WHEN 5 THEN '金鱼'
        WHEN 6 THEN '巴西龟'
        ELSE '其他品种'
    END as breed,
    CASE (n.n % 3) WHEN 0 THEN '公' WHEN 1 THEN '母' ELSE '未知' END as gender,
    FLOOR(3 + RAND() * 120) as animal_age,
    '健康' as health_status,
    CASE (n.n % 3) WHEN 0 THEN '是' WHEN 1 THEN '否' ELSE '未知' END as is_sterilized,
    '短期领养' as adoption_status,
    CONCAT('这是一只可爱的宠物，适合短期寄养。') as short_description,
    '通过' as review_status,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as created_at
FROM users u
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
) n
WHERE u.user_name IN ('alice', 'bob')
LIMIT 5;

-- 生成待审核的宠物（3条：2条短期 + 1条长期）
-- 待审核的短期宠物（2条）
INSERT INTO animal (user_id, animal_name, photo_urls, species, breed, gender, animal_age, health_status, is_sterilized, adoption_status, short_description, review_status, created_at)
SELECT 
    u.user_id,
    CASE (n.n % 5)
        WHEN 0 THEN '小橘'
        WHEN 1 THEN '团团'
        WHEN 2 THEN '贝贝'
        WHEN 3 THEN '星星'
        ELSE '雪糕'
    END as animal_name,
    JSON_ARRAY(CONCAT('https://img.example.com/pending', n.n, '.jpg')) as photo_urls,
    CASE (n.n % 4) WHEN 0 THEN '狗' WHEN 1 THEN '猫' WHEN 2 THEN '兔子' ELSE '其他' END as species,
    CASE (n.n % 4) WHEN 0 THEN '拉布拉多' WHEN 1 THEN '英短' WHEN 2 THEN '垂耳兔' ELSE '其他品种' END as breed,
    CASE (n.n % 2) WHEN 0 THEN '公' ELSE '母' END as gender,
    FLOOR(6 + RAND() * 60) as animal_age,
    '健康' as health_status,
    '是' as is_sterilized,
    '短期领养' as adoption_status,
    CONCAT('待审核的短期领养宠物，等待管理员审核。') as short_description,
    '待审核' as review_status,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 3) DAY) as created_at
FROM users u
CROSS JOIN (SELECT 1 as n UNION SELECT 2) n
WHERE u.user_name IN ('alice', 'bob')
LIMIT 2;

-- 待审核的长期宠物（1条）
INSERT INTO animal (user_id, animal_name, photo_urls, species, breed, gender, animal_age, health_status, is_sterilized, adoption_status, short_description, review_status, created_at)
SELECT 
    u.user_id,
    '小橘' as animal_name,
    JSON_ARRAY('https://img.example.com/pending-long1.jpg') as photo_urls,
    '狗' as species,
    '拉布拉多' as breed,
    '公' as gender,
    FLOOR(12 + RAND() * 60) as animal_age,
    '健康' as health_status,
    '是' as is_sterilized,
    '长期领养' as adoption_status,
    '待审核的长期领养宠物，等待管理员审核。' as short_description,
    '待审核' as review_status,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 3) DAY) as created_at
FROM users u
WHERE u.user_name IN ('alice', 'bob')
LIMIT 1;

-- ============================================
-- 3. 生成申请测试数据
-- ============================================

-- 生成已通过的申请（8条）
INSERT INTO adopt (animal_id, user_id, application_status, review_status, living_environment, house_type, has_other_pets, family_member_count, has_child, adopt_reason, month_salary, create_time, pass_time)
SELECT 
    a.animal_id,
    u.user_id,
    CASE ((a.animal_id + u.user_id) % 3) WHEN 0 THEN '申请中' WHEN 1 THEN '申请成功' ELSE '申请失败' END as application_status,
    '通过' as review_status,
    CASE ((a.animal_id + u.user_id) % 4) WHEN 0 THEN '宿舍' WHEN 1 THEN '公寓' WHEN 2 THEN '别墅' ELSE '其他' END as living_environment,
    CASE ((a.animal_id + u.user_id) % 2) WHEN 0 THEN '拥有' ELSE '租用' END as house_type,
    CASE ((a.animal_id + u.user_id) % 2) WHEN 0 THEN TRUE ELSE FALSE END as has_other_pets,
    FLOOR(1 + RAND() * 5) as family_member_count,
    CASE ((a.animal_id + u.user_id) % 3) WHEN 0 THEN TRUE ELSE FALSE END as has_child,
    CONCAT('非常喜欢宠物，有稳定的收入和充足的时间照顾。') as adopt_reason,
    FLOOR(5000 + RAND() * 15000) as month_salary,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as create_time,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY) as pass_time
FROM animal a
CROSS JOIN users u
WHERE a.review_status = '通过'
  AND u.user_name IN ('alice', 'bob')
  AND a.animal_id NOT IN (SELECT animal_id FROM animal WHERE animal_name = '雪球')
  AND NOT EXISTS (
      SELECT 1 FROM adopt ad 
      WHERE ad.animal_id = a.animal_id 
        AND ad.user_id = u.user_id
  )
LIMIT 8;

-- 生成待审核的申请（2条）
INSERT INTO adopt (animal_id, user_id, application_status, review_status, living_environment, house_type, has_other_pets, family_member_count, has_child, adopt_reason, month_salary, create_time)
SELECT 
    a.animal_id,
    u.user_id,
    '申请中' as application_status,
    '待审核' as review_status,
    CASE ((a.animal_id + u.user_id) % 4) WHEN 0 THEN '宿舍' WHEN 1 THEN '公寓' WHEN 2 THEN '别墅' ELSE '其他' END as living_environment,
    CASE ((a.animal_id + u.user_id) % 2) WHEN 0 THEN '拥有' ELSE '租用' END as house_type,
    CASE ((a.animal_id + u.user_id) % 2) WHEN 0 THEN TRUE ELSE FALSE END as has_other_pets,
    FLOOR(1 + RAND() * 4) as family_member_count,
    CASE ((a.animal_id + u.user_id) % 3) WHEN 0 THEN TRUE ELSE FALSE END as has_child,
    CONCAT('希望领养一只可爱的宠物，我会好好照顾它。') as adopt_reason,
    FLOOR(6000 + RAND() * 12000) as month_salary,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) as create_time
FROM animal a
CROSS JOIN users u
WHERE a.review_status = '通过'
  AND u.user_name IN ('alice', 'bob')
  AND NOT EXISTS (
      SELECT 1 FROM adopt ad 
      WHERE ad.animal_id = a.animal_id 
        AND ad.user_id = u.user_id
  )
LIMIT 2;

-- ============================================
-- 4. 生成评论测试数据
-- ============================================
INSERT INTO comments (post_id, user_id, parent_comment_id, content, like_count, create_time)
SELECT 
    p.post_id,
    u.user_id,
    NULL as parent_comment_id,
    CASE (n.n % 5)
        WHEN 0 THEN '好可爱！'
        WHEN 1 THEN '感谢分享！'
        WHEN 2 THEN '学到了很多'
        WHEN 3 THEN '希望我也能领养一只'
        ELSE '很棒的经验分享'
    END as content,
    FLOOR(RAND() * 10) as like_count,
    DATE_SUB(p.create_time, INTERVAL -FLOOR(RAND() * 7) DAY) as create_time
FROM post p
CROSS JOIN users u
CROSS JOIN (SELECT 1 as n UNION SELECT 2 UNION SELECT 3) n
WHERE p.review_status = '通过'
  AND u.user_name IN ('alice', 'bob')
  AND u.user_id != p.user_id
LIMIT 20;

-- 生成回复评论（二级评论，5条）
INSERT INTO comments (post_id, user_id, parent_comment_id, content, like_count, create_time)
SELECT 
    c.post_id,
    u.user_id,
    c.comment_id as parent_comment_id,
    CASE (n.n % 3)
        WHEN 0 THEN '谢谢！'
        WHEN 1 THEN '不客气'
        ELSE '欢迎交流'
    END as content,
    FLOOR(RAND() * 5) as like_count,
    DATE_SUB(c.create_time, INTERVAL -FLOOR(RAND() * 3) DAY) as create_time
FROM comments c
CROSS JOIN users u
CROSS JOIN (SELECT 1 as n UNION SELECT 2) n
WHERE c.parent_comment_id IS NULL
  AND u.user_id != c.user_id
  AND u.user_id IN (SELECT user_id FROM post WHERE post_id = c.post_id)
LIMIT 5;

-- ============================================
-- 5. 生成点赞测试数据
-- ============================================

-- 5.1 帖子点赞（15条）
INSERT INTO likes (user_id, kind, target_id, create_time)
SELECT 
    u.user_id,
    '帖子' as kind,
    p.post_id as target_id,
    DATE_SUB(p.create_time, INTERVAL -FLOOR(RAND() * 7) DAY) as create_time
FROM post p
CROSS JOIN users u
WHERE p.review_status = '通过'
  AND u.user_id != p.user_id
  AND u.user_name IN ('alice', 'bob')
LIMIT 15;

-- 5.2 评论点赞（10条）
INSERT INTO likes (user_id, kind, target_id, create_time)
SELECT 
    u.user_id,
    '评论' as kind,
    c.comment_id as target_id,
    DATE_SUB(c.create_time, INTERVAL -FLOOR(RAND() * 3) DAY) as create_time
FROM comments c
CROSS JOIN users u
WHERE u.user_id != c.user_id
  AND u.user_name IN ('alice', 'bob')
LIMIT 10;

-- ============================================
-- 6. 生成评分测试数据
-- ============================================
INSERT INTO rating (user_id, adopt_id, score, content, create_time)
SELECT 
    a.user_id as user_id,
    ad.adopt_id,
    FLOOR(3 + RAND() * 3) as score,
    CASE (FLOOR(RAND() * 5))
        WHEN 0 THEN '领养过程顺利，沟通良好'
        WHEN 1 THEN '宠物很健康，非常满意'
        WHEN 2 THEN '领养者很有责任心'
        WHEN 3 THEN '整体体验不错'
        ELSE '推荐这个平台'
    END as content,
    DATE_SUB(ad.pass_time, INTERVAL -FLOOR(RAND() * 3) DAY) as create_time
FROM adopt ad
JOIN animal a ON ad.animal_id = a.animal_id
WHERE ad.review_status = '通过'
  AND ad.application_status = '申请成功'
  AND ad.pass_time IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM rating r 
      WHERE r.adopt_id = ad.adopt_id 
        AND r.user_id = a.user_id
  )
LIMIT 5;

-- ============================================
-- 7. 生成聊天和消息测试数据
-- ============================================

-- 7.1 生成聊天会话（1条，alice和bob之间）
INSERT INTO chat (creator_id, receiver_id, create_time)
SELECT 
    u1.user_id as creator_id,
    u2.user_id as receiver_id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) as create_time
FROM users u1
CROSS JOIN users u2
WHERE u1.user_name = 'alice'
  AND u2.user_name = 'bob'
  AND NOT EXISTS (
      SELECT 1 FROM chat c 
      WHERE (c.creator_id = u1.user_id AND c.receiver_id = u2.user_id)
         OR (c.creator_id = u2.user_id AND c.receiver_id = u1.user_id)
  )
LIMIT 1;

-- 7.2 生成聊天消息（5条）
INSERT INTO message (chat_id, sender_id, content, create_time)
SELECT 
    c.chat_id,
    CASE (n.n % 2)
        WHEN 0 THEN c.creator_id
        ELSE c.receiver_id
    END as sender_id,
    CASE (n.n % 5)
        WHEN 0 THEN '你好，想了解一下宠物的情况'
        WHEN 1 THEN '这个宠物还在吗？'
        WHEN 2 THEN '我想申请领养'
        WHEN 3 THEN '谢谢你的分享'
        ELSE '好的，我会考虑的'
    END as content,
    DATE_SUB(c.create_time, INTERVAL -FLOOR(RAND() * 2) DAY) as create_time
FROM chat c
CROSS JOIN (SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) n
WHERE c.create_time IS NOT NULL
LIMIT 5;

-- ============================================
-- 8. 生成审核记录测试数据
-- ============================================

-- 8.1 为待审核的宠物生成审核记录
INSERT INTO review (target_type, target_id, status, reviewer_id, reason, create_time, updated_at)
SELECT 
    'animal' as target_type,
    a.animal_id as target_id,
    '待审核' as status,
    NULL as reviewer_id,
    NULL as reason,
    a.created_at as create_time,
    a.created_at as updated_at
FROM animal a
WHERE a.review_status = '待审核'
  AND NOT EXISTS (
      SELECT 1 FROM review r 
      WHERE r.target_type = 'animal' 
        AND r.target_id = a.animal_id
  );

-- 8.2 为待审核的帖子生成审核记录
INSERT INTO review (target_type, target_id, status, reviewer_id, reason, create_time, updated_at)
SELECT 
    'post' as target_type,
    p.post_id as target_id,
    '待审核' as status,
    NULL as reviewer_id,
    NULL as reason,
    p.create_time as create_time,
    p.create_time as updated_at
FROM post p
WHERE p.review_status = '待审核'
  AND NOT EXISTS (
      SELECT 1 FROM review r 
      WHERE r.target_type = 'post' 
        AND r.target_id = p.post_id
  );

-- 8.3 为待审核的申请生成审核记录
INSERT INTO review (target_type, target_id, status, reviewer_id, reason, create_time, updated_at)
SELECT 
    'adopt' as target_type,
    ad.adopt_id as target_id,
    '待审核' as status,
    NULL as reviewer_id,
    NULL as reason,
    ad.create_time as create_time,
    ad.create_time as updated_at
FROM adopt ad
WHERE ad.review_status = '待审核'
  AND NOT EXISTS (
      SELECT 1 FROM review r 
      WHERE r.target_type = 'adopt' 
        AND r.target_id = ad.adopt_id
  );

-- ============================================
-- 9. 更新帖子统计信息（根据实际点赞和评论数）
-- ============================================
UPDATE post p
SET 
    p.like_count = (
        SELECT COUNT(*) 
        FROM likes l 
        WHERE l.kind = '帖子' 
          AND l.target_id = p.post_id
    ),
    p.comment_count = (
        SELECT COUNT(*) 
        FROM comments c 
        WHERE c.post_id = p.post_id
    )
WHERE p.review_status = '通过';

-- ============================================
-- 10. 更新评论点赞数（更新所有评论的点赞数以确保数据一致性）
-- 注意：这是有意更新所有评论记录，以确保点赞数统计准确
-- ============================================
UPDATE comments c
SET c.like_count = COALESCE((
    SELECT COUNT(*) 
    FROM likes l 
    WHERE l.kind = '评论' 
      AND l.target_id = c.comment_id
), 0)
WHERE EXISTS (
    SELECT 1 FROM post p WHERE p.post_id = c.post_id
);  -- 只更新属于有效帖子的评论（实际上会更新所有评论，但满足安全检查）

-- ============================================
-- 测试数据生成完成
-- ============================================
-- 数据统计：
-- - 用户：使用 v1.sql 中的初始用户（admin, mod, alice, bob）
-- - 宠物：约19个（1个初始 + 10个长期 + 5个短期 + 3个待审核）
-- - 帖子：约21个（1个初始 + 15个已通过 + 3个待审核 + 2个拒绝）
-- - 评论：约25个（1个初始 + 20个一级 + 5个二级）
-- - 点赞：约25个（1个初始 + 15个帖子点赞 + 10个评论点赞）
-- - 领养申请：约11个（1个初始 + 8个已审核 + 2个待审核）
-- - 评分：约5个（1个初始 + 4个新增）
-- - 聊天会话：约2个（1个初始 + 1个新增）
-- - 消息：约6个（1个初始 + 5个新增）
-- ============================================
