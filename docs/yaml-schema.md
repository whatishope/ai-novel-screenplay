# AI 小说转剧本 YAML Schema 设计文档

## 1. Schema 设计目标

AI 小说转剧本工具需要把小说章节内容转换为稳定、可编辑、可追溯、可扩展的结构化剧本。YAML Schema 的设计目标如下：

- 可编辑性：字段命名清晰，便于创作者直接阅读和修改。
- 可追溯性：场景、动作和对白能够关联到原文章节或文本片段。
- 可扩展性：后续可以增加分镜、拍摄信息、质量报告和多种剧本风格。
- 机器可解析：后端可以使用 SnakeYAML 解析、校验和重新生成局部内容。
- 演示友好：结构不依赖数据库，适合 MVP 阶段导入、导出和前端展示。

## 2. 顶层字段说明

完整剧本 YAML 建议包含以下顶层字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `metadata` | object | 是 | 剧本基础信息，例如标题、语言、风格、来源章节数。 |
| `characters` | array | 是 | 角色列表，描述人物身份、性格和目标。 |
| `relationships` | array | 否 | 角色关系，用于表达亲属、同伴、对立等关系。 |
| `scenes` | array | 是 | 剧本场景列表，是剧本主体内容。 |
| `production` | object | 否 | 面向拍摄或制作的扩展信息。 |

## 3. `metadata` 字段

`metadata` 描述剧本整体信息。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | string | 是 | 剧本标题。 |
| `source_title` | string | 否 | 原小说或上传文本名称。 |
| `language` | string | 是 | 语言，例如 `zh-CN`。 |
| `genre` | string | 否 | 类型，例如悬疑、都市、奇幻。 |
| `style` | string | 否 | 剧本风格，例如短剧、电影、舞台剧。 |
| `chapter_count` | number | 是 | 来源章节数量。 |
| `generated_at` | string | 否 | 生成时间，建议使用 ISO-8601 格式。 |
| `version` | string | 是 | Schema 或生成结果版本。 |

示例：

```yaml
metadata:
  title: 雨夜来客
  source_title: 示例小说
  language: zh-CN
  genre: 悬疑
  style: 短剧
  chapter_count: 3
  generated_at: "2026-06-06T18:30:00+08:00"
  version: "1.0"
```

## 4. `characters` 字段

`characters` 是角色列表。每个角色应有稳定 ID，方便在场景和对白中引用。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 是 | 角色唯一标识，例如 `char_001`。 |
| `name` | string | 是 | 角色姓名。 |
| `description` | string | 否 | 身份或背景描述。 |
| `personality` | string | 否 | 性格特点。 |
| `goal` | string | 否 | 角色在故事中的目标。 |
| `first_appeared_chapter` | number | 否 | 首次出现章节。 |

示例：

```yaml
characters:
  - id: char_001
    name: 林默
    description: 年轻侦探
    personality: 冷静、敏锐
    goal: 查明雨夜失踪案真相
    first_appeared_chapter: 1
```

## 5. `relationships` 字段

`relationships` 描述角色之间的关系，便于前端展示人物关系，也便于后续做一致性检查。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `from` | string | 是 | 起始角色 ID。 |
| `to` | string | 是 | 目标角色 ID。 |
| `type` | string | 是 | 关系类型，例如同伴、对手、亲属。 |
| `description` | string | 否 | 关系补充说明。 |

示例：

```yaml
relationships:
  - from: char_001
    to: char_002
    type: 同伴
    description: 林默和许岚共同调查旧仓库事件。
```

## 6. `scenes` 字段

`scenes` 是剧本主体。场景编号应连续，场景 ID 应稳定，便于局部重新生成。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `scene_id` | string | 是 | 场景唯一标识，例如 `scene_001`。 |
| `scene_number` | number | 是 | 场景序号，从 1 开始连续编号。 |
| `title` | string | 是 | 场景标题。 |
| `location` | string | 否 | 场景地点。 |
| `time` | string | 否 | 场景时间，例如夜晚、清晨。 |
| `characters` | array | 否 | 出场角色 ID 列表。 |
| `summary` | string | 是 | 场景摘要。 |
| `conflict` | string | 否 | 场景核心冲突。 |
| `actions` | array | 否 | 动作、调度和画面描述。 |
| `dialogues` | array | 否 | 对白列表。 |
| `source_trace` | object | 否 | 场景来源追溯信息。 |

### 6.1 `actions` 字段

`actions` 表示场景中的动作或画面。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `order` | number | 是 | 动作顺序。 |
| `content` | string | 是 | 动作描述。 |
| `source_trace` | object | 否 | 动作来源追溯信息。 |

### 6.2 `dialogues` 字段

`dialogues` 表示场景中的对白。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `order` | number | 是 | 对白顺序。 |
| `character` | string | 是 | 说话角色 ID。 |
| `character_name` | string | 否 | 角色姓名，方便前端直接显示。 |
| `content` | string | 是 | 对白内容。 |
| `tone` | string | 否 | 语气，例如低声、急切、冷静。 |
| `source_trace` | object | 否 | 对白来源追溯信息。 |

## 7. `source_trace` 字段

`source_trace` 用于记录剧本内容与原文章节之间的关系。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `chapter_index` | number | 是 | 来源章节序号。 |
| `chapter_title` | string | 否 | 来源章节标题。 |
| `paragraph_range` | string | 否 | 来源段落范围，例如 `1-4`。 |
| `note` | string | 否 | 追溯说明。 |

示例：

```yaml
source_trace:
  chapter_index: 1
  chapter_title: 第一章 雨夜
  paragraph_range: "1-5"
  note: 根据主角进入旧仓库的情节改编。
```

## 8. `production` 字段

`production` 面向后续制作扩展。MVP 阶段可以为空或只包含基础字段。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `estimated_duration` | string | 否 | 预计时长，例如 `5 分钟`。 |
| `target_format` | string | 否 | 输出形式，例如短剧、电影、分镜。 |
| `notes` | array | 否 | 制作备注。 |

示例：

```yaml
production:
  estimated_duration: 5 分钟
  target_format: 短剧
  notes:
    - 雨声和昏暗灯光是主要氛围元素。
```

## 9. 完整 YAML 示例

完整示例见：

```text
examples/sample-screenplay.yaml
```

## 10. 设计原因说明

本 Schema 采用 `metadata`、`characters`、`relationships`、`scenes` 和 `production` 的分层结构，原因如下：

- `metadata` 独立保存剧本整体信息，方便前端展示和文件管理。
- `characters` 使用稳定 ID，避免角色改名后场景引用失效。
- `relationships` 单独建模，方便后续生成人物关系图和一致性检查。
- `scenes` 作为主体结构，贴合剧本创作和前端预览的核心流程。
- `dialogues` 和 `actions` 分开保存，便于用户区分画面动作和人物对白。
- `source_trace` 支持从剧本追溯到原文章节，降低 AI 改编不可控的问题。
- `production` 保留扩展空间，后续可以加入分镜、镜头、场务和拍摄信息。

这个设计既能满足 MVP 阶段的生成、编辑、校验和导出需求，也能支持后续单场景重新生成、剧情遗漏检查、人物一致性检查和多风格剧本输出。
