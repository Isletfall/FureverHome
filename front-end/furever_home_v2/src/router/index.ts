import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Home from '../views/home/Home.vue'
import PetList from '../views/pet/PetList.vue'
import PostPet from '../views/pet/PostPet.vue'
import PetDetail from '../views/pet/PetDetail.vue'
import ApplyAdoption from '../views/pet/ApplyAdoption.vue'
import Talk from '../views/talk/Talk.vue'
import PostList from '../views/forum/PostList.vue'
import PostDetail from '../views/forum/PostDetail.vue'
import Profile from '../views/user/Profile.vue'
import UserCenter from '../views/user/UserCenter.vue'
import Login from '../views/user/Login.vue'
import Register from '../views/user/Register.vue'
import ResetPasswordRequest from '../views/user/ResetPasswordRequest.vue'
import ResetPasswordNew from '../views/user/ResetPasswordNew.vue'
import ResetPasswordSuccess from '../views/user/ResetPasswordSuccess.vue'
import NotFound from '../views/error/404.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminPosts from '../views/admin/Posts.vue'
import AdminPets from '../views/admin/Pets.vue'
import AdminApplications from '../views/admin/Applications.vue'

// 来自 cwx 的新增发布帖子页面
import PostNew from '../views/forum/PostNew.vue'

// 登录流程相关页面
import LoginEntry from '../views/user/LoginEntry.vue'
import LoginEmailEnter from '../views/user/LoginEmailEnter.vue'
import LoginEmail from '../views/user/LoginEmail.vue'
import LoginSuccess from '../views/user/LoginSuccess.vue';
import LoginPassword from '../views/user/LoginPassword.vue';


const routes: RouteRecordRaw[] = [
  {
    // 入口路径：重定向到登录页
    path: '/',
    redirect: '/login'
  },
  {
    // 平台首页
    path: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/pets',
    name: 'PetList',
    component: PetList
  },
  {
    path: '/post-pet',
    name: 'PostPet',
    component: PostPet
  },
  {
    path: '/pet/:id',
    name: 'PetDetail',
    component: PetDetail
  },
  {
    path: '/pet/:id/apply',
    name: 'ApplyAdoption',
    component: ApplyAdoption
  },
  {
    path: '/communication',
    name: 'Communication',
    component: Talk
  },
  {
    path: '/forum',
    name: 'Forum',
    component: PostList
  },
  {
    path: '/forum/:id',
    name: 'PostDetail',
    component: PostDetail
  },
  {
    path: '/forum/new',
    name: 'PostNew',
    component: PostNew
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile
  },
  {
    path: '/profile/:userId',
    name: 'UserProfile',
    component: Profile
  },
  {
    path: '/user-center',
    name: 'UserCenter',
    component: UserCenter
  },
  {
    // 登录首屏：选择邮箱验证码 / 密码登录 / 注册
    path: '/login',
    name: 'Login',
    component: LoginEntry
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    // 管理后台登录：重用前台的密码登录页，保持登录页统一
    path: '/admin/login',
    name: 'AdminLogin',
    redirect: { name: 'LoginPassword', query: { from: 'admin' } }
  },
  {
    path: '/admin',
    component: AdminLayout,
    beforeEnter: (to, from, next) => {
      const token = localStorage.getItem('token') || localStorage.getItem('saTokenValue')
      if (!token) {
        next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
      } else {
        next()
      }
    },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: AdminDashboard
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: AdminPosts
      },
      {
        path: 'pets',
        name: 'AdminPets',
        component: AdminPets
      },
      {
        path: 'applications',
        name: 'AdminApplications',
        component: AdminApplications
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  },
  {
    // 输入邮箱，准备发送登录验证码
    path: '/login/email-enter',
    name: 'LoginEmailEnter',
    component: LoginEmailEnter
  },
  {
    // 输入验证码页面
    path: '/login/email',
    name: 'LoginEmail',
    component: LoginEmail
  },
  {
    // 密码登录页面（复用原来的 Login.vue，已接好登录接口）
    path: '/login/password',
    name: 'LoginPassword',
    component: LoginPassword
  },
  {
    path: '/reset-password',
    name: 'ResetPasswordRequest',
    component: ResetPasswordRequest
  },
  {
    path: '/reset-password/new',
    name: 'ResetPasswordNew',
    component: ResetPasswordNew
  },
  {
    path: '/reset-password/success',
    name: 'ResetPasswordSuccess',
    component: ResetPasswordSuccess
  },
  {
    path: '/login-success',
    name: 'LoginSuccess',
    component: LoginSuccess
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router
