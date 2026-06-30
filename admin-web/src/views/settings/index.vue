<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword } from '../../api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const currentUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }

  callback()
}

const rules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度必须为 6 到 32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true

  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    })

    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    router.push('/login')
  } finally {
    submitLoading.value = false
  }
}

const handleReset = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <div>
        <h1>系统设置</h1>
        <p>维护当前登录账号和安全信息</p>
      </div>
    </div>

    <div class="settings-grid">
      <section class="account-panel">
        <div class="avatar">
          {{ String(currentUser.nickname || currentUser.username || '管').slice(0, 1) }}
        </div>

        <div class="account-name">{{ currentUser.nickname || '-' }}</div>
        <div class="account-role">{{ currentUser.roleName || '-' }}</div>

        <div class="info-list">
          <div class="info-item">
            <span>用户名</span>
            <strong>{{ currentUser.username || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>用户 ID</span>
            <strong>{{ currentUser.userId || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>角色编码</span>
            <strong>{{ currentUser.roleCode || '-' }}</strong>
          </div>
        </div>
      </section>

      <section class="password-panel">
        <div class="panel-header">
          <h2>修改密码</h2>
          <p>修改成功后需要重新登录</p>
        </div>

        <el-form
          ref="formRef"
          class="password-form"
          :model="form"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="form.oldPassword"
              type="password"
              show-password
              placeholder="请输入原密码"
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              show-password
              placeholder="请输入 6 到 32 位新密码"
            />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
              保存修改
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </el-form>
      </section>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

.page-header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.settings-grid {
  display: grid;
  grid-template-columns: 360px minmax(480px, 760px);
  gap: 20px;
  align-items: start;
}

.account-panel,
.password-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.account-panel {
  padding: 28px 24px;
  text-align: center;
}

.avatar {
  width: 72px;
  height: 72px;
  margin: 0 auto 14px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8f3ff;
  color: #1677ff;
  font-size: 30px;
  font-weight: 700;
}

.account-name {
  color: #111827;
  font-size: 20px;
  font-weight: 700;
}

.account-role {
  margin-top: 6px;
  color: #6b7280;
  font-size: 14px;
}

.info-list {
  margin-top: 24px;
  border-top: 1px solid #edf0f5;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 15px 0;
  border-bottom: 1px solid #edf0f5;
  text-align: left;
}

.info-item span {
  color: #8a8f99;
}

.info-item strong {
  color: #1f2937;
  font-weight: 600;
  word-break: break-all;
}

.password-panel {
  padding: 0;
}

.panel-header {
  padding: 22px 28px;
  border-bottom: 1px solid #edf0f5;
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.panel-header p {
  margin: 6px 0 0;
  color: #8a8f99;
  font-size: 13px;
}

.password-form {
  max-width: 520px;
  padding: 26px 28px 28px;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 4px;
}

@media (max-width: 1100px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }

  .password-form {
    max-width: none;
  }
}
</style>
