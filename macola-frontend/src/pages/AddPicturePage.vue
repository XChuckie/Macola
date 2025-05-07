<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <PictureUpload :picture="picture" :onSuccess="onSuccess" />

    <a-form v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item name="name" label="名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea
          :rows="2"
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          auto-size
          allow-clear
        />
      </a-form-item>
      <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="tags" label="标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" style="width: 100%" html-type="submit">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureV0ByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

const picture = ref<API.PictureV0>()
const pictureForm = reactive<API.PictureEditRequest>({})

/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureV0) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const routerNavigator = useRouter()
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  // 1. 检验图片是否存在
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  // 2. 上传
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })
  // 3. 校验
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    await routerNavigator.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败' + res.data.message)
  }
}

interface Option {
  value: string
  label: string
}
const categoryOptions = ref<Option[]>([])
const tagOptions = ref<Option[]>([])
/**
 * 获取标签和分类选项
 */
const getCategoryAndTagOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    categoryOptions.value = (res.data.data.categoryList ?? []).map((item: string) => {
      return {
        value: item,
        label: item,
      }
    })
    tagOptions.value = (res.data.data.tagList ?? []).map((item: string) => {
      return {
        value: item,
        label: item,
      }
    })
  } else {
    message.error('加载预定义分类和标签失败' + res.data.message)
  }
}
//  页面加载时调用
onMounted(() => {
  getCategoryAndTagOptions()
})

const route = useRoute() // 获取路由信息
/**
 * 图片信息修改，通过URL查询参数后增加?id=xxx表示修改对应id的图片，否则就表示创建图片
 */
const getOldPicture = async () => {
  // 1. 获取图片id
  const id = route.query?.id ? String(route.query.id) : undefined

  if (id) {
    // 2. 获取图片信息
    const res = await getPictureV0ByIdUsingGet({
      id,
    })
    // 3. 验证
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}
onMounted(() => {
  getOldPicture()
})
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
