CREATE DATABASE IF NOT EXISTS furever_home DEFAULT CHARACTER SET utf8mb4;
USE furever_home;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS email_verification, review, rating, comments, likes, message, chat, adopt, post, animal, user_roles, role_permissions, permissions, roles, users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    user_age INT DEFAULT NULL COMMENT '年龄',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '邮箱号',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像',
    sex ENUM('男', '女', '保密') DEFAULT NULL COMMENT '性别',
    location VARCHAR(50) DEFAULT NULL COMMENT '所在地',
    proof_text VARCHAR(500) DEFAULT NULL COMMENT '爱宠证明简介',
    proof_photo JSON DEFAULT NULL COMMENT '爱宠证明',
    credit_score FLOAT DEFAULT 0 CHECK (credit_score BETWEEN 0 AND 5) COMMENT '信用分',
    credit_score_count INT DEFAULT 0 COMMENT '信用分评分人数',
    status ENUM('正常','禁用') NOT NULL DEFAULT '正常' COMMENT '账户状态',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    last_login_at TIMESTAMP NULL DEFAULT NULL COMMENT '最近登录时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '存放用户信息';

CREATE TABLE animal (
  animal_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '动物ID',
  user_id INT NOT NULL COMMENT '拥有该动物的用户ID',
  animal_name VARCHAR(50) NOT NULL COMMENT '动物名字',
  photo_urls JSON NOT NULL COMMENT '动物照片',
  species ENUM('狗', '猫', '兔子', '仓鼠', '鸟类', '鱼类', '龟类', '其他') NOT NULL COMMENT '动物种类（大类）',
  breed VARCHAR(50) COMMENT '动物品种（细分）',
  gender ENUM('公', '母', '未知') NOT NULL COMMENT '动物性别',
  animal_age INT NOT NULL CHECK (`animal_age` >= 0) COMMENT '动物月龄',
  health_status VARCHAR(200) NOT NULL COMMENT '健康状态描述',
  is_sterilized ENUM('是','否','未知') DEFAULT NULL COMMENT '是否绝育',
  adoption_status ENUM('短期领养', '长期领养') NOT NULL COMMENT '领养状态',
  short_description VARCHAR(200) COMMENT '宠物简介',
  review_status ENUM('待审核','通过','拒绝') NOT NULL DEFAULT '待审核',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',

-- 外键约束：关联 users 表
CONSTRAINT fk_animal_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动物信息（短期领养者发布的动物信息）';

CREATE TABLE chat (
  chat_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天ID',
  creator_id INT NOT NULL COMMENT '发起聊天者的user_id',
  receiver_id INT NOT NULL COMMENT '接收聊天者的user_id',
  create_time DATETIME NOT NULL COMMENT '创建聊天时间',
  last_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近聊天时间',

-- 外键约束，关联到 users 表


CONSTRAINT fk_chat_creator
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
    
  CONSTRAINT fk_chat_receiver
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天基础信息';

# chat 表要使用 InnoDB 引擎，并采用 utf8mb4 编码，在元数据中注明它是‘聊天基础信息’表。

CREATE TABLE message (
  message_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  chat_id INT NOT NULL COMMENT '关联的聊天会话ID',
  sender_id INT NOT NULL COMMENT '发送者的用户ID',
  content TEXT NOT NULL COMMENT '消息内容',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

-- 外键约束：关联 chat 表
CONSTRAINT fk_message_chat FOREIGN KEY (chat_id) REFERENCES chat (chat_id) ON DELETE CASCADE ON UPDATE CASCADE,

-- 外键约束：关联 users 表
CONSTRAINT fk_message_sender
    FOREIGN KEY (sender_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录每个聊天会话的消息';

CREATE TABLE adopt (
    adopt_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '领养记录ID',
    animal_id INT NOT NULL COMMENT '领养动物ID',
    user_id INT NOT NULL COMMENT '申请人/领养用户ID',
    application_status ENUM('申请中','申请成功','申请失败') NOT NULL DEFAULT '申请中' COMMENT '申请领养的状态',
    review_status ENUM('待审核','通过','拒绝') NOT NULL DEFAULT '待审核',
    living_environment ENUM('宿舍','公寓','别墅','其他') NOT NULL COMMENT '申请者居住环境',
    house_type ENUM('拥有','租用') NOT NULL COMMENT '申请者房屋产权',
    has_other_pets BOOLEAN NOT NULL COMMENT '申请者是否拥有其他动物',
    family_member_count INT NOT NULL CHECK (family_member_count > 0) COMMENT '申请者家庭同居人数',
    has_child BOOLEAN NOT NULL COMMENT '申请者是否有小孩',
    adopt_reason VARCHAR(1000) NOT NULL COMMENT '领养原因',
    month_salary INT NOT NULL CHECK (month_salary > 0) COMMENT '申请者月工资',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    pass_time TIMESTAMP NULL COMMENT '审核结束时间，管理员审核完一条申请记录后插入当前时间',

-- 外键约束：关联 animal 表
CONSTRAINT fk_adopt_animal FOREIGN KEY (animal_id) REFERENCES animal (animal_id) ON DELETE CASCADE ON UPDATE CASCADE,

-- 外键约束：关联 users 表
CONSTRAINT fk_adopt_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,

-- 添加唯一约束，防止同一用户对同一动物重复申请
UNIQUE KEY uk_adopt_animal_user (animal_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录动物从申请领养和领养的全部信息';

CREATE TABLE post (
    post_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    user_id INT NOT NULL COMMENT '发帖用户的ID',
    title VARCHAR(100) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子文本内容',
    review_status ENUM('待审核','通过','拒绝') NOT NULL DEFAULT '待审核',
    media_urls JSON DEFAULT NULL COMMENT '帖子图片内容',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞量',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论量',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发帖时间',

-- 外键约束：关联 users 表
CONSTRAINT fk_post_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子';

CREATE TABLE likes (
    like_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    user_id INT NOT NULL COMMENT '点赞者ID',
    kind ENUM('帖子','评论') NOT NULL COMMENT '点赞类型',
    target_id INT NOT NULL COMMENT '被点赞者ID',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',

-- 外键约束：关联 users 表
CONSTRAINT fk_like_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞关系表';

CREATE TABLE review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    target_type ENUM('animal','post','adopt') NOT NULL,
    target_id INT NOT NULL,
    status ENUM('待审核','通过','拒绝') NOT NULL DEFAULT '待审核',
    reviewer_id INT NULL,
    reason TEXT DEFAULT NULL,
    extra_info JSON DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_review_target (target_type, target_id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 被点赞者可能引用 post 表或 comments 表，无法在数据库层面设置单一的外键约束
# 需要添加触发器

CREATE TABLE comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    post_id INT NOT NULL COMMENT '评论属于哪篇帖子',
    user_id INT NOT NULL COMMENT '发表评论的用户ID',
    parent_comment_id INT NULL COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT NOT NULL DEFAULT 0 COMMENT '该条评论的点赞量',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',

-- 外键约束：关联 post 表
CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE ON UPDATE CASCADE,

-- 外键约束：关联 users 表
CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,

-- 外键约束：自引用，关联父评论
CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存放评论相关数据';

CREATE TABLE rating (
    rating_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '信用评分记录ID',
    user_id INT NOT NULL COMMENT '发布评分的用户ID',
    target_user_id INT NOT NULL COMMENT '被评价的用户ID',
    adopt_id INT NULL COMMENT '关联的领养记录ID（可选）',
    score INT NOT NULL CHECK (score BETWEEN 1 AND 5) COMMENT '评分分数',
    content TEXT DEFAULT NULL COMMENT '评价内容',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 外键约束：关联 users 表
CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT fk_rating_target_user FOREIGN KEY (target_user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,

    -- 外键约束：关联 adopt 表（可选）
CONSTRAINT fk_rating_adopt FOREIGN KEY (adopt_id) REFERENCES adopt (adopt_id) ON DELETE CASCADE ON UPDATE CASCADE,

    -- 添加唯一约束，防止同一用户对同一用户重复评分
UNIQUE KEY uk_rating_user_target (user_id, target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录评分信息';

-- RBAC 权限模型
CREATE TABLE roles (
  role_id INT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE permissions (
  permission_id INT AUTO_INCREMENT PRIMARY KEY,
  perm_code VARCHAR(100) NOT NULL UNIQUE,
  perm_name VARCHAR(100) NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE user_roles (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联';

CREATE TABLE role_permissions (
  role_id INT NOT NULL,
  permission_id INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_role_permissions_perm FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联';




-- 初始化 RBAC 数据
INSERT INTO roles (role_code, role_name, description) VALUES
('ADMIN','管理员','系统最高权限'),
('MOD','版主','内容审核与管理'),
('USER','用户','普通用户权限');

INSERT INTO permissions (perm_code, perm_name, description) VALUES
('user:read','用户查看',NULL),('user:ban','用户封禁',NULL),
('animal:create','动物创建',NULL),('animal:read','动物查看',NULL),('animal:review','动物审核',NULL),
('adopt:apply','提交领养申请',NULL),('adopt:review','审核领养申请',NULL),('adopt:read','领养记录查看',NULL),
('post:create','帖子创建',NULL),('post:read','帖子查看',NULL),('post:delete','帖子删除',NULL),
('comment:create','评论创建',NULL),('comment:read','评论查看',NULL),('comment:delete','评论删除',NULL),
('chat:start','发起聊天',NULL),('chat:send_message','发送消息',NULL),('chat:read','聊天查看',NULL),
('rating:create','评分创建',NULL),('rating:read','评分查看',NULL),('rating:delete','评分删除',NULL);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r CROSS JOIN permissions p WHERE r.role_code='ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r JOIN permissions p
  ON r.role_code='MOD' AND p.perm_code IN (
    'animal:read','animal:review',
    'adopt:read','adopt:review',
    'post:read','post:delete',
    'comment:read','comment:delete',
    'chat:read','rating:read'
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r JOIN permissions p
  ON r.role_code='USER' AND p.perm_code IN (
    'animal:create','animal:read',
    'adopt:apply','adopt:read',
    'post:create','post:read',
    'comment:create','comment:read',
    'chat:start','chat:send_message','chat:read',
    'rating:create','rating:read'
  );

-- 初始化用户与用户-角色映射
INSERT INTO users (user_name, user_age, password_hash, email, avatar_url, sex, location, proof_text, proof_photo)
VALUES
('admin', 30, '$2a$10$eImG8G7G7G7G8G7G8G7G8uOqzTQn5Jf0gDsw4hV1Bf6e2bC/1aQyS', 'admin@example.com', NULL, '男', '福州', NULL, NULL),
('mod',   28, '$2a$10$eImG8G7G7G7G8G7G8G7G8uOqzTQn5Jf0gDsw4hV1Bf6e2bC/1aQyS', 'mod@example.com',   NULL, '女', '福州', NULL, NULL),
('alice', 24, '$2a$10$eImG8G7G7G7G8G7G8G7G8uOqzTQn5Jf0gDsw4hV1Bf6e2bC/1aQyS', 'alice@example.com', NULL, '女', '厦门', '多年养宠经验', JSON_ARRAY('https://img.example.com/p1.jpg')),
('bob',   26, '$2a$10$eImG8G7G7G7G8G7G8G7G8uOqzTQn5Jf0gDsw4hV1Bf6e2bC/1aQyS', 'bob@example.com',   NULL, '男', '泉州', NULL, NULL);

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u JOIN roles r ON u.user_name='admin' AND r.role_code='ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u JOIN roles r ON u.user_name='mod' AND r.role_code='MOD';
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u JOIN roles r ON u.user_name IN ('alice','bob') AND r.role_code='USER';

-- 初始化业务数据
INSERT INTO animal (user_id, animal_name, photo_urls, species, breed, gender, animal_age, health_status, is_sterilized, adoption_status, short_description)
SELECT u.user_id, '雪球', JSON_ARRAY('https://img.example.com/cat1.jpg'), '猫', '英短', '母', 12, '健康', '是', '短期领养', '活泼可爱'
FROM users u WHERE u.user_name='alice';

INSERT INTO post (user_id, title, content, media_urls)
SELECT u.user_id, '关于雪球的日常', '雪球很黏人，欢迎了解', JSON_ARRAY('https://img.example.com/cat1-post.jpg')
FROM users u WHERE u.user_name='alice';

INSERT INTO comments (post_id, user_id, content)
SELECT p.post_id, u.user_id, '好可爱！'
FROM post p JOIN users u ON p.title='关于雪球的日常' AND u.user_name='bob';

INSERT INTO likes (user_id, kind, target_id)
SELECT u.user_id, '帖子', p.post_id
FROM users u JOIN post p ON u.user_name='bob' AND p.title='关于雪球的日常';

INSERT INTO chat (creator_id, receiver_id, create_time)
SELECT u1.user_id, u2.user_id, CURRENT_TIMESTAMP
FROM users u1 JOIN users u2 ON u1.user_name='alice' AND u2.user_name='bob';

INSERT INTO message (chat_id, sender_id, content)
SELECT c.chat_id, u.user_id, '你好，想了解雪球的情况'
FROM chat c JOIN users u ON u.user_name='bob'
ORDER BY c.chat_id DESC LIMIT 1;

INSERT INTO adopt (animal_id, user_id, application_status, living_environment, house_type, has_other_pets, family_member_count, has_child, adopt_reason, month_salary, pass_time)
SELECT a.animal_id, u.user_id, '申请成功', '公寓', '租用', FALSE, 2, FALSE, '非常喜欢猫咪，有稳定收入', 8000, CURRENT_TIMESTAMP
FROM animal a JOIN users u ON a.animal_name='雪球' AND u.user_name='bob';

INSERT INTO rating (user_id, target_user_id, adopt_id, score, content)
SELECT ur.user_id, owner.user_id AS target_user_id, ad.adopt_id, 5, '领养过程顺利，沟通良好'
FROM users ur JOIN adopt ad ON ur.user_name='alice'
JOIN animal an ON ad.animal_id = an.animal_id
JOIN users owner ON owner.user_id = an.user_id;