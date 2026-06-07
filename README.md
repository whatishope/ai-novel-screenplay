# AI Novel Screenplay

AI Novel Screenplay 是一个 AI 小说转剧本 MVP 工具。它可以把小说文本拆分为章节，提取角色，规划剧本场景，并生成可编辑、可校验、可导出的 YAML 剧本。

## 功能概览

- 上传 UTF-8 `.txt` 小说文本。
- 自动拆分章节，支持常见中文章节标题和英文 Chapter 标题。
- 提取角色 ID、姓名、描述、性格、目标和首次出现章节。
- 规划剧本场景，包含地点、时间、出场角色、摘要和来源章节。
- 生成结构化 YAML 剧本，包含 `metadata`、`characters`、`relationships`、`scenes` 和 `production`。
- 校验 YAML 结构，返回错误、警告、角色数量、关系数量和场景数量。
- 在前端工作台编辑、复制、校验并导出 `screenplay.yaml`。

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Maven
- SnakeYAML

前端：

- Vue 3
- Vite
- Element Plus
- lucide-vue-next

## 项目结构

```text
backend/                 Spring Boot 后端
frontend/                Vue 前端工作台
docs/                    产品设计、Schema 和 API 文档
examples/                示例 YAML
```

## 本地启动

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

健康检查：

```text
GET http://localhost:8080/api/health
```

### 2. 启动前端

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

Vite 会显示本地访问地址，通常是：

```text
http://localhost:5173
```

PowerShell 如果拦截 `npm`，请使用 `npm.cmd`。

## 演示流程

1. 打开前端工作台。
2. 点击“示例”填入演示小说，或上传 UTF-8 `.txt` 文件。
3. 点击“一键生成”，等待章节、角色、场景和 YAML 生成完成。
4. 在 YAML 区域查看或编辑生成结果。
5. 点击“校验”，确认结构是否通过。
6. 校验通过后点击“导出”，下载 `screenplay.yaml`。
7. 如需重新演示，点击顶栏“重置”。

## 测试与构建

后端测试：

```bash
cd backend
mvn test
```

前端构建：

```bash
cd frontend
npm.cmd run build
```

## 文档

- [产品设计](docs/product-design.md)
- [YAML Schema](docs/yaml-schema.md)
- [API Reference](docs/api-reference.md)
- [示例 YAML](examples/sample-screenplay.yaml)

## MVP 范围

本项目聚焦小说文本到 YAML 剧本初稿的一条完整演示链路。登录、多项目管理、数据库持久化、多人协作和复杂权限控制不在 MVP 范围内。
