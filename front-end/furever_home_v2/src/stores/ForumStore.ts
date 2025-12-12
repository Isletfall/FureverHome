import { reactive, readonly} from 'vue';
import { formatDateTime } from '@/utils/format';

// ----------------------------------------
// 1. 定义类型
// ----------------------------------------

// 媒体文件类型
export interface MediaFile {
    url: string;
    type: 'image' | 'video';
}

// 帖子类型
export interface Post {
    id: number;
    title: string;
    content: string;
    author: string;
    avatarUrl: string;
    publishDate: string; // 格式 'YYYY-MM-DD'
    views: number;
    likes: number;
    comments: number;
    media: MediaFile[];
}

// 状态接口
interface ForumState {
    posts: Post[];
    nextId: number;
}

// ----------------------------------------
// 2. 状态定义
// ----------------------------------------

// 预设数据（确保这些帖子ID从1开始，可以被列表和详情页查找到）
const initialState: ForumState = {
    posts: [
        {
            id: 1,
            title: '小橘的领养更新：越来越亲人啦！',
            content: '自从领养了小橘，家里每天都充满了欢声笑语。它刚来的时候有点怕生，现在已经会主动在脚边蹭来蹭去要吃的了！谢谢FUREVERHOME平台。',
            author: '领养人-莉莉',
            avatarUrl: 'https://i.ibb.co/L8bY02N/avatar-lily.jpg', // 示例头像URL
            publishDate: '2025-11-20',
            views: 450,
            likes: 88,
            comments: 15,
            media: [
                { url: 'https://i.ibb.co/L8bY02N/post-media-1.jpg', type: 'image' }, // 示例图片URL
            ],
        },
        {
            id: 2,
            title: '分享我的流浪狗救助经验：如何处理应激反应',
            content: '最近救助了一只受到惊吓的流浪狗，分享一下我处理应激反应的经验和步骤。首先要保持安静，不要直视它的眼睛...',
            author: '爱心大使-小王',
            avatarUrl: 'https://i.ibb.co/7j9d0gP/avatar-xiaowang.jpg',
            publishDate: '2025-11-18',
            views: 620,
            likes: 120,
            comments: 30,
            media: [],
        },
    ],
    nextId: 3, // 下一个帖子的 ID 从 3 开始
};

// ----------------------------------------
// 3. 响应式状态和方法
// ----------------------------------------

const state = reactive<ForumState>(initialState);

/**
 * 添加一个新帖子到列表中
 */
function addPost(
    newPostData: { title: string; content: string },
    uploadedFiles: { name: string; preview: string; type: string; file: File }[]
): number {

    const mediaList: MediaFile[] = uploadedFiles.map(file => ({
        url: file.preview,
        type: file.type.startsWith('image') ? 'image' : 'video' as 'image' | 'video',
    }));

    const newPost: Post = {
        id: state.nextId,
        title: newPostData.title,
        content: newPostData.content,
        author: '当前用户',
        avatarUrl: 'https://i.ibb.co/34Gf4rW/avatar-default.png',
        // 修正日期格式，避免 TS 错误
        publishDate: formatDateTime(new Date()),
        views: 0,
        likes: 0,
        comments: 0,
        media: mediaList,
    };

    // 将新帖子添加到列表顶部
    state.posts.unshift(newPost);
    state.nextId++;

    return newPost.id;
}


// ----------------------------------------
// 4. 导出
// ----------------------------------------

export function useForumStore() {
    return {
        // 帖子列表 (只读)
        posts: readonly(state.posts) as Readonly<Post[]>,

        // 方法
        addPost,

        /**
         * 通过 ID 获取帖子详情
         * @param id 帖子 ID (number)
         * @returns 帖子对象或 undefined
         */
        getPostById: (id: number): Post | undefined => {
            // 确保使用数字 ID 进行查找
            return state.posts.find(post => post.id === id);
        }
    };
}
