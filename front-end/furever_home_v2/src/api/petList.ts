// 宠物列表接口类型与本地 mock 实现（暂时代替后端）

export interface PetListRequest {
  /** 根据动物领养状态筛选 */
  adoption_status?: string
  /** 根据动物性别筛选 */
  gender?: string
  /** 根据动物位置筛选 */
  location?: string
  /** 页码 */
  page?: number
  /** 页容量 */
  page_size?: number
  /** 根据动物种类筛选 */
  species?: string
  // 其余可扩展字段
  [property: string]: any
}

export interface PetListResponse {
  /** 状态码 */
  code: number
  data: PetListData
  /** 提示信息 */
  message: string
  [property: string]: any
}

export interface PetListData {
  animals: Animal[]
  /** 页码 */
  page: number
  /** 页容量 */
  page_size: number
  /** 总动物数 */
  total_count: number
  [property: string]: any
}

export interface Animal {
  /** 领养状态 */
  adoption_status: string
  /** 动物名（注意：后端字段为 anima_name，这里保留原样） */
  anima_name: string
  /** 动物月龄 */
  animal_age: number
  /** 动物ID */
  animal_id: number
  /** 动物品种 */
  breed: string
  /** 临时收养者姓名 */
  temporary_caregiver: string
  /** 动物位置 */
  location?: string
  /** 动物种类 */
  species?: string
  [property: string]: any
}

// ---- 本地 mock 数据和模拟接口实现 ----

const mockAnimals: Animal[] = [
  {
    adoption_status: 'available',
    anima_name: '小橘',
    animal_age: 13,
    animal_id: 1,
    breed: '橘猫',
    temporary_caregiver: '用户1003',
    location: '福州大学 xx 校区',
    species: 'cat'
  },
  {
    adoption_status: 'adopted',
    anima_name: '豆豆',
    animal_age: 24,
    animal_id: 2,
    breed: '金毛寻回犬',
    temporary_caregiver: '用户1001',
    location: '福州市 仓山区',
    species: 'dog'
  }
]

// 模拟后端分页 & 筛选逻辑
export async function fetchPetList(
  params: PetListRequest
): Promise<PetListResponse> {
  const {
    adoption_status,
    gender, // 当前前端还没有使用性别字段，预留
    location,
    species,
    page = 1,
    page_size = 20
  } = params

  let list = [...mockAnimals]

  if (location) {
    const q = location.trim()
    if (q) {
      list = list.filter((item) => item.location?.includes(q))
    }
  }

  if (species) {
    list = list.filter((item) => item.species === species)
  }

  if (adoption_status) {
    list = list.filter((item) => item.adoption_status === adoption_status)
  }

  // TODO: gender 字段未来接入真实数据后再补充过滤

  const total = list.length
  const start = (page - 1) * page_size
  const end = start + page_size
  const pageItems = list.slice(start, end)

  return {
    code: 0,
    message: 'success (mock)',
    data: {
      animals: pageItems,
      page,
      page_size,
      total_count: total
    }
  }
}
