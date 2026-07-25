import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import mitt from 'mitt'

const app = createApp(App)
const pinia = createPinia()

// Global event bus
const bus = mitt()
app.provide('$bus', bus)
app.config.globalProperties.$bus = bus

app.use(pinia)
app.use(router)
app.use(Antd)
app.mount('#app')
