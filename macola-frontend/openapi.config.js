import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://localhost:8123/api/v2/api-docs', // 根据后端实际运行的端口进行修改
  serversPath: './src',
})
