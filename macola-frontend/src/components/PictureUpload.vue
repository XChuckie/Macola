<template>
  <div id="pictureUpload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleChange"
      :before-upload="beforeUpload"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController'

interface Props {
  picture?: API.PictureV0 // 上传图片
  onSuccess?: (newPicture: API.PictureV0) => void // 上传成功后，将得到的新图片返回给父组件
}

const props = defineProps<Props>()

const loading = ref(false)

const handleChange = async ({ file }: any) => {
  loading.value = true
  try {
    // 1. 判断是新增 or 更新(获取所覆盖图片对应的id)
    const param = props.picture ? { id: props.picture.id } : {}
    // 2. 上传
    const res = await uploadPictureUsingPost(param, {}, file)
    // 3. 验证
    if (res.data.code === 0 && res.data.data) {
      // 上传成功，将返回信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('上传图片失败：' + res.data.message)
    }
  } catch (error) {
    message.error('上传图片失败')
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPngOrJpeg =
    file.type === 'image/jpg' || file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPngOrJpeg) {
    message.error('不支持上传该图片格式，推荐上传jpg或jpeg或png')
  }
  const isLt2M = file.size / 1024 / 1024
  if (!isLt2M) {
    message.error('不支持上传超过2M得图片')
  }
  return isJpgOrPngOrJpeg && isLt2M
}
</script>

<style scoped>
#pictureUpload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

#pictureUpload img {
  max-width: 100%;
  max-height: 480px;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
