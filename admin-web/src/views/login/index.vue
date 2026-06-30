<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { login } from '../../api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = ref({
  username: 'admin',
  password: '123456',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true

  try {
    const res = await login(form.value)

    localStorage.setItem('admin_token', res.data.token)
    localStorage.setItem('admin_user', JSON.stringify(res.data))

    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-visual">
      <div class="visual-content">
        <p class="visual-kicker">Enterprise Admin</p>
        <h2>统一权限、组织与运营数据管理</h2>
        <p>面向企业后台的账号、权限、通知与审计基础平台。</p>
      </div>
    </div>

    <div class="login-panel">
      <div class="brand">
        <h1>后台管理系统</h1>
        <p>企业级运营管理平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            size="large"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="login-button"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(420px, 0.85fr);
  align-items: center;
  background:
    linear-gradient(90deg, rgb(7 20 42 / 88%) 0%, rgb(15 23 42 / 66%) 48%, rgb(248 250 252 / 94%) 100%),
    url('../../assets/hero.png') center/cover no-repeat;
}

.login-visual {
  min-height: 100vh;
  display: flex;
  align-items: flex-end;
  padding: 72px;
  color: #ffffff;
}

.visual-content {
  max-width: 560px;
}

.visual-kicker {
  margin: 0 0 14px;
  color: #93c5fd;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.visual-content h2 {
  margin: 0;
  font-size: 42px;
  line-height: 1.2;
  font-weight: 800;
}

.visual-content p:last-child {
  margin: 18px 0 0;
  color: #dbeafe;
  font-size: 16px;
  line-height: 1.8;
}

.login-panel {
  width: 420px;
  margin: 0 auto;
  padding: 38px;
  background: rgb(255 255 255 / 96%);
  border: 1px solid rgb(226 232 240 / 86%);
  border-radius: 8px;
  box-shadow: 0 24px 60px rgb(15 23 42 / 20%);
  backdrop-filter: blur(12px);
}

.brand {
  margin-bottom: 30px;
  text-align: center;
}

.brand h1 {
  margin: 0 0 8px;
  color: #0f172a;
  font-size: 30px;
}

.brand p {
  margin: 0;
  color: #64748b;
}

.login-button {
  width: 100%;
  height: 42px;
  margin-top: 10px;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .login-visual {
    display: none;
  }

  .login-panel {
    width: min(420px, 100%);
  }
}
</style>
