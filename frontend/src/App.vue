<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  BookOpen,
  CheckCircle2,
  Copy,
  Download,
  FileText,
  ListChecks,
  Sparkles,
  Upload,
  Users,
  Wand2
} from 'lucide-vue-next'
import {
  extractCharacters,
  generateFromText,
  generateYaml,
  planScenes,
  splitChapters,
  uploadNovel,
  validateYaml
} from './api/screenplayApi'

const title = ref('雨夜来客')
const selectedFile = ref(null)
const novelText = ref('')
const uploadInfo = ref(null)
const chapters = ref([])
const characters = ref([])
const scenes = ref([])
const yamlText = ref('')
const validation = ref(null)

const loading = reactive({
  upload: false,
  split: false,
  characters: false,
  scenes: false,
  yaml: false,
  all: false,
  validate: false
})

const activeStep = computed(() => {
  if (yamlText.value) return 4
  if (scenes.value.length) return 3
  if (characters.value.length) return 2
  if (chapters.value.length) return 1
  return 0
})

const canExport = computed(() => validation.value?.valid === true)
const validationWarnings = computed(() => validation.value?.warnings ?? [])

watch(yamlText, () => {
  validation.value = null
})

function handleFileChange(file) {
  selectedFile.value = file.raw
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择 .txt 文件')
    return
  }

  loading.upload = true
  try {
    const [info, text] = await Promise.all([
      uploadNovel(selectedFile.value),
      selectedFile.value.text()
    ])
    uploadInfo.value = info
    novelText.value = text
    ElMessage.success('上传完成')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.upload = false
  }
}

async function handleSplit() {
  if (!novelText.value.trim()) {
    ElMessage.warning('小说正文不能为空')
    return
  }

  loading.split = true
  try {
    const result = await splitChapters(novelText.value)
    chapters.value = result.chapters
    ElMessage.success('章节已生成')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.split = false
  }
}

async function handleExtractCharacters() {
  loading.characters = true
  try {
    const result = await extractCharacters(chapters.value)
    characters.value = result.characters
    ElMessage.success('角色已生成')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.characters = false
  }
}

async function handlePlanScenes() {
  loading.scenes = true
  try {
    const result = await planScenes(chapters.value, characters.value)
    scenes.value = result.scenes
    ElMessage.success('场景已生成')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.scenes = false
  }
}

async function handleGenerateYaml() {
  loading.yaml = true
  try {
    const result = await generateYaml(title.value, chapters.value, characters.value, scenes.value)
    yamlText.value = result.yamlText
    validation.value = null
    ElMessage.success('YAML 已生成')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.yaml = false
  }
}

async function handleGenerateAll() {
  if (!novelText.value.trim()) {
    ElMessage.warning('小说正文不能为空')
    return
  }

  loading.all = true
  try {
    const result = await generateFromText(title.value, novelText.value)
    chapters.value = result.chapters
    characters.value = result.characters
    scenes.value = result.scenes
    yamlText.value = result.yamlText
    validation.value = null
    ElMessage.success('剧本已生成')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.all = false
  }
}

