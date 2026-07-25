import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/flow/list',
  },
  {
    path: '/flow/list',
    name: 'FlowList',
    component: () => import('@/views/flow/list.vue'),
  },
  {
    path: '/flow/:appId',
    name: 'FlowEditor',
    component: () => import('@/views/flow/index.vue'),
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
