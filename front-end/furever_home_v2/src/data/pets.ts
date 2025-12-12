export interface Pet {
  id: number
  name: string
  photoText: string
  fosterer: string
  location: string
  species: 'cat' | 'dog'
  age: number
  gender: 'male' | 'female'
  adoption_status: 'adopted' | 'available'
}

export const pets: Pet[] = [
  {
    id: 1,
    name: '小橘',
    photoText: '小橘的照片',
    fosterer: '李同学',
    location: '广东省',
    species: 'cat',
    age: 1,
    gender: 'male',
    adoption_status: 'available'
  },
  {
    id: 2,
    name: '煤球',
    photoText: '煤球的照片',
    fosterer: '王同学',
    location: '北京市',
    species: 'dog',
    age: 0.5,
    gender: 'female',
    adoption_status: 'adopted'
  },
  {
    id: 3,
    name: '雪球',
    photoText: '雪球的照片',
    fosterer: '赵同学',
    location: '上海市',
    species: 'cat',
    age: 3,
    gender: 'female',
    adoption_status: 'available'
  },
  {
    id: 4,
    name: '小黑',
    photoText: '小黑的照片',
    fosterer: '李同学',
    location: '四川省',
    species: 'dog',
    age: 0.7,
    gender: 'male',
    adoption_status: 'adopted'
  },
  {
    id: 5,
    name: '灰灰',
    photoText: '灰灰的照片',
    fosterer: '刘同学',
    location: '浙江省',
    species: 'cat',
    age: 0.4,
    gender: 'female',
    adoption_status: 'available'
  },
  {
    id: 6,
    name: '卷卷',
    photoText: '卷卷的照片',
    fosterer: '杨同学',
    location: '江苏省',
    species: 'dog',
    age: 0.3,
    gender: 'male',
    adoption_status: 'available'
  },
  {
    id: 7,
    name: '花花',
    photoText: '花花的照片',
    fosterer: '张同学',
    location: '山东省',
    species: 'cat',
    age: 2,
    gender: 'female',
    adoption_status: 'available'
  },
  {
    id: 8,
    name: '豆豆',
    photoText: '豆豆的照片',
    fosterer: '王同学',
    location: '河南省',
    species: 'dog',
    age: 1,
    gender: 'male',
    adoption_status: 'adopted'
  },
  {
    id: 9,
    name: '咪咪',
    photoText: '咪咪的照片',
    fosterer: '赵同学',
    location: '湖北省',
    species: 'cat',
    age: 1,
    gender: 'female',
    adoption_status: 'available'
  },
  {
    id: 10,
    name: '旺旺',
    photoText: '旺旺的照片',
    fosterer: '李同学',
    location: '湖南省',
    species: 'dog',
    age: 2,
    gender: 'male',
    adoption_status: 'adopted'
  }
]