async function handleValidateYaml() {
  loading.validate = true
  try {
    validation.value = await validateYaml(yamlText.value)
    if (!validation.value.valid) {
      ElMessage.warning('校验未通过')
    } else if (validationWarnings.value.length) {
      ElMessage.warning('校验通过，有质量提示')
    } else {
      ElMessage.success('校验通过')
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.validate = false
  }
}

function handleDownload() {
  if (!canExport.value) {
    ElMessage.warning('请先通过 YAML 校验')
    return
  }

  const blob = new Blob([yamlText.value], { type: 'text/yaml;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'screenplay.yaml'
  link.click()
  URL.revokeObjectURL(url)
}

async function handleCopyYaml() {
  if (!yamlText.value) {
    ElMessage.warning('暂无可复制的 YAML')
    return
  }

  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(yamlText.value)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = yamlText.value
      textarea.setAttribute('readonly', '')
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('YAML 已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

function loadSampleText() {
  title.value = '雨夜来客'
  novelText.value = `第一章 雨夜
雨声敲在旧事务所的玻璃窗上。林默合上档案，听见门外传来急促的脚步声。

许岚推门进来，手里攥着一张被雨水浸湿的照片。她说哥哥失踪前最后一次出现，就是在城南旧仓库。

第二章 旧仓库
林默和许岚来到仓库。手电光扫过墙面，照出一排新鲜的泥脚印。远处忽然传来铁门合上的声音。`
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <Sparkles :size="22" />
        <div>
          <h1>AI Novel Screenplay</h1>
          <span>小说转剧本工作台</span>
        </div>
      </div>
      <el-tag type="success" effect="plain">MVP</el-tag>
    </header>

    <main class="workspace">
      <aside class="rail">
        <el-steps :active="activeStep" direction="vertical" finish-status="success">
          <el-step title="原文" />
          <el-step title="章节" />
          <el-step title="角色" />
          <el-step title="场景" />
          <el-step title="YAML" />
        </el-steps>
      </aside>

      <section class="panel input-panel">
        <div class="panel-head">
          <h2>原文</h2>
          <el-button text :icon="FileText" @click="loadSampleText">示例</el-button>
        </div>

        <el-input v-model="title" class="title-input" placeholder="剧本标题" />

        <div class="upload-row">
          <el-upload
            accept=".txt"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
          >
            <el-button :icon="Upload">选择文件</el-button>
          </el-upload>
          <el-button type="primary" :loading="loading.upload" @click="handleUpload">
            上传
          </el-button>
        </div>

        <div v-if="uploadInfo" class="meta-line">
          <span>{{ uploadInfo.fileName }}</span>
          <span>{{ uploadInfo.length }} 字</span>
        </div>

        <el-input
          v-model="novelText"
          class="novel-editor"
          type="textarea"
          resize="none"
          placeholder="粘贴小说正文"
        />

        <div class="summary-strip">
          <span>{{ chapters.length }} 章节</span>
          <span>{{ characters.length }} 角色</span>
          <span>{{ scenes.length }} 场景</span>
        </div>

        <div class="action-row">
          <el-button
            type="primary"
            :icon="Sparkles"
            :loading="loading.all"
            @click="handleGenerateAll"
          >
            一键生成
          </el-button>
          <el-button
            :icon="BookOpen"
            :loading="loading.split"
            @click="handleSplit"
          >
            拆分章节
          </el-button>
        </div>
      </section>

      <section class="panel outline-panel">
        <div class="panel-head">
          <h2>结构</h2>
          <div class="toolbar">
            <el-button
              :icon="Users"
              :disabled="!chapters.length"
              :loading="loading.characters"
              @click="handleExtractCharacters"
            >
              角色
            </el-button>
            <el-button
              :icon="ListChecks"
              :disabled="!characters.length"
              :loading="loading.scenes"
              @click="handlePlanScenes"
            >
              场景
            </el-button>
          </div>
        </div>

        <el-tabs>
          <el-tab-pane :label="`章节 ${chapters.length}`">
            <div class="list">
              <article v-for="chapter in chapters" :key="chapter.chapterIndex" class="item-card">
                <strong>{{ chapter.chapterIndex }}. {{ chapter.title }}</strong>
                <p>{{ chapter.wordCount }} 字</p>
              </article>
              <el-empty v-if="!chapters.length" :image-size="90" description="暂无章节" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`角色 ${characters.length}`">
            <div class="list">
              <article v-for="character in characters" :key="character.id" class="item-card">
                <strong>{{ character.name }}</strong>
                <p>{{ character.description }}</p>
                <span>{{ character.id }}</span>
              </article>
              <el-empty v-if="!characters.length" :image-size="90" description="暂无角色" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`场景 ${scenes.length}`">
            <div class="list">
              <article v-for="scene in scenes" :key="scene.sceneId" class="item-card">
                <strong>{{ scene.sceneNumber }}. {{ scene.title }}</strong>
                <p>{{ scene.location }} · {{ scene.time }}</p>
                <span>{{ scene.summary }}</span>
              </article>
              <el-empty v-if="!scenes.length" :image-size="90" description="暂无场景" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>

      <section class="panel yaml-panel">
        <div class="panel-head">
          <h2>YAML</h2>
          <div class="toolbar">
            <el-button
              :icon="Wand2"
              :disabled="!scenes.length"
              :loading="loading.yaml"
              @click="handleGenerateYaml"
            >
              生成
            </el-button>
            <el-button
              :icon="CheckCircle2"
              :disabled="!yamlText"
              :loading="loading.validate"
              @click="handleValidateYaml"
            >
              校验
            </el-button>
            <el-button :icon="Copy" :disabled="!yamlText" @click="handleCopyYaml">
              复制
            </el-button>
            <el-button :icon="Download" :disabled="!canExport" @click="handleDownload">
              导出
            </el-button>
          </div>
        </div>

        <el-input
          v-model="yamlText"
          class="yaml-editor"
          type="textarea"
          resize="none"
          spellcheck="false"
          placeholder="生成后的 YAML"
        />

        <div class="export-status" :class="{ ready: canExport }">
          {{ canExport ? '当前 YAML 已校验，可导出' : '校验通过后可导出 screenplay.yaml' }}
        </div>

        <div v-if="validation" class="validation" :class="{ valid: validation.valid }">
          <strong>{{ validation.valid ? '通过' : '未通过' }}</strong>
          <span>{{ validation.characterCount }} 角色 · {{ validation.sceneCount }} 场景</span>

          <div v-if="validation.errors.length" class="validation-group">
            <b>错误</b>
            <ul>
              <li v-for="error in validation.errors" :key="error">{{ error }}</li>
            </ul>
          </div>

          <div v-if="validationWarnings.length" class="validation-group warning">
            <b>警告</b>
            <ul>
              <li v-for="warning in validationWarnings" :key="warning">{{ warning }}</li>
            </ul>
          </div>

          <ul v-if="!validation.errors.length && !validationWarnings.length">
            <li>结构完整，未发现错误或警告。</li>
          </ul>
        </div>
      </section>
    </main>
  </div>
</template>
