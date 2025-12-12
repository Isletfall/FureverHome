// 宠物详情接口类型与示例 mock 实现（暂时代替后端）

// 请求：根据动物 ID 获取详情
export interface PetDetailRequest {
  /** 动物ID */
  animal_id: number
  [property: string]: any
}

// 响应外层
export interface PetDetailResponse {
  /** 200，状态码 */
  code: number
  /** 详情数据 */
  data: PetDetailData
  /** 获取动物详情成功，提示信息 */
  message: string
  [property: string]: any
}

// 详情数据主体
export interface PetDetailData {
  /** 领养状态 */
  adoption_status: string
  /** 动物月龄 */
  animal_age: number
  /** 动物ID */
  animal_id: number
  /** 所在位置 */
  animal_location: string
  /** 动物名 */
  animal_name: string
  /** 动物品种 */
  breed: string
  /** 详细描述 */
  detail_info: string
  /** 动物性别 */
  gender: string
  /** 健康状态 */
  health_status: string
  /** 是否绝育 */
  is_sterilized: string
  /** 动物图片列表 */
  photo_urls: string[]
  /** 短暂收养者信息 */
  publisher: Publisher
  /** 动物简介 */
  short_description: string
  /** 动物种类 */
  species: string
  [property: string]: any
}

/** 短暂收养者信息 */
export interface Publisher {
  /** 收养者头像 */
  avatar_url: string
  /** 收养者信用分 */
  credit_score: string
  /** 收养者ID */
  user_id: string
  /** 收养者姓名 */
  username: string
  [property: string]: any
}

// ---- 示例 mock 数据与函数：现在未在页面中使用，仅作为未来对接模板 ----

const mockDetail: PetDetailData = {
  adoption_status: 'available',
  animal_age: 13,
  animal_id: 1,
  animal_location: '福州大学 xx 校区',
  animal_name: '小橘',
  breed: '橘猫',
  detail_info: '小橘于 2022 年在福州大学校园被发现，性格温顺，已经完成基础体检...',
  gender: '公',
  health_status: '已免疫、已驱虫，无重大疾病史',
  is_sterilized: '是',
  photo_urls: [
    'https://static.example.com/pets/1/photo1.jpg',
    'https://static.example.com/pets/1/photo2.jpg'
  ],
  publisher: {
    avatar_url: 'https://static.example.com/users/1003/avatar.jpg',
    credit_score: '4.9',
    user_id: '1003',
    username: '用户1003'
  },
  short_description: '性格活泼温顺的小橘。',
  species: 'cat'
}

/**
 * 示例：根据 animal_id 获取宠物详情的 mock 函数
 * 说明：
 * - 当前仅返回本地 mockDetail；
 * - 未来接后端时，可以在这里改成真实的 fetch/axios 请求：
 *   - GET /api/animal/detail?animal_id=xxx
 */
export async function fetchPetDetail(
  params: PetDetailRequest
): Promise<PetDetailResponse> {
  // TODO: 接后端时，将这里改为真实网络请求
  console.log('fetchPetDetail mock called with:', params)

  return {
    code: 200,
    message: '获取动物详情成功（mock）',
    data: mockDetail
  }
}
