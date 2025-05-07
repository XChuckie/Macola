<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="从海量图片中搜索"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>
    <!-- 分类 + 标签 -->
    <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryOptions" :key="category" :tab="category" />
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagOptions"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
      :pagination="pagination"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.url"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureV0ByPageUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

// 定义数据
const dataList = ref<API.PictureV0[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})
// 分页参数设置
const pagination = computed(() => {
  return {
    total: total.value, // 总数据条数
    current: searchParams.current ?? 1, // 当前页码
    pageSize: searchParams.pageSize ?? 10, // 每页显示条数
    // 切换页号时，修改搜索参数并获取数据
    onChange: (page: number, pageSize: number) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    },
  }
})
// 获取数据
const fetchData = async () => {
  loading.value = true
  const params = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagOptions.value[index])
    }
  })

  const res = await listPictureV0ByPageUsingPost(params)

  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}
// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 搜索功能：搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 获取预定义的分类和标签
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])
const selectedCategory = ref<string>('all')
const selectedTagList = ref<string[]>([])
/**
 * 获取标签和分类选项
 */
const getCategoryAndTagOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    categoryOptions.value = res.data.data.categoryList ?? []
    tagOptions.value = res.data.data.tagList ?? []
  } else {
    message.error('加载预定义分类和标签失败' + res.data.message)
  }
}
//  页面加载时调用
onMounted(() => {
  getCategoryAndTagOptions()
})

// 跳转详情页面
const routerNavigator = useRouter()
const doClickPicture = (picture: API.PictureV0) => {
  routerNavigator.push({
    path: `picture/${picture.id}`,
  })
}
</script>

<style scoped>
#homePage {
  margin-bottom: 16px;
}
#homePage .search-bar {
  max-width: 600px;
  margin: 0 auto 16px;
}
#homePage .tag-bar {
  margin-bottom: 16px;
}
</style>
