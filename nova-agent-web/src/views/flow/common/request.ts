import http from '@/utils/http'

interface RequestArgs {
  url: string
  method?: string
  params?: Record<string, any>
  data?: any
}

export function flowRequest(args: RequestArgs): Promise<any> {
  const { url, method = 'get', params, data } = args
  const config: any = {
    url,
    method,
    params,
    data,
  }
  // Add product line header if available
  const headers: Record<string, string> = {}
  if (params && (params as any).productLine) {
    headers['PRODUCT-LINE-ID'] = (params as any).productLine
  }
  config.headers = headers
  return http(config)
}
