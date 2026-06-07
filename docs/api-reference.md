# API Reference

This document describes the backend API used by the MVP frontend.

Base URL in local development:

```text
http://localhost:8080
```

The Vite frontend proxies `/api` requests to this backend during development.

## Response Format

All JSON endpoints return the same envelope:

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

Validation and business errors return HTTP `400`:

```json
{
  "code": 400,
  "message": "Novel text must not be blank.",
  "data": null
}
```

## Health

### `GET /api/health`

Returns backend availability status.

Response data:

```json
"AI Novel Screenplay backend is running"
```

## Novel APIs

### `POST /api/novel/upload`

Uploads a UTF-8 `.txt` novel file.

Content type:

```text
multipart/form-data
```

Form fields:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `file` | file | Yes | UTF-8 `.txt` novel file. |

Response data:

```json
{
  "fileName": "sample-novel.txt",
  "length": 1200,
  "preview": "First 500 characters..."
}
```

Notes:

- Only `.txt` files are accepted.
- File size must not exceed `5MB`.
- Uploaded text must be valid UTF-8.

### `POST /api/novel/split-chapters`

Splits novel text into chapters.

Request:

```json
{
  "text": "Chapter 1\nA visitor arrives.\nChapter 2\nThe warehouse opens."
}
```

Response data:

```json
{
  "chapterCount": 2,
  "chapters": [
    {
      "chapterIndex": 1,
      "title": "Chapter 1",
      "content": "A visitor arrives.",
      "wordCount": 18
    }
  ]
}
```

## Screenplay APIs

### `POST /api/screenplay/extract-characters`

Extracts characters from chapter data.

Request:

```json
{
  "chapters": [
    {
      "chapterIndex": 1,
      "title": "Chapter 1",
      "content": "A visitor arrives.",
      "wordCount": 18
    }
  ]
}
```

Response data:

```json
{
  "characters": [
    {
      "id": "char_001",
      "name": "Lin",
      "description": "Detective",
      "personality": "Calm",
      "goal": "Find the truth",
      "firstAppearedChapter": 1
    }
  ]
}
```

### `POST /api/screenplay/plan-scenes`

Plans screenplay scenes from chapters and characters.

Request:

```json
{
  "chapters": [],
  "characters": []
}
```

Response data:

```json
{
  "scenes": [
    {
      "sceneId": "scene_001",
      "sceneNumber": 1,
      "title": "Opening",
      "location": "Office",
      "time": "Night",
      "characters": ["char_001"],
      "summary": "Lin meets the visitor.",
      "sourceChapter": 1
    }
  ]
}
```

### `POST /api/screenplay/generate-yaml`

Generates structured screenplay YAML from chapters, characters, and scenes.

Request:

```json
{
  "title": "Demo",
  "chapters": [],
  "characters": [],
  "scenes": []
}
```

Response data:

```json
{
  "yamlText": "metadata:\n  title: Demo\n",
  "sceneCount": 1,
  "characterCount": 2
}
```

### `POST /api/screenplay/validate-yaml`

Validates screenplay YAML structure.

Request:

```json
{
  "yamlText": "metadata:\n  title: Demo\n"
}
```

Response data:

```json
{
  "valid": false,
  "errors": ["characters is required."],
  "warnings": ["production is missing; export will not include production notes."],
  "sceneCount": 0,
  "characterCount": 0
}
```

Notes:

- `errors` determine whether `valid` is `true`.
- `warnings` are quality or completeness hints and do not fail validation.

### `POST /api/screenplay/generate-from-text`

Runs the full generation workflow from raw novel text.

Request:

```json
{
  "title": "Demo",
  "text": "Chapter 1\nA visitor arrives."
}
```

Response data:

```json
{
  "chapters": [],
  "characters": [],
  "scenes": [],
  "yamlText": "metadata:\n  title: Demo\n",
  "chapterCount": 1,
  "characterCount": 2,
  "sceneCount": 3
}
```

Workflow:

1. Split novel text into chapters.
2. Extract characters.
3. Plan screenplay scenes.
4. Generate screenplay YAML.

## Local Development

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```
