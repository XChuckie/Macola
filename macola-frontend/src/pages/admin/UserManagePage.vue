<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userName'">
          <div v-if="editableData[record.id]">
            <a-input v-model:value="editableData[record.id].userName" style="width: 100%" />
          </div>
          <div v-else>
            {{ record.userName }}
          </div>
        </template>
        <template v-if="column.dataIndex === 'userAvatar'">
          <div v-if="editableData[record.id]">
            <a-input v-model:value="editableData[record.id].userAvatar" style="width: 100%" />
          </div>
          <div v-else>
            <a-image :src="record.userAvatar" :width="50" />
          </div>
        </template>
        <template v-if="column.dataIndex === 'userProfile'">
          <div v-if="editableData[record.id]">
            <a-input v-model:value="editableData[record.id].userProfile" style="width: 100%" />
          </div>
          <div v-else>
            {{ record.userProfile }}
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="editableData[record.id]">
            <a-input v-model:value="editableData[record.id].userRole" style="width: 100%" />
          </div>
          <div v-else>
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <!--            <a-button type="primary" @click="showEditModal(record)">编辑</a-button>-->
            <span v-if="editableData[record.id]">
              <a-typography-link @click="save(record.id)">保存</a-typography-link>
              <a-popconfirm title="确定取消编辑吗?" @confirm="cancel(record.id)">
                <a>取消</a>
              </a-popconfirm>
            </span>
            <span v-else>
              <a @click="edit(record.id)">编辑</a>
            </span>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, type UnwrapRef } from 'vue'
import { message } from 'ant-design-vue'
import {
  deleteUserUsingPost,
  listUserV0ByPageUsingPost,
  updateUserUsingPost,
} from '@/api/userController'
import dayjs from 'dayjs'

// 定义展示的数据信息
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 定义数据
const dataList = ref<API.UserV0[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})
// 获取数据
const fetchData = async () => {
  const res = await listUserV0ByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}
// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})
// 分页参数设置
const pagination = computed(() => {
  return {
    total: total.value, // 总数据条数
    current: searchParams.current ?? 1, // 当前页码
    pageSize: searchParams.pageSize ?? 10, // 每页显示条数
    showSizeChanger: true, // 是否显示可以改变 pageSize 的下拉框
    showTotal: (total) => `共 ${total} 条`, // 显示总数据条数
  }
})
// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索功能：搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    await fetchData()
  } else {
    message.error('删除失败')
  }
}

// 用于行内编辑的数据
const editableData: UnwrapRef<Record<string, API.UserV0>> = reactive({})
// 编辑
const edit = (id: number) => {
  const record = dataList.value.filter((item) => item.id === id)[0]
  if (record) {
    editableData[id] = {
      id: record.id,
      userName: record.userName,
      userAvatar: record.userAvatar,
      userProfile: record.userProfile,
      userRole: record.userRole,
    }
  }
}
// 保存
const save = async (id: number) => {
  // 提交
  const res = await updateUserUsingPost({
    id,
    ...editableData[id],
  })
  // 判断
  if (res.data.code === 0) {
    message.success('更新成功')
    await fetchData() // 重新刷新用户列表
    delete editableData[id] // 删除缓存数据
  } else {
    message.error('更新失败：' + res.data.message)
  }
}
// 取消
const cancel = (id: number) => {
  delete editableData[id]
}
</script>
