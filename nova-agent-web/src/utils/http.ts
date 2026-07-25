import axios from 'axios'

const http = axios.create({
  timeout: 30000,
})

http.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 0 || data.code === 200) {
      return data.data ?? data
    }
    return Promise.reject(data)
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default http
