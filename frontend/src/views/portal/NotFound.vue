<template>
  <div class="notfound">
    <el-result icon="warning" title="404" sub-title="抱歉，您访问的页面不存在或已被移除">
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        <el-button @click="$router.back()">返回上页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.notfound { display: flex; align-items: center; justify-content: center; min-height: 70vh; }
</style>
